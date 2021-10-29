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

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.SourcePosition;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.Reporter;

final class DelegatingDocErrorReporter implements DocErrorReporter {

    private final Reporter reporter;

    public DelegatingDocErrorReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public void printError(String msg) {
        reporter.print(Diagnostic.Kind.ERROR, msg);
    }

    @Override
    public void printError(SourcePosition pos, String msg) {
        reporter.print(Diagnostic.Kind.ERROR, msg);
    }

    @Override
    public void printWarning(String msg) {
        reporter.print(Diagnostic.Kind.WARNING, msg);
    }

    @Override
    public void printWarning(SourcePosition pos, String msg) {
        reporter.print(Diagnostic.Kind.WARNING, msg);
    }

    @Override
    public void printNotice(String msg) {
        notice(reporter, msg);
    }

    @Override
    public void printNotice(SourcePosition pos, String msg) {
        notice(reporter, msg);
    }

    private static void notice(Reporter r, String m) {
        try {
            final Method method = Reporter.class.getMethod("getStandardWriter");
            PrintWriter w = (PrintWriter) method.invoke(r);
            if (w != null) {
                w.println(m);
                return;
            }
        } catch (ClassCastException | ReflectiveOperationException | SecurityException ex) {
            // fall thru
        }
        r.print(Diagnostic.Kind.NOTE, m);
    }
}
