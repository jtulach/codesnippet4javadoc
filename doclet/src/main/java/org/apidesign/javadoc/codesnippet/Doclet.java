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

import com.sun.tools.oldlets.javadoc.AnnotationDesc;
import com.sun.tools.oldlets.javadoc.AnnotationTypeDoc;
import com.sun.tools.oldlets.javadoc.AnnotationTypeElementDoc;
import com.sun.tools.oldlets.javadoc.ClassDoc;
import com.sun.tools.oldlets.javadoc.ConstructorDoc;
import com.sun.tools.oldlets.javadoc.DocErrorReporter;
import com.sun.tools.oldlets.javadoc.FieldDoc;
import com.sun.tools.oldlets.javadoc.LanguageVersion;
import com.sun.tools.oldlets.javadoc.MethodDoc;
import com.sun.tools.oldlets.javadoc.PackageDoc;
import com.sun.tools.oldlets.javadoc.ProgramElementDoc;
import com.sun.tools.oldlets.javadoc.RootDoc;
import com.sun.tools.oldlets.javadoc.SeeTag;
import com.sun.tools.oldlets.formats.html.HtmlDoclet;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import javax.lang.model.SourceVersion;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * Enhance your own Javadoc with professionally looking
 * <a target="_blank" href="https://github.com/jtulach/codesnippet4javadoc#readme">code snippets</a>.
 * Find out more at the
 * <a target="_blank" href="https://github.com/jtulach/codesnippet4javadoc">project page</a>.
 */
public final class Doclet implements jdk.javadoc.doclet.Doclet {
    private static Snippets snippets;
    private Locale locale;
    private Reporter reporter;
    public Doclet() {
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

    enum SnippetOption implements jdk.javadoc.doclet.Doclet.Option {
        AUTHOR(1, "-author"),
        BOTTOM(2, "-bottom"),
        CHARSET(2, "-charset"),
        D(2, "-d"),
        DOCENCODING(2, "-docencoding"),
        DOCTITLE(2, "-doctitle"),
        WINDOWTITLE(2, "-windowtitle"),
        LINKOFFLINE(3, "-linkoffline"),
        USE(1, "-use"),
        VERSION(1, "-version"),

        SOURCEPATH(2, "-sourcepath"),
        SNIPPETPATH(2, "-snippetpath"),
        SNIPPETCLASSES(2, "-snippetclasses"),
        MAXLINELENGTH(2, "-maxLineLength"),
        HIDINGANNOTATION(2, "-hiddingannotation"),
        VERIFYSINCE(1, "-verifysince"),
        VERIFYSINCEPRESENT(1, "-verifysincepresent"),
        ENCODING(2, "-encoding");

        final int length;
        final String name;

        private SnippetOption(int length, String name) {
            this.length = length;
            this.name = name;
        }

        public boolean matches(String option) {
            return option.equals(name);
        }

        public int getArgumentCount() {
            return length - 1;
        }

        public String getDescription() {
            return null;
        }

        public jdk.javadoc.doclet.Doclet.Option.Kind getKind() {
            return jdk.javadoc.doclet.Doclet.Option.Kind.STANDARD;
        }

        public List<String> getNames() {
            return Arrays.asList(name);
        }

        public String getParameters() {
            return null;
        }

        public boolean process(String option, List<String> arguments) {
            ArrayList<String> all = new ArrayList<>();
            all.add(name);
            all.addAll(arguments);
            return validOptions(new String[][] { all.toArray(new String[0]) }, null);
        }
    }

    public static int optionLength(String option) {
        if (SnippetOption.SNIPPETPATH.matches(option)) {
            return 2;
        }
        if (SnippetOption.SNIPPETCLASSES.matches(option)) {
            return 2;
        }
        if (SnippetOption.MAXLINELENGTH.matches(option)) {
            return 2;
        }
        if (SnippetOption.VERIFYSINCEPRESENT.matches(option)) {
            return 1;
        }
        if (SnippetOption.VERIFYSINCE.matches(option)) {
            return 2;
        }
        if (SnippetOption.HIDINGANNOTATION.matches(option)) {
            return 2;
        }
        return HtmlDoclet.optionLength(option);
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter) {
        if (snippets == null) {
            snippets = new Snippets(reporter);
        }
        for (String[] optionAndParams : options) {
            Boolean visible = null;
            if (SnippetOption.SOURCEPATH.matches(optionAndParams[0])) {
                visible = true;
            }
            if (SnippetOption.SNIPPETPATH.matches(optionAndParams[0])) {
                visible = false;
            }
            if (visible != null) {
                for (int i = 1; i < optionAndParams.length; i++) {
                    for (String elem : optionAndParams[i].split(File.pathSeparator)) {
                        snippets.addPath(findAbsolutePath(elem), visible);
                    }
                }
            }
            if (SnippetOption.SNIPPETCLASSES.matches(optionAndParams[0])) {
                for (int i = 1; i < optionAndParams.length; i++) {
                    snippets.addClasses(optionAndParams[i]);
                }
            }
            if (SnippetOption.MAXLINELENGTH.matches(optionAndParams[0])) {
                if ( optionAndParams.length > 1 ) {
                    snippets.setMaxLineLength( optionAndParams[1] );
                }
            }
            if (
                SnippetOption.VERIFYSINCEPRESENT.matches(optionAndParams[0]) ||
                SnippetOption.VERIFYSINCE.matches(optionAndParams[0])
            ) {
                if ( optionAndParams.length > 1 ) {
                    snippets.setVerifySince(optionAndParams[1]);
                } else {
                    snippets.setVerifySince("");
                }
            }
            if (
                SnippetOption.HIDINGANNOTATION.matches(optionAndParams[0])
            ) {
                snippets.addHiddenAnnotation(optionAndParams[1]);
            }
            if (
                SnippetOption.ENCODING.matches(optionAndParams[0])
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

    public void init(Locale locale, Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;
    }

    public String getName() {
        return "CodesnippetDoclet";
    }

    public Set<? extends jdk.javadoc.doclet.Doclet.Option> getSupportedOptions() {
        return EnumSet.allOf(SnippetOption.class);
    }

    public SourceVersion getSupportedSourceVersion() {
        throw new UnsupportedOperationException();
    }

    public boolean run(DocletEnvironment environment) {
        throw new UnsupportedOperationException();
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
