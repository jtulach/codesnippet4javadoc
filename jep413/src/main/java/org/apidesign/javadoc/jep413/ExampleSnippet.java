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

package org.apidesign.javadoc.jep413;

class ExampleSnippet {
    static void show(String s) {
        // @start region="demo"
        if (!s.isEmpty()) {
            SampleClass.show("text: " + s);
        }
        // @end region="demo"
    }
    static void noEnd(String s) {
        // @start region="endless"
        if (!s.isEmpty()) {
            SampleClass.simpleend("noEnd: " + s);
        }
        // @end
    }
    static int asInt(String s) {
        // @start region="asInt"
        return Integer.valueOf(s);
        // @end
    }
}
