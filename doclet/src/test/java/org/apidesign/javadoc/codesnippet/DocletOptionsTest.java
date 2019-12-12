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
package org.apidesign.javadoc.codesnippet;

import java.util.List;
import java.util.Set;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.Doclet.Option;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DocletOptionsTest {
    private Doclet standardDoclet;

    public DocletOptionsTest() {
    }

    @BeforeMethod
    public void initStandardDoclet() throws Exception {
        try {
            standardDoclet = (jdk.javadoc.doclet.Doclet) Class.forName("jdk.javadoc.doclet.StandardDoclet").newInstance();
        } catch (ClassNotFoundException ex) {
            if (System.getProperty("java.version").startsWith("1.8")) {
                throw new SkipException("Can only run on JDK9+");
            }
            throw ex;
        }
    }

    @Test
    public void testGetSupportedOptions() {
        Doclet instance = new org.apidesign.javadoc.codesnippet.Doclet();
        Set<? extends Doclet.Option> expResult = standardDoclet.getSupportedOptions();
        Set<? extends Doclet.Option> result = instance.getSupportedOptions();
        assertOptions(expResult, result);
    }

    private static void assertOptions(
        Set<? extends jdk.javadoc.doclet.Doclet.Option> exp,
        Set<? extends jdk.javadoc.doclet.Doclet.Option> result
    ) {
        StringBuilder err = new StringBuilder();
        int missing = 0;
        for (jdk.javadoc.doclet.Doclet.Option o : exp) {
            jdk.javadoc.doclet.Doclet.Option found = findOption(o.getNames(), result, err);
            if (found == null) {
                missing++;
            }
        }
        if (missing > 0) {
            for (Option o : result) {
                err.append("\n  known ").append(o).append(" with ").append(o.getNames());
            }
            Assert.assertEquals(missing, 0, err.toString());
        }
    }

    private static jdk.javadoc.doclet.Doclet.Option findOption(
        List<String> names, Set<? extends jdk.javadoc.doclet.Doclet.Option> set,
        StringBuilder err
    ) {
        for (jdk.javadoc.doclet.Doclet.Option o : set) {
            for (String n : o.getNames()) {
                if (names.contains(n)) {
                    return o;
                }
            }
        }
        err.append("\nCannot find option named ").append(names);
        return null;
    }
}
