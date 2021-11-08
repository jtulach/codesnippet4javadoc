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

import java.util.regex.Matcher;
import static org.apidesign.javadoc.codesnippet.CodeSnippet.sectionName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class SnippetsRegexTest {

    public SnippetsRegexTest() {
    }

    @Test
    public void testRecognizeSnippetTagVariant1() {
        SnippetCollection coll = new SnippetCollection(null);
        String[] code = { null };
        int[] end = { -1 };
        Matcher m = Snippets.matchSnippet((__) -> coll, null, "\n\n" +
" * Text before.\n" +
" * {@snippet :\n" +
" * public static void main(String... args) {\n" +
" * }\n" +
" * }\n" +
" * Text after.\n\n", code, end);
        assertNotNull(m, "Match found");
        assertContains(code[0], "public");
        assertContains(code[0], "static");
        assertContains(code[0], "void");
        assertContains(code[0], "main");
    }

    @Test
    public void testRecognizeSnippetTagVariant2() {
        SnippetCollection coll = new SnippetCollection(null);
        coll.registerSnippet("org/text/Snip.java", "demo", "\n"
                + "public static void main(String... args) {\n"
                + "}\n");
        String[] code = { null };
        int[] end = { -1 };
        Matcher m = Snippets.matchSnippet((__) -> coll, null, "\n\n" +
" * Before file snippet:\n" +
" * {@snippet file=\"org/text/Snip.java\" region=\"demo\"}\n" +
" * After file snippet.\n\n", code, end);
        assertNotNull(m, "Match found");
        assertContains(code[0], "public");
        assertContains(code[0], "static");
        assertContains(code[0], "void");
        assertContains(code[0], "main");
    }

    @Test
    public void matchesEnd() {
        Matcher m = Snippets.END.matcher("// @end");
        assertTrue(m.matches());
        assertEquals(m.groupCount(), 2, "Two groups");
        assertEquals(sectionName(m.group(2)), "", "region name is empty");
    }

    @Test
    public void matchesEndRegion() {
        Matcher m = Snippets.END.matcher("// @end region=\"xyz\"");
        assertTrue(m.matches());
        assertEquals(m.groupCount(), 2, "Two groups");
        assertEquals(sectionName(m.group(2)), "xyz", "region name found");
    }

    private static void assertContains(String txt, String token) {
        int index = txt.indexOf(token);
        if (index >= 0) {
            return;
        }
        fail("Expecting " + token + " in:\n" + txt);
    }
}
