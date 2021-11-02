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

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

public class VerifyJdkTest extends VerifyJavadocBase {
    @BeforeClass
    public static void verifyJDK18Plus() {
        String v = System.getProperty("java.version");
        if (v == null || v.startsWith("1.8")) {
            throw new SkipException("Too old JDK: " + v);
        }
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(v);
        if (!m.find() || Integer.parseInt(m.group()) < 18) {
            throw new SkipException("Too old JDK: " + v);
        }
    }

    @Override
    protected URL loadResource(String path) {
        ClassLoader l = VerifyJavadocBase.class.getClassLoader();
        return l.getResource("jdk/" + path);
    }
}
