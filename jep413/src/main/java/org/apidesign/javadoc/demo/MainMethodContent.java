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
package org.apidesign.javadoc.demo;

/** Snippet demo showing content of {@code main} method:
 *
 * {@snippet file="org/apidesign/javadoc/demo/MainMethodContent.java" region="main"}
 *
 * The snippet is extracted from region {@code main} defined in the 
 * {@code MainMethodContent} file below.
 */
public final class MainMethodContent {
    // @start region="main"
    public static void main(String... args) throws Exception {
        System.out.println("Better Javadoc for Everyone!");
    }
    // @end region="main"

    private MainMethodContent() {
    }
}
