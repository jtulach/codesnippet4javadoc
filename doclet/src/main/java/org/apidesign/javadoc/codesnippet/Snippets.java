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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Snippets {
    private static final Pattern TAG = Pattern.compile("\\{ *@codesnippet *([a-z0-9A-Z]*) *\\}");

    void fixCodesnippets(Doc element) {
        final String txt = element.getRawCommentText();
        Matcher match = TAG.matcher(txt);
        for (;;) {
            if (!match.find()) {
                break;
            }
            String newTxt = txt.substring(0, match.start(0)) +
                findSnippet(match.group(1)) +
                txt.substring(match.end(0));
            element.setRawCommentText(newTxt);
        }
    }

    private static String findSnippet(String text) {
        return "<pre>\n" + text + "</pre>";
    }
}
