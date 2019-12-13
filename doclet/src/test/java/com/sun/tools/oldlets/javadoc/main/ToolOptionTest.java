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
package com.sun.tools.oldlets.javadoc.main;

import com.sun.tools.javac.util.Context;
import com.sun.tools.oldlets.javadoc.main.ToolOption.Helper;
import java.lang.reflect.InvocationTargetException;
import org.testng.annotations.Test;

public class ToolOptionTest {

    public ToolOptionTest() {
    }

    @Test
    public void processOptionTest() {
        Helper helper = new Start(new Context());

        try {
            ToolOption.ADD_OPENS.process(helper, "xyz");
        } catch (IllegalStateException ex) {
            if (ex.getCause() instanceof InvocationTargetException) {
                if (ex.getCause().getCause() instanceof NullPointerException) {
                    // this is OK
                    return;
                }
            }
            throw ex;
        }
    }

}
