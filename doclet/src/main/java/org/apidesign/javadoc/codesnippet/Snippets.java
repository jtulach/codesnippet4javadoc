/**
 * Codesnippet Javadoc Doclet
 * Copyright (C) 2015-2020 Jaroslav Tulach - jaroslav.tulach@apidesign.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.0 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-3.0.
 */
package org.apidesign.javadoc.codesnippet;

import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Tag;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Snippets {
    private static final Pattern TAG = Pattern.compile("\\{ *@codesnippet *([\\.\\-a-z0-9A-Z#]*) *\\}");
    private static final Pattern SNIPPET = Pattern.compile("\\{ *@snippet (( *\\w+ *= *\"[^\"]+\")*) *[:\\}]");
    private static final Pattern SNIPPET_ATTR = Pattern.compile(" *(\\w+) *= *\"([^\"]+)\"");
    private static final Pattern LINKTAG = Pattern.compile("\\{ *@link *([\\.\\-a-z0-9A-Z#]*) *\\}");
    private static final Pattern PACKAGE = Pattern.compile(" *package *([\\p{Alnum}\\.]+);");
    private static final Pattern IMPORT = Pattern.compile(" *import *([\\p{Alnum}\\.\\*]+);");
    private static final Pattern LEGACY_BEGIN = Pattern.compile(".* (BEGIN: *)(\\p{Graph}+)[-\\> ]*");
    private static final Pattern LEGACY_END = Pattern.compile(".* (END|FINISH): *(\\p{Graph}+)[-\\> ]*");
    private static final Pattern BEGIN = Pattern.compile(".* (@start *region=\")(\\p{Graph}+)[\"-\\> ]*");
    private static final Pattern END = Pattern.compile(".* (@end *)(\\p{Graph}*)[\"-\\> ]*");
    private final DocErrorReporter reporter;
    private final List<Path> search = new ArrayList<>();
    private final List<Path> visible = new ArrayList<>();
    private final List<Pattern> classes = new ArrayList<>();
    private SnippetCollection snippets;
    private int maxLineLength = 80;
    private String verifySince;
    private String encoding;
    private Set<String> hiddenAnno;
    private boolean modeJep413 = true;
    private boolean modeLegacy = true;

    Snippets(DocErrorReporter reporter) {
        this.reporter = reporter;
    }

    void fixCodesnippets(Doc enclosingElement, Doc element) {
        try {
            for (;;) {
                final String txt = element.getRawCommentText();
                final String[] code = { null };
                final int[] end = { -1 };
                Matcher match = null;
                if (modeJep413) {
                    match = matchSnippet(this::getSnippet, element, txt, code, end);
                }
                if (match == null) {
                    if (modeLegacy) {
                        match = matchLegacyCodeSnippet(this::getSnippet, element, txt, code, end);
                    }
                    if (match == null) {
                        break;
                    }
                }
                String newTxt = txt.substring(0, match.start(0)) +
                    code[0] +
                    txt.substring(end[0]);
                element.setRawCommentText(newTxt);
            }
            element.inlineTags();
            if (verifySince != null) {
                verifySinceTag(element, enclosingElement, verifySince);
            }
        } catch (IOException ex) {
            Logger.getLogger(Snippets.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static Matcher matchSnippet(
        SnippetCollection snippets,
        String txt, String[] code, int[] end
    ) {
        return matchSnippet((__) -> snippets, null, txt, code, end);
    }

    private static Matcher matchSnippet(
        Function<Doc, SnippetCollection> snippets, Doc element,
        String txt, String[] code, int[] end
    ) {
        Matcher match = SNIPPET.matcher(txt);
        if (match.find()) {
            int s = match.start();
            int colon = txt.indexOf(':', s);
            if (colon != -1) {
                int curly = 1;
                end[0] = colon + 1;
                for (;;) {
                    char ch = txt.charAt(end[0]++);
                    if (ch == '}') {
                        if (--curly <= 0) {
                            break;
                        }
                    }
                    if (ch == '{') {
                        curly++;
                    }
                }
                code[0] = pre(CodeSnippet.boldJavaKeywords(
                    txt.substring(colon + 1, end[0] - 1),
                    Collections.emptyMap(), Collections.emptySet()
                ));
            } else {
                Map<String,String> attr = parseAttributes(match.group(1));
                code[0] = pre(snippets.apply(element).findSnippet(element, attr.get("file"), attr.get("region")));
                end[0] = match.end();
            }
            return match;
        }
        return null;
    }

    private static String pre(String code) {
        return "<pre class='snippet'>" + code + "</pre>";
    }

    Matcher matchLegacyCodeSnippet(
        Function<Doc, SnippetCollection> snippets, Doc element,
        String txt, String[] code, int[] end
    ) {
        Matcher match = TAG.matcher(txt);
        if (!match.find()) {
            if (classes.isEmpty()) {
                return null;
            }
            match = LINKTAG.matcher(txt);
            if (!findLinkSnippet(match)) {
                return null;
            }
        }
        code[0] = pre(snippets.apply(element).findGlobalSnippet(element, match.group(1)));
        end[0] = match.end(0);
        return match;
    }

    private static Map<String, String> parseAttributes(String txt) {
        Map<String, String> attrs = new HashMap<>();
        Matcher m = SNIPPET_ATTR.matcher(txt);
        while (m.find()) {
            attrs.put(m.group(1), m.group(2));
        }
        return attrs;
    }

    private boolean verifySinceTag(Doc element, Doc enclosingElement, String expVersion) throws IOException {
        for (Tag t : element.tags()) {
            if (t.name().equals("@since")) {
                return false;
            }
        }
        if (enclosingElement.isEnum() && element.isMethod()) {
            if (element.name().equals("valueOf") || element.name().equals("values")) {
                // skip these well known autogenerated methods
                return false;
            }
        }
        reporter.printWarning(element.position(), "missing @since tag for " + element);
        if (!expVersion.isEmpty()) {
            addSinceTag(element, expVersion);
            return true;
        } else {
            return false;
        }
    }

    private void addSinceTag(Doc element, final String version) throws IOException {
        final File f = element.position().file();
        int index = element.position().line();
        List<String> lines = Files.readAllLines(f.toPath(), Charset.forName("UTF-8"));
        boolean second = false;
        for (;;) {
            String l = lines.get(--index);
            int at = l.indexOf("*/");
            if (at >= 0) {
                if (l.contains("@since " + version)) {
                    break;
                }
                lines.set(index, l.substring(0, at) + "@since " + version + " */");
                break;
            }
            if (l.isEmpty()) {
                lines.set(index, l + "/** @since " + version + " */");
                break;
            }
            if (l.endsWith(";")) {
                if (second) {
                    lines.set(index, l + " /** @since " + version + " */");
                    break;
                }
                second = true;
            }
        }
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            for (String l : lines) {
                w.write(l);
                w.newLine();
            }
        }
    }

    private boolean findLinkSnippet(Matcher match) {
        for (;;) {
            if (!match.find()) {
                return false;
            }
            String className = match.group(1);
            for(Pattern p : classes) {
                if (p.matcher(className).matches()) {
                    return true;
                }
            }
        }
    }

    SnippetCollection getSnippet(Doc element) {
        if (snippets == null) {
            SnippetCollection tmp = new SnippetCollection(reporter);
            final Map<String,String> topClasses = new TreeMap<>();
            for (Path path : visible) {
                if (!Files.isDirectory(path)) {
                    printWarning(null, "Cannot scan " + path + " not a directory!");
                    continue;
                }
                try {
                    CodeSnippet.collectClasses(path, topClasses, this);
                } catch (IOException ex) {
                    printError(element, "Cannot read " + path + ": " + ex.getMessage());
                }
            }
            for (Path path : search) {
                if (!Files.isDirectory(path)) {
                    printWarning(null, "Cannot scan " + path + " not a directory!");
                    continue;
                }
                try {
                    CodeSnippet.scanDir(path, topClasses, tmp, this);
                } catch (IOException ex) {
                    printError(element, "Cannot read " + path + ": " + ex.getMessage());
                }
            }
            snippets = tmp;
        }
        return snippets;
    }

    void addPath(Path path, boolean useLink) {
        search.add(path);
        if (useLink) {
            visible.add(path);
        }
    }

    void addClasses(String classRegExp) {
        classes.add(Pattern.compile(classRegExp));
    }

    final void printNotice(Doc where, String msg) {
        if (reporter != null) {
            if (where == null) {
                reporter.printNotice(msg);
            } else {
                reporter.printNotice(where.position(), msg);
            }
        } else {
            throw new IllegalStateException(msg);
        }
    }

    final void printWarning(Doc where, String msg) {
        if (reporter != null) {
            if (where == null) {
                reporter.printWarning(msg);
            } else {
                reporter.printWarning(where.position(), msg);
            }
        } else {
            throw new IllegalStateException(msg);
        }
    }

    final void printError(Doc where, String msg) {
        if (reporter != null) {
            if (where == null) {
                reporter.printError(msg);
            } else {
                reporter.printError(where.position(), msg);
            }
        } else {
            throw new IllegalStateException(msg);
        }
    }

    void setMaxLineLength(String maxLineLength) {
        if ( maxLineLength != null ) {
            try {
                this.maxLineLength = Integer.parseInt( maxLineLength );
            }
            catch (NumberFormatException ex) {

            }
        }
    }

    int getMaxLineLength() {
        return this.maxLineLength;
    }

    void setVerifySince(String sinceCheck) {
        this.verifySince = sinceCheck;
    }

    void addHiddenAnnotation(String fqn) {
        if (this.hiddenAnno == null) {
            this.hiddenAnno = new HashSet<>();
        }
        this.hiddenAnno.add(fqn);
    }

    boolean isHiddingAnnotation(String name) {
        return this.hiddenAnno != null && this.hiddenAnno.contains(name);
    }

    void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    Charset getEncoding() {
        Charset charset = Charset.defaultCharset();
        if (encoding != null && !encoding.isEmpty()) {
            charset = Charset.forName(encoding);
        }
        return charset;
    }

    void setModeJep413(boolean b) {
        this.modeJep413 = b;
    }

    void setModeLegacy(boolean b) {
        this.modeLegacy = b;
    }

    Matcher packageMatcher(CharSequence line) {
        return PACKAGE.matcher(line);
    }

    Matcher importMatcher(CharSequence line) {
        return IMPORT.matcher(line);
    }

    Matcher startMatcher(CharSequence line) {
        if (modeLegacy) {
            Matcher m = LEGACY_BEGIN.matcher(line);
            if (m.matches()) {
                return m;
            }
        }
        if (modeJep413) {
            return BEGIN.matcher(line);
        } else {
            return noMatch();
        }
    }

    Matcher endMatcher(CharSequence line) {
        if (modeLegacy) {
            Matcher m = LEGACY_END.matcher(line);
            if (m.matches()) {
                return m;
            }
        }
        if (modeJep413) {
            return END.matcher(line);
        } else {
            return noMatch();
        }
    }

    private static Matcher noMatch() {
        Pattern noMatch = PACKAGE;
        return noMatch.matcher("");
    }
}
