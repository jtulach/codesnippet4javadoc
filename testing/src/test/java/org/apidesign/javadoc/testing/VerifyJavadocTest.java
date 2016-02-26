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

import java.io.File;
import static org.testng.Assert.*;

import java.net.URL;
import java.nio.file.Files;
import org.testng.annotations.Test;

// BEGIN: sampleClass
public class VerifyJavadocTest {
    public VerifyJavadocTest() {
    }
// FINISH: sampleClass

    @Test
    public void testSnippetInMainClassFound() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));
        assertEquals(text.indexOf("codesnippet"), -1, "No code snippet text found");

        assertSnippet(text, "sample1", "<b>int</b> x = 42;");
    }

    @Test
    public void testSnippetFromTest() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        // BEGIN: read.in.test
        byte[] data = Files.readAllBytes(file.toPath());
        // END: read.in.test
        String text = new String(data);
        assertEquals(text.indexOf("codesnippet"), -1, "No code snippet text found");

        assertSnippet(text, "read.in.test", "readAllBytes(file.toPath());");
        assertSnippet(text, "read.in.test", "java.nio.file\"><code>Files</code></a>");
    }

    @Test
    public void testSnippetSecondaryFileAsALink() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/EmbeddingSampleCode.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        byte[] data = Files.readAllBytes(file.toPath());
        String text = new String(data);
        assertSnippet(text, "EmbeddedSnippet#fourtyTwo", "mul(6, 7);");
    }

    @Test
    public void testLinkForAnnotation() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        byte[] data = Files.readAllBytes(file.toPath());
        String text = new String(data);
        assertEquals(text.indexOf("codesnippet"), -1, "No code snippet text found");

        assertSnippet(text, "ANNO", "SampleAnno");
        assertSnippet(text, "ANNO", "javadoc.testing\"><code>SampleAnno</code></a>");
    }

    @Test
    public void testDontLinkToImplClasses() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        byte[] data = Files.readAllBytes(file.toPath());
        String text = new String(data);
        assertEquals(text.indexOf("org.apidesign.javadoc.testing.VerifyJavadocTest"), -1, "FQN reference to VerifyJavadocTest shouldn't be found");
        assertNotEquals(text.indexOf("VerifyJavadocTest"), -1, "Simple name reference to VerifyJavadocTest shouldn't be found");
    }

    private void assertSnippet(String text, final String snippetKey, final String snippetText) {
        int from = 0;
        for (;;) {
            int start = text.indexOf("<pre>", from);
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

}
