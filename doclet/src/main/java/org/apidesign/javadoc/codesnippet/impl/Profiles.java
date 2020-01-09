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
package org.apidesign.javadoc.codesnippet.impl;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SourcePosition;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.Context;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.concurrent.Callable;
import javax.tools.FileObject;
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
        Object r = Proxy.getInvocationHandler(configurationRoot);
        Class<?> c = r.getClass();
        while (c != null) {
            if (c.getSimpleName().equals("RootDocImpl")) {
                try {
                    Method isFunctionalInterface = c.getMethod("isFunctionalInterface", AnnotationDesc.class);
                    AnnotationDesc[] annotationDescList = classDoc.annotations();
                    for (AnnotationDesc annoDesc : annotationDescList) {
                        if (Boolean.TRUE.equals(isFunctionalInterface.invoke(r, annoDesc))) {
                            return true;
                        }
                    }
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            c = c.getSuperclass();
        }
        return false;
    }

    public static Locale getLocale(RootDoc configurationRoot) {
        Object r = Proxy.getInvocationHandler(configurationRoot);
        Class<?> c = r.getClass();
        while (c != null) {
            if (c.getSimpleName().equals("RootDocImpl")) {
                try {
                    Method m = c.getMethod("getLocale");
                    return (Locale) m.invoke(r);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            c = c.getSuperclass();
        }
        return Locale.getDefault();
    }

    public static JavaFileManager findFileManager(RootDoc root) {
        try {
            Object obj;
            if (Proxy.isProxyClass(root.getClass())) {
                InvocationHandler handler = Proxy.getInvocationHandler(root);
                Callable<Object> callable = (Callable<Object>) handler;
                obj = callable.call();
            } else {
                obj = root;
            }
            Object fm = obj.getClass().getMethod("getFileManager").invoke(obj);
            return (JavaFileManager) fm;
        } catch (IllegalAccessException ex) {
            // OK
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return new JavacFileManager(new Context(), false, null);
    }

    public static FileObject[] findFileObject(SourcePosition obj) {
        Class<?> c = obj.getClass();
        while (c != null) {
            if (c.getSimpleName().equals("SourcePositionImpl")) {
                try {
                    Method m = c.getMethod("fileObject");
                    return new FileObject[]{(FileObject) m.invoke(obj)};
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

}
