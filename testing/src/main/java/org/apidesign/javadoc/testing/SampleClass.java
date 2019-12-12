/**
 * Codesnippet Javadoc Doclet
 * Copyright (C) 2015-2018 Jaroslav Tulach - jaroslav.tulach@apidesign.org
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
 * Rest of the text. With sample code in with a test class:
 * {@codesnippet sampleClass}
 * And we are done.
 */
public class SampleClass {
    // BEGIN: annotation.link
    @SampleAnno(reference = SampleClass.class)
    private SampleClass() {
    }
    // END: annotation.link

    private static void sample1() {
        // BEGIN: sample1
        int x = 42;
        // END: sample1
    }

    /** Show snippet from test code. Here it is:
     * {@codesnippet read.in.test}
     * End of snippet and here goes another one:
     * {@codesnippet sample1}
     * Great, we are done.
     * @throws org.apidesign.javadoc.testing.RealException
     */
    public static void initialize() throws RealException {
    }

    /**
     * This is how you apply an annotation. ANNOBEG:
     * {@codesnippet annotation.link}
     * ANNOEND found.
     */
    public static void showUseOfAnnotation() {
    }

    /**
     * This method Javadoc from distribution OpenJDK complains:
     * <p>
     * warning - Parameter "value" is documented more than once.
     * <p>
     * The OpenJDK from AdoptOpenJDK has a different warning:
     * <p>
     * warning: no description for <code>@param<code>
     * @param value
     */
    public void distributionJavaComplains(String value) {
    }

    @Deprecated
    public static void hiddenMethod() {
    }

    /** Snippet on an inner class:
     * {@codesnippet read.in.test}
     * End of the code.
     */
    public static abstract class Inner {
        /** Method on a innerclass with code snippet:
         * {@codesnippet read.in.test}
         */
        protected abstract void documented();
    }
}
