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
import java.nio.file.Paths;
import org.testng.annotations.Test;

// BEGIN: sampleClass
public class VerifyJavadocTest {
    public VerifyJavadocTest() {
    }
// FINISH: sampleClass

    @Test
    public void docFilesCopied() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/doc-files/addon.js");
        assertNotNull(url, "doc-files file found");
    }

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
    public void deprecatedMethodCanBeHidden() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/SampleClass.html");
        assertNotNull(url, "Generated page found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));

        assertTrue(text.contains("showUseOfAnnotation"), "This method is found");
        assertEquals(text.indexOf("hiddenMethod"), -1, "No hiddenMethod() found");
    }

    @Test
    public void deprecatedClassCanBeHiddenFromPackageList() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        {
            URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/DeprecatedClass.html");
            assertNotNull(url, "page generated for DeprecatedClass.html");

            File file = new File(url.toURI());
            assertTrue(file.exists(), "File found " + file);
            String text = new String(Files.readAllBytes(file.toPath()));

            assertTrue(text.contains("initialize"), "Method initialize() found");
        }

        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/package-summary.html");
        assertNotNull(url, "package summary found as well");

        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));

        assertTrue(text.contains("SampleClass"), "This callsis found");
        assertEquals(text.indexOf("DeprecatedClass"), -1, "No DeprecatedClass found");
    }

    @Test
    public void deprecatedParametricClassCanBeHiddenFromPackageList() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        {
            URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/DeprecatedParametricClass.html");
            assertNotNull(url, "page generated for DeprecatedParametricClass.html");

            File file = new File(url.toURI());
            assertTrue(file.exists(), "File found " + file);
            String text = new String(Files.readAllBytes(file.toPath()));

            assertTrue(text.contains("initialize"), "Method initialize() found");
        }

        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/package-summary.html");
        assertNotNull(url, "package summary found as well");

        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));

        assertTrue(text.contains("SampleClass"), "This callsis found");
        assertEquals(text.indexOf("DeprecatedParametricClass"), -1, "No DeprecatedParametricClass found");
    }

    @Test
    public void hidingClassWithOwnAnnotation() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        {
            URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/HiddenSnippets.html");
            assertNotNull(url, "page generated for HiddenSnippets.html");
        }

        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/package-summary.html");
        assertNotNull(url, "package summary found as well");

        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);
        String text = new String(Files.readAllBytes(file.toPath()));

        assertTrue(text.contains("SampleClass"), "This callsis found");
        assertEquals(text.indexOf("HiddenSnippets"), -1, "No HiddenSnippets found");
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

        assertEquals(text.indexOf("Description copied from class"), -1, "Descriptions shouldn't be copied");
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

        assertNotEquals(text.indexOf("SampleClass"), -1, "Contains {@link SampleClass}");
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

    @Test
    public void testAssertPackageInfoSnippet() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/package-summary.html");
        assertNotNull(url, "Generated page for package found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        byte[] data = Files.readAllBytes(file.toPath());
        String text = new String(data);
        assertSnippet(text, "sample1", "<b>int</b> x = 42;");
    }

    @Test
    public void testSnippetOnAnnotationMethod() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/SampleAnno.html");
        assertNotNull(url, "Generated page for package found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        byte[] data = Files.readAllBytes(file.toPath());
        String text = new String(data);
        assertSnippet(text, "sample1", "<b>int</b> x = 42;");
    }

    @Test
    public void testSnippetWithSpecialCharacters() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/SpecialCharacters.html");
        assertNotNull(url, "Generated page for package found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        byte[] data = Files.readAllBytes(file.toPath());
        String text = new String(data);
        assertSnippet(text, "dollarSnippet1", "MY_MONEY = <em>\"$100.00\"</em>");
        assertSnippet(text, "dollarSnippet2", "MONEY_SIGNS = <em>\"$ € £\"</em>");
    }

    @Test
    public void testPackageListElementListExists() {
        String packageListFileName = "javadoc-bundle-options/package-list";
        int javaSpecVersion;
        String javaSpecVersionProperty = System.getProperty("java.specification.version");
        try {
            // JDK 9 and above have properties set as "9", "10"...
            // JDK 8 and below have this property set as "1.8", "1.7"
            // https://openjdk.java.net/jeps/223
            javaSpecVersion = Integer.parseInt(javaSpecVersionProperty);
        } catch (NumberFormatException ex) {
            javaSpecVersion = Integer
                .parseInt(javaSpecVersionProperty.substring(javaSpecVersionProperty.indexOf('.') + 1));
        }
        if (javaSpecVersion >= 10) {
            packageListFileName = "javadoc-bundle-options/element-list";
        }
        File file = Paths.get("target", packageListFileName).toFile();
        assertTrue(file.exists(), "File found " + file);
    }

    @Test
    public void testElementListLinks() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/org/apidesign/javadoc/testing/UseFlux.html");
        assertNotNull(url, "Generated page for package found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        byte[] data = Files.readAllBytes(file.toPath());
        String text = new String(data);
        assertNotEquals(text.indexOf("/Flux.html"), -1, "Checks for existence of link to Flux");
        assertNotEquals(text.indexOf("/Integer.html"), -1, "Checks for existence of link to Integer");
    }

    @Test
    public void testPackageGroups() throws Exception {
        ClassLoader l = VerifyJavadocTest.class.getClassLoader();
        URL url = l.getResource("apidocs/overview-summary.html");
        assertNotNull(url, "Generated page for package found");
        File file = new File(url.toURI());
        assertTrue(file.exists(), "File found " + file);

        byte[] data = Files.readAllBytes(file.toPath());
        String text = new String(data);
        assertNotEquals(text.indexOf("Service Package"), -1, "Second group found");
        assertNotEquals(text.indexOf("Basic Package"), -1, "First group found");
    }

    private void assertSnippet(String text, final String snippetKey, final String snippetText) {
        int from = 0;
        for (;;) {
            int start = text.indexOf("<pre class='snippet'>", from);
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
