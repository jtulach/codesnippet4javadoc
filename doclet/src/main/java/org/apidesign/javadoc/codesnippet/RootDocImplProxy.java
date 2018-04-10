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
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.tools.javac.util.List;
import com.sun.tools.javadoc.DocEnv;
import com.sun.tools.javadoc.RootDocImpl;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.tools.JavaFileManager;

final class RootDocImplProxy extends RootDocImpl {
    private final Map<String,java.util.List<Method>> methods;
    private final InvocationHandler delegate;

    RootDocImplProxy(DocEnv env, InvocationHandler delegate) {
        super(env, List.<String>nil(), List.<String[]>nil());
        this.delegate = delegate;
        this.methods = new HashMap<>();
        for (Method m : RootDocImpl.class.getMethods()) {
            if (m.getDeclaringClass() == Object.class) {
                continue;
            }
            m.setAccessible(true);

            java.util.List<Method> arr = methods.get(m.getName());
            if (arr == null) {
                arr = new ArrayList<>();
                methods.put(m.getName(), arr);
            }

            final int index = m.getParameterTypes().length;
            while (arr.size() <= index) {
                arr.add(null);
            }
            
            Method prev = arr.set(index, m);
            if (prev != null) {
                throw new IllegalStateException("Two methods with the same name: " + m + " and " + prev);
            }
        }
    }

    private <R> R invoke(String method, Object... args) {
        Method m = methods.get(method).get(args.length);
        assert m != null;
        try {
            return (R) delegate.invoke(this, m, args);
        } catch (Throwable ex) {
            throw raise(RuntimeException.class, ex);
        }
    }

    public boolean showTagMessages() {
        return invoke("showTagMessages");
    }

    public boolean isFunctionalInterface(AnnotationDesc ad) {
        return invoke("isFunctionalInterface", ad);
    }

    public void initDocLint(Collection<String> clctn, Collection<String> clctn1) {
        invoke("initDocLint", clctn, clctn1);
    }

    public JavaFileManager getFileManager() {
        return invoke("getFileManager");
    }

    public Locale getLocale() {
        return invoke("getLocale");
    }

    public SourcePosition position() {
        return invoke("position");
    }

    @Override
    protected String documentation() {
        return invoke("documentation");
    }

    @Override
    public void printNotice(SourcePosition sp, String string) {
        invoke("printNotice", sp, string);
    }

    @Override
    public void printNotice(String string) {
        invoke("printNotice", string);
    }

    @Override
    public void printWarning(SourcePosition sp, String string) {
        invoke("printWarning", sp, string);
    }

    @Override
    public void printWarning(String string) {
        invoke("printWarning", string);
    }

    @Override
    public void printError(SourcePosition sp, String string) {
        invoke("printError", sp, string);
    }

    @Override
    public void printError(String string) {
        invoke("printError", string);
    }

    @Override
    public boolean isIncluded() {
        return invoke("isIncluded");
    }

    @Override
    public String qualifiedName() {
        return invoke("qualifiedName");
    }

    @Override
    public String name() {
        return invoke("name");
    }

    @Override
    public PackageDoc packageNamed(String string) {
        return invoke("packageNamed", string);
    }

    @Override
    public ClassDoc classNamed(String string) {
        return invoke("classNamed", string);
    }

    @Override
    public ClassDoc[] classes() {
        return invoke("classes");
    }

    @Override
    public ClassDoc[] specifiedClasses() {
        return invoke("specifiedClasses");
    }

    @Override
    public PackageDoc[] specifiedPackages() {
        return invoke("specifiedPackages");
    }

    @Override
    public String[][] options() {
        return invoke("options");
    }

    @Override
    public boolean isClass() {
        return invoke("isClass");
    }

    @Override
    public boolean isOrdinaryClass() {
        return invoke("isOrdinaryClass");
    }

    @Override
    public boolean isAnnotationType() {
        return invoke("isAnnotationType");
    }

    @Override
    public boolean isEnum() {
        return invoke("isEnum");
    }

    @Override
    public boolean isError() {
        return invoke("isError");
    }

    @Override
    public boolean isException() {
        return invoke("isException");
    }

    @Override
    public boolean isInterface() {
        return invoke("isInterface");
    }

    @Override
    public boolean isAnnotationTypeElement() {
        return invoke("isAnnotationTypeElement");
    }

    @Override
    public boolean isMethod() {
        return invoke("isMethod");
    }

    @Override
    public boolean isConstructor() {
        return invoke("isConstructor");
    }

    @Override
    public boolean isEnumConstant() {
        return invoke("isEnumConstant");
    }

    @Override
    public boolean isField() {
        return invoke("isField");
    }

    @Override
    public int compareTo(Object o) {
        return invoke("compareTo", o);
    }

    @Override
    public String toString() {
        return invoke("toString");
    }

    @Override
    public void setRawCommentText(String string) {
        invoke("setRawCommentText", string);
    }

    @Override
    public String getRawCommentText() {
        return invoke("getRawCommentText");
    }

    @Override
    public Tag[] firstSentenceTags() {
        return invoke("firstSentenceTags");
    }

    @Override
    public Tag[] inlineTags() {
        return invoke("inlineTags");
    }

    @Override
    public SeeTag[] seeTags() {
        return invoke("seeTags");
    }

    @Override
    public Tag[] tags(String string) {
        return invoke("tags", string);
    }

    @Override
    public Tag[] tags() {
        return invoke("tags");
    }

    @Override
    public String commentText() {
        return invoke("commentText");
    }

    @Override
    public boolean equals(Object obj) {
        return invoke("equals", obj);
    }

    @Override
    public int hashCode() {
        return invoke("hashCode");
    }

    private static <E extends Exception> E raise(Class<E> ignore, Throwable ex) throws E {
        throw (E)ex;
    }

}
