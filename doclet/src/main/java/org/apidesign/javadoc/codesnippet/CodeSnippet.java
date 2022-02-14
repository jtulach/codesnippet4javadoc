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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CodeSnippet implements CharSequence {
    private StringBuilder sb = new StringBuilder();
    private int spaces = Integer.MAX_VALUE;
    private Stack<Integer> remove = new Stack<Integer>();
    private final Path file;
    private final Snippets snippets;

    public CodeSnippet(Path file, final Snippets outer) {
        this.snippets = outer;
        this.file = file;
    }

    public int length() {
        return sb.length();
    }

    public char charAt(int index) {
        return sb.charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    void append(String line) {
        for (int sp = 0; sp < line.length(); sp++) {
            if (line.charAt(sp) != ' ') {
                if (sp < spaces) {
                    spaces = sp;
                    break;
                }
            }
        }
        remove.push(sb.length());
        sb.append(line);
        sb.append('\n');
    }

    public String toString(Boolean finish, Map<String, String> imports, Set<String> packages) {
        final int len = snippets.getMaxLineLength();
        if (remove != null) {
            while (!remove.isEmpty()) {
                Integer pos = remove.pop();
                for (int i = 0; i < spaces; i++) {
                    if (sb.charAt(pos) == '\n') {
                        break;
                    }
                    sb.deleteCharAt(pos);
                }
            }
            remove = null;
            int line = 0;
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '\n') {
                    line = 0;
                    continue;
                }
                if (++line > len) {
                    snippets.printError(null, "Line is too long in: " + file + "\n" + sb);
                }
            }
            int open = countChar(sb, '{');
            int end = countChar(sb, '}');
            if (Boolean.TRUE.equals(finish)) {
                for (int i = 0; i < open - end; i++) {
                    int missingBraceIndent = findMissingIndentation(sb.toString());
                    while (missingBraceIndent-- > 0) {
                        sb.append(" ");
                    }
                    sb.append("}\n");
                }
            }
            if (finish != null && countChar(sb, '{') != countChar(sb, '}')) {
                snippets.printError(null, "not paired amount of braces (consider using '// FINISH:' instead of '// END:') in " + file + "\n" + sb);
            }
        }
        String xml = xmlize(sb.toString());
        if (javaName(file) != null) {
            return boldJavaKeywords(xml, imports, packages);
        } else {
            return xml;
        }
    }

    private static final Pattern WORDS = Pattern.compile("(\\w+)|(//.*)\n|(\"[^\"]*\")");
    static String boldJavaKeywords(String text, Map<String,String> imports, Set<String> packages) {
        Matcher m = WORDS.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String append;
            switch (m.group(0)) {
                case "abstract":
                case "assert":
                case "boolean":
                case "break":
                case "byte":
                case "case":
                case "catch":
                case "class":
                case "const":
                case "continue":
                case "default":
                case "do":
                case "double":
                case "else":
                case "enum":
                case "extends":
                case "final":
                case "finally":
                case "float":
                case "for":
                case "goto":
                case "char":
                case "if":
                case "implements":
                case "import":
                case "instanceof":
                case "int":
                case "interface":
                case "long":
                case "native":
                case "new":
                case "package":
                case "private":
                case "protected":
                case "public":
                case "return":
                case "short":
                case "static":
                case "strictfp":
                case "super":
                case "switch":
                case "synchronized":
                case "this":
                case "throw":
                case "throws":
                case "transient":
                case "try":
                case "void":
                case "volatile":
                case "while":
                case "true":
                case "false":
                case "null":
                    append = "<b>" + m.group(0) + "</b>";
                    break;
                default:
                    if (m.group(0).startsWith("//")) {
                        append = "<em>" + m.group(0).substring(0, m.group(0).length() - 1) + "</em>\n";
                        break;
                    }
                    if (m.group(0).startsWith("\"")) {
                        append = "<em>" + m.group(0) + "</em>";
                        break;
                    }
                    String fqn;
                    fqn = imports.get(m.group(0));
                    if (fqn == null) {
                        fqn = tryLoad("java.lang", m.group(0));
                        if (fqn == null && packages != null) {
                            for (String p : packages) {
                                fqn = tryLoad(p, m.group(0));
                                if (fqn != null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (fqn == null) {
                        append = m.group(0);
                    } else {
                        append = "{@link " + fqn + "}";
                    }
            }
            append = append.replace("\\", "\\\\")
                    .replace("$", "\\$");
            m.appendReplacement(sb, append);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static int findMissingIndentation(String unclosedText) {
        int closed = 0;
        int i = unclosedText.length() - 1;
        while (i >= 0) {
            char ch = unclosedText.charAt(i--);
            if (ch == '}') {
                closed++;
            }
            if (ch == '{') {
                if (closed-- == 0) {
                    break;
                }
            }
        }
        int spaces = 0;
        while (i >= 0) {
            char ch = unclosedText.charAt(i--);
            if (ch == ' ') {
                spaces++;
                continue;
            }
            if (ch == '\n' || ch == '\r') {
                break;
            }
            spaces = 0;
        }
        return spaces;
    }

    private static String tryLoad(String pkg, String name) {
        try {
            String loaded = pkg + "." + name;
            Class.forName(loaded);
            return loaded;
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private static final int countChar(CharSequence seq, char ch) {
        int cnt = 0;
        for (int i = 0; i < seq.length(); i++) {
            if (ch == seq.charAt(i)) {
                cnt++;
            }
        }
        return cnt;
    }

    private static String xmlize(CharSequence text) {
        String noAmp = text.toString().replaceAll("&", "&amp;");
        String noZav = noAmp.toString().replaceAll("@", "&#064;");
        String noLt = noZav.replaceAll("<", "&lt;");
        String noGt = noLt.replaceAll(">", "&gt;");
        return noGt;
    }

    static void collectClasses(Path dir, final Map<String, String> topClasses, Snippets snippets1) throws IOException {
        Files.walkFileTree(dir, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String javaName = CodeSnippet.javaName(file);
                if (javaName != null) {
                    try {
                        try (final BufferedReader r = Files.newBufferedReader(file, Charset.defaultCharset())) {
                            for (;;) {
                                String line = r.readLine();
                                if (line == null) {
                                    break;
                                }
                                Matcher pkgMatch = snippets1.packageMatcher(line);
                                if (pkgMatch.matches()) {
                                    final String fqn = pkgMatch.group(1);
                                    topClasses.put(javaName, fqn + '.' + javaName);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        snippets1.printError(null, "Cannot read " + file.toString() + " " + ex.getMessage());
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static void scanDir(Path dir, final Map<String, String> topClasses, final SnippetCollection collect, Snippets snip) throws IOException {
        Files.walkFileTree(dir, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fullName = CodeSnippet.fullName(dir, file);
                String javaName = CodeSnippet.javaName(file);
                Map<String, CharSequence> texts = new LinkedHashMap<>();
                Map<String, String> imports = new TreeMap<>(topClasses);
                Set<String> packages = new LinkedHashSet<>();
                Charset charset = snip.getEncoding();
                try (final BufferedReader r = Files.newBufferedReader(file, charset)) {
                    for (;;) {
                        String line = r.readLine();
                        if (line == null) {
                            break;
                        }
                        if (javaName != null) {
                            Matcher m = snip.importMatcher(line);
                            if (m.matches()) {
                                final String fqn = m.group(1);
                                if (fqn.endsWith(".*")) {
                                    packages.add(fqn.substring(0, fqn.length() - 2));
                                } else {
                                    int lastDot = fqn.lastIndexOf('.');
                                    imports.put(fqn.substring(lastDot + 1), fqn);
                                }
                            }
                        }
                        {
                            Matcher m = snip.startMatcher(line);
                            if (m.matches()) {
                                CodeSnippet sb = new CodeSnippet(file, snip);
                                CharSequence prev = texts.put(sectionName(m.group(2)), sb);
                                if (prev != null) {
                                    snip.printError(null, "Same pattern is there twice: " + m.group(1) + " in " + file);
                                }
                                continue;
                            }
                        }
                        {
                            Matcher m = snip.endMatcher(line);
                            if (m.matches()) {
                                String sectionName = sectionName(m.group(2));
                                if (sectionName.isEmpty()) {
                                    // find last
                                    Iterator<String> it = texts.keySet().iterator();
                                    while (it.hasNext()) {
                                        sectionName = it.next();
                                    }
                                }
                                final CharSequence s = texts.get(sectionName);
                                if (s instanceof CodeSnippet) {
                                    Boolean finish;
                                    if (m.group(1).startsWith("FINISH")) {
                                        finish = true;
                                    } else if (m.group(1).startsWith("END")) {
                                        finish = false;
                                    } else {
                                        finish = null;
                                    }
                                    texts.put(sectionName, ((CodeSnippet) s).toString(finish, imports, packages));
                                    continue;
                                }
                                if (s == null) {
                                    snip.printError(null, "Closing unknown section: " + m.group(2) + " in " + file);
                                    continue;
                                }
                                snip.printError(null, "Closing not opened section: " + m.group(2) + " in " + file);
                                continue;
                            }
                        }
                        for (CharSequence charSequence : texts.values()) {
                            if (charSequence instanceof CodeSnippet) {
                                CodeSnippet sb = (CodeSnippet) charSequence;
                                sb.append(line);
                            }
                        }
                    }
                } catch (MalformedInputException ex) {
                    snip.printNotice(null, "Skipping binary file " + file.toString());
                } catch (IOException ex) {
                    snip.printError(null, "Cannot read " + file.toString() + " " + ex.getMessage());
                }
                for (Map.Entry<String, CharSequence> entry : texts.entrySet()) {
                    CharSequence v = entry.getValue();
                    if (v instanceof CodeSnippet) {
                        snip.printError(null, "Not closed section " + entry.getKey() + " in " + file);
                    }
                    collect.registerSnippet(fullName, entry.getKey(), v.toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

        });
    }

    static String sectionName(String group) {
        if (group.startsWith("region=\"")) {
            group = group.substring(8);
        }
        return group.replaceAll("\"", "");
    }

    static String fullName(Path dir, Path f) {
        return dir.relativize(f).toString();
    }

    static String javaName(Path file1) {
        final String name = file1.getFileName().toString();
        return name.endsWith(".java") ? name.substring(0, name.length() - 5) : null;
    }
}
