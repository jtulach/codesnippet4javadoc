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

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javadoc.RootDocImpl;
import java.io.File;
import java.util.Locale;
import javax.tools.JavaFileManager;

public final class Profiles {

    private Profiles() {
    }

    public static Profiles read(File file) {
        return new Profiles();
    }

    public int getProfileCount() {
        return 0;
    }

    public int getProfile(String typeNameForProfile) {
        throw new UnsupportedOperationException();
    }

    //
    // extra support JDK9
    //

    public static boolean isFunctionalInterface(RootDoc configurationRoot, ClassDoc classDoc) {
        if (configurationRoot instanceof RootDocImpl) {
            RootDocImpl root = (RootDocImpl) configurationRoot;
            AnnotationDesc[] annotationDescList = classDoc.annotations();
            for (AnnotationDesc annoDesc : annotationDescList) {
                if (root.isFunctionalInterface(annoDesc)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Locale getLocale(RootDoc root) {
        if (root instanceof RootDocImpl)
            return ((RootDocImpl)root).getLocale();
        else
            return Locale.getDefault();
    }

    public static JavaFileManager findFileManager(RootDoc root) {
            if (root instanceof RootDocImpl)
                return ((RootDocImpl) root).getFileManager();
            else
                return new JavacFileManager(new Context(), false, null);
    }

}
