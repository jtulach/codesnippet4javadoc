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
import java.util.HashMap;
import java.util.Map;

final class SnippetCollection {
    private final DocErrorReporter reporter;
    private final Map<String, String> snippets;
    private final Map<String, Map<String, String>> perFileSnippets;

    SnippetCollection(DocErrorReporter reporter) {
        this.reporter = reporter;
        this.snippets = new HashMap<>();
        this.perFileSnippets = new HashMap<>();
    }

    final void registerSnippet(String file, String key, String code) {
        this.snippets.put(key, code);

        Map<String, String> local = this.perFileSnippets.get(file);
        if (local == null) {
            local = new HashMap<>();
            this.perFileSnippets.put(file, local);
        }
        local.put(key, code);
    }

    final String findGlobalSnippet(Doc element, String key) {
        String code = snippets.get(key);
        if (code == null) {
            reporter.printWarning(element.position(), code = "Snippet '" + key + "' not found.");
        }
        return code;
    }

    final String findSnippet(Doc element, String file, String key) {
        Map<String, String> snip = perFileSnippets.get(file);
        String code = snip == null ? null : snip.get(key);
        if (code == null) {
            reporter.printWarning(element.position(), code = "Snippet '" + key + "' in file '" + file + "' not found.");
        }
        return code;
    }
}
