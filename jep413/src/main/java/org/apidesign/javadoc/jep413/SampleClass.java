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

/** Inline snippet. Text before.
 * {@snippet :
 * public static void main(String... args) {
 * }
 * }
 * Text after.
 */
public class SampleClass {
    private SampleClass() {
    }

    /** Show the string, if not empty. Before file snippet:
     * {@snippet file="org/apidesign/javadoc/jep413/ExampleSnippet.java" region="demo"}
     * After file snippet.
     *
     * @param s the text
     */
    public static void show(String s) {
    }
    /** Show the string, if not empty. Start snippet:
     * {@snippet file="org/apidesign/javadoc/jep413/ExampleSnippet.java" region="endless"}
     * End snippet.
     *
     * @param s the text
     */
    public static void simpleend(String s) {
    }
}
