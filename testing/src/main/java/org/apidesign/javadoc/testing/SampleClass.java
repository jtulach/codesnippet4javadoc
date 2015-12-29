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
package org.apidesign.javadoc.testing;

/** My sample class.
 * {@codesnippet sample1}
 * Rest of the text.
 */
public class SampleClass {
    private SampleClass() {
    }

    private static void sample1() {
        // BEGIN: sample1
        int x = 42;
        // END: sample1
    }

    /** Show snippet from test code:
     * {@codesnippet read.in.test}
     * End of snippet.
     */
    public static void initialize() {
    }
}
