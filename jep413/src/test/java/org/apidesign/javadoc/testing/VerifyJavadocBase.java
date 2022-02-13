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
package org.apidesign.javadoc.testing;

import java.io.File;
import static org.testng.Assert.*;

import java.net.URL;
import java.nio.file.Files;
import org.testng.annotations.Test;

public abstract class VerifyJavadocBase {
    protected VerifyJavadocBase() {
    }

    @Test
    public void testSnippetInMainClassFound() throws Exception {
        URL url = loadResource("apidocs/org/apidesign/javadoc/jep413/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));
        assertEquals(text.indexOf("codesnippet"), -1, "No code snippet text found");

        assertSnippet(text, "sample1", "public");
        assertSnippet(text, "sample1", "static");
        assertSnippet(text, "sample1", "void");
        assertSnippet(text, "sample1", "main");
        assertSnippet(text, "sample1", "String");
        assertSnippet(text, "sample1", "args");

        assertContains(text, "Text before.");
        assertContains(text, "Text after.");
    }

    @Test
    public void testExternalSnippetFound() throws Exception {
        URL url = loadResource("apidocs/org/apidesign/javadoc/jep413/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));
        assertEquals(text.indexOf("@snippet"), -1, "No code snippet text found");

        assertSnippet(text, "demo", "SampleClass");
        assertSnippet(text, "demo", "show");
        assertSnippet(text, "demo", "text");

        assertContains(text, "Before file snippet");
        assertContains(text, "After file snippet");
    }

    @Test
    public void testExternalEndlessSnippetFound() throws Exception {
        URL url = loadResource("apidocs/org/apidesign/javadoc/jep413/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));
        assertEquals(text.indexOf("@snippet"), -1, "No code snippet text found");

        assertSnippet(text, "endless", "simpleend");
        assertSnippet(text, "endless", "noEnd:");

        assertContains(text, "Start snippet:");
        assertContains(text, "End snippet.");
    }

    @Test
    public void testDemoMainSnippet() throws Exception {
        URL url = loadResource("apidocs/org/apidesign/javadoc/demo/MainMethodContent.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));
        assertEquals(text.indexOf("@snippet"), -1, "No code snippet text found");

        assertSnippet(text, "demo", "main");
        assertSnippet(text, "demo", "String");
        assertSnippet(text, "demo", "println");

        assertContains(text, "showing content of");
        assertContains(text, "snippet is extracted");
    }
    
    private void assertSnippet(String text, final String snippetKey, final String snippetText) {
        int from = 0;
        for (;;) {
            int start = text.indexOf("<pre", from);
            assertTrue(start >= 0, snippetText + " found in " + text + " from " + from);
            int end = text.indexOf("</pre>", start);
            assertTrue(end >= start, "</pre> found in " + text);

            String snippet = text.substring(start, end);
            int textIndex = snippet.indexOf(snippetKey);
            assertEquals(textIndex, -1, snippetKey + " code not found @ " + (textIndex - start));

            textIndex = snippet.indexOf(snippetText);
            if (textIndex == -1) {
                from = end;
                continue;
            }
            return;
        }
    }

    protected abstract URL loadResource(String path);

    private static void assertContains(String text, String search) {
        int at = text.indexOf(search);
        if (at >= 0) {
            return;
        }
        fail("Expecting " + search + " in\n" + text);
    }
}
