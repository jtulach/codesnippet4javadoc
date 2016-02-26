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

/** Show snippet {@link #mul(int, int) embedded} in the API source.
 */
public final class EmbeddingSampleCode {
    private EmbeddingSampleCode() {
    }

    /** Here is a way to include snippet code next to your API.
     * If you want the sample code to be available in the sources of your
     * application and easily navigate to it in an IDE, you can use regular
     * <b>link</b> tag with a reference to a class named in a special way:
     * {@link EmbeddedSnippet#fourtyTwo}
     * Put the class at the end of the API file (it needs to be package private)
     * and it is guaranteed your snippet code will properly compile every
     * time your API compiles.
     *
     * @param x first number
     * @param y second number
     * @return their multiplication
     */
    public static int mul(int x, int y) {
        return x * y;
    }
}

class EmbeddedSnippet {
    // BEGIN: EmbeddedSnippet#fourtyTwo
    public static int fourtyTwo() {
        return EmbeddingSampleCode.mul(6, 7);
    }
    // END: EmbeddedSnippet#fourtyTwo
}
