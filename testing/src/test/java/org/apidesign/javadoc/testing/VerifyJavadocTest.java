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
import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class VerifyJavadocTest {

    public VerifyJavadocTest() {
    }

    @Test
    public void testSomeMethod() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = Files.readFile(file);
        assertEquals(text.indexOf("codesnippet"), -1, "No code snippet text found");

        int start = text.indexOf("<pre>");
        assertTrue(start >= 0, "<pre> found in " + text);
        int textIndex = text.indexOf("sample1", start);
        assertEquals(textIndex, -1, "sample1 code not found @ " + (textIndex - start));
        textIndex = text.indexOf("int x = 42;", start);
        assertTrue(textIndex >= start, "text found in " + text);
        int end = text.indexOf("</pre>", textIndex);
        assertTrue(end >= start, "</pre> found in " + text);
    }

}
