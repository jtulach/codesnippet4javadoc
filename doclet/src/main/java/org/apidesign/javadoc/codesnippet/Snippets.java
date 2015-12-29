/**
 * Codesnippet Javadoc Doclet
 * Copyright (C) 2015-2016 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Snippets {
    private static final Pattern TAG = Pattern.compile("\\{ *@codesnippet *([a-z0-9A-Z]*) *\\}");
    private final DocErrorReporter reporter;
    private List<String> search = new ArrayList<>();
    private Map<String,String> snippets;

    Snippets(DocErrorReporter reporter) {
        this.reporter = reporter;
    }

    void fixCodesnippets(Doc element) {
        final String txt = element.getRawCommentText();
        Matcher match = TAG.matcher(txt);
        for (;;) {
            if (!match.find()) {
                break;
            }
            String newTxt = txt.substring(0, match.start(0)) +
                findSnippet(element, match.group(1)) +
                txt.substring(match.end(0));
            element.setRawCommentText(newTxt);
        }
    }

    private String findSnippet(Doc element, String key) {
        if (snippets == null) {
            Map<String,String> tmp = new TreeMap<>();
            for (String path : search) {
                File dir = new File(path);
                if (!dir.isDirectory()) {
                    reporter.printWarning("Cannot scan " + dir + " not a directory!");
                    continue;
                }
                scanDir(dir, tmp);
            }
            snippets = tmp;
        }
        String code = snippets.get(key);
        if (code == null) {
            reporter.printWarning(element.position(), code = "Snippet '" + key + "' not found.");
        }
        return "<pre>\n" + code + "</pre>";
    }

    void addPath(String path) {
        search.add(path);
    }

    private static void scanDir(File dir, Map<String, String> tmp) {
    }
}
