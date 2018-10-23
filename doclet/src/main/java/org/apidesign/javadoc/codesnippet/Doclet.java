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
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.tools.oldlets.formats.html.HtmlDoclet;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * Enhance your own Javadoc with professionally looking
 * <a target="_blank" href="https://github.com/jtulach/codesnippet4javadoc#readme">code snippets</a>.
 * Find out more at the 
 * <a target="_blank" href="https://github.com/jtulach/codesnippet4javadoc">project page</a>.
 */
public final class Doclet {
    private static Snippets snippets;
    private Doclet() {
    }
    public static boolean start(RootDoc root) {
        for (ClassDoc clazz : root.classes()) {
            snippets.fixCodesnippets(root, clazz);
            for (MethodDoc method : clazz.methods()) {
                snippets.fixCodesnippets(clazz, method);
            }
            for (FieldDoc field : clazz.fields()) {
                snippets.fixCodesnippets(clazz, field);
            }
            for (ConstructorDoc con : clazz.constructors()) {
                snippets.fixCodesnippets(clazz, con);
            }
            if (clazz instanceof AnnotationTypeDoc) {
                for (AnnotationTypeElementDoc element : ((AnnotationTypeDoc) clazz).elements()) {
                    snippets.fixCodesnippets(clazz, element);
                }
            }
        }
        for (PackageDoc pkg : root.specifiedPackages()) {
            snippets.fixCodesnippets(root, pkg);
        }
        RootDoc rootProxy = hideElements(RootDoc.class, root);
        return HtmlDoclet.start(rootProxy);
    }

    public static int optionLength(String option) {
        if (option.equals("-snippetpath")) {
            return 2;
        }
        if (option.equals("-snippetclasses")) {
            return 2;
        }
        if (option.equals("-maxLineLength")) {
            return 2;
        }
        if (option.equals("-verifysincepresent")) {
            return 1;
        }
        if (option.equals("-verifysince")) {
            return 2;
        }
        if (option.equals("-hiddingannotation")) {
            return 2;
        }
        return HtmlDoclet.optionLength(option);
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter) {
        snippets = new Snippets(reporter);
        for (String[] optionAndParams : options) {
            Boolean visible = null;
            if (optionAndParams[0].equals("-sourcepath")) {
                visible = true;
            }
            if (optionAndParams[0].equals("-snippetpath")) {
                visible = false;
            }
            if (visible != null) {
                for (int i = 1; i < optionAndParams.length; i++) {
                    for (String elem : optionAndParams[i].split(File.pathSeparator)) {
                        snippets.addPath(findAbsolutePath(elem), visible);
                    }
                }
            }
            if (optionAndParams[0].equals("-snippetclasses")) {
                for (int i = 1; i < optionAndParams.length; i++) {
                    snippets.addClasses(optionAndParams[i]);
                }
            }
            if (optionAndParams[0].equals("-maxLineLength")) {
                if ( optionAndParams.length > 1 ) {
                    snippets.setMaxLineLength( optionAndParams[1] );
                }
            }
            if (
                optionAndParams[0].equals("-verifysincepresent") ||
                optionAndParams[0].equals("-verifysince")
            ) {
                if ( optionAndParams.length > 1 ) {
                    snippets.setVerifySince(optionAndParams[1]);
                } else {
                    snippets.setVerifySince("");
                }
            }
            if (
                optionAndParams[0].equals("-hiddingannotation")
            ) {
                snippets.addHiddenAnnotation(optionAndParams[1]);
            }
            if (
                optionAndParams[0].equals("-encoding")
            ) {
                snippets.setEncoding(optionAndParams[1]);
            }
        }
        return HtmlDoclet.validOptions(options, reporter);
    }

    private static Path findAbsolutePath(String elem) {
        File file = new File(elem);
        if (file.isAbsolute()) {
            return file.toPath();
        } else {
            File root = new File(".").getAbsoluteFile();
            while (!file.exists() && root != null) {
                file = new File(root, elem);
                root = root.getParentFile();
            }
            return file.getAbsoluteFile().toPath();
        }

    }

    public static LanguageVersion languageVersion() {
        return HtmlDoclet.languageVersion();
    }

    private static <T> T hideElement(Class<T> clazz, final Object obj) {
        return hideElements(clazz, clazz.cast(obj));
    }

    private static <T> T hideElements(Class<T> clazz, final T obj) {
        if (!toBeHiddenInterface(clazz)) {
            return obj;
        }
        Class<?> c = clazz;
        if (clazz.isAssignableFrom(ClassDoc.class)) {
            if (obj instanceof ClassDoc && ((ClassDoc) obj).isAnnotationType()) {
                c = AnnotationTypeDoc.class;
            }
        }
        if (clazz.isAssignableFrom(SeeTag.class)) {
            if (obj instanceof SeeTag) {
                c = SeeTag.class;
            }
        }
        InvocationHandler h = new DocProxy(obj);
        return clazz.cast(Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[]{c}, h));
    }

    private static boolean toBeHiddenInterface(final Class<?> type) {
        if (type == null) {
            return false;
        }
        if (type.getPackage() == RootDoc.class.getPackage()) {
            return true;
        }
        for (Class<?> interfce : type.getInterfaces()) {
            if (toBeHiddenInterface(interfce)) {
                return true;
            }
        }
        if (toBeHiddenInterface(type.getSuperclass())) {
            return true;
        }
        return false;
    }

    private static class DocProxy<T> implements InvocationHandler, Callable<T> {
        private final T obj;

        public DocProxy(T obj) {
            this.obj = obj;
        }

        @Override
        public T call() {
            return obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        continue;
                    }
                    InvocationHandler handler = null;
                    try {
                        handler = Proxy.getInvocationHandler(args[i]);
                    } catch (IllegalArgumentException ignore) {
                        continue;
                    }
                    if (handler instanceof DocProxy) {
                        args[i] = ((DocProxy)handler).obj;
                    }
                }
            }

            boolean doSkip = true;
            if (method.getName().equals("allClasses")) {
                doSkip = false;
            }

            Object ret = method.invoke(obj, args);
            final Class<?> requestedType = method.getReturnType();
            if (requestedType.isArray()) {
                final Class<?> componentType = requestedType.getComponentType();
                if (toBeHiddenInterface(componentType)) {
                    Object[] arr = (Object[]) ret;
                    List<Object> copy = new ArrayList<>();
                    for (Object element : arr) {
                        boolean skip = false;
                        for (String name : findAnnotationsNames(element)) {
                            if (snippets.isHiddingAnnotation(name)) {
                                skip = doSkip;
                                break;
                            }
                        }
                        if (!skip) {
                            copy.add(hideElement(componentType, element));
                        }
                    }
                    Object[] reqArr = (Object[])Array.newInstance(requestedType.getComponentType(), 0);
                    return copy.toArray(reqArr);
                }
            }
            if (ret instanceof Object && toBeHiddenInterface(ret.getClass())) {
                ret = hideElement(ret.getClass().getInterfaces()[0], ret);
            }
            return ret;
        }

        private AnnotationDesc[] findAnnotations(Object element) {
            if (element instanceof ProgramElementDoc) {
                ProgramElementDoc ped = (ProgramElementDoc) element;
                return ped.annotations();
            }
            if (element instanceof PackageDoc) {
                return ((PackageDoc) element).annotations();
            }
            return new AnnotationDesc[0];
        }

        private Iterable<String> findAnnotationsNames(Object element) {
            Set<String> names = new TreeSet<>();
            for (AnnotationDesc desc : findAnnotations(element)) {
                try {
                    String name = desc.annotationType().qualifiedName();
                    names.add(name);
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
            return names;
        }
    }
}
