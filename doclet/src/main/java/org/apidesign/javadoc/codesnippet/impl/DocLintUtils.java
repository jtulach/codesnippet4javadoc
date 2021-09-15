package org.apidesign.javadoc.codesnippet.impl;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.tools.doclint.DocLint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DocLintUtils {
    private static Class<?> docLintClass;
    private static DocLint docLint;

    static {
        try {
            docLintClass = Class.forName("com.sun.tools.doclint.DocLint");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static int getVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            final int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public static String XCUSTOM_TAGS_PREFIX = "-XcustomTags:";

    public static DocLint getDocLint() {
        if (docLint == null) {
            try {
                if (getVersion() <= 8) {
                    // new DocLint()
                    docLint = (DocLint) docLintClass.getConstructors()[0].newInstance();
                } else {
                    // DocLint.newDocLint()
                    docLint = (DocLint) docLintClass.getDeclaredMethod("newDocLint").invoke(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return docLint;
    }

    public static boolean isValidOption(String s) {
        try {
            if (getVersion() <= 8) {
                // DocLint.isValidOption(s) - static method
                return (boolean) docLintClass.getDeclaredMethod("isValidOption", String.class).invoke(null, s);
            } else {
                // getDocLint().isValidOption(s) - instance method
                return (boolean) docLintClass.getDeclaredMethod("isValidOption", String.class).invoke(getDocLint(), s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void init(DocLint doclint, JavacTask t, String[] doclintOpts, boolean b) {
        try {
            if (getVersion() <= 8) {
                // doclint.init(t, doclintOpts, b)
                docLintClass.getDeclaredMethod("init", JavacTask.class, String[].class, boolean.class)
                    .invoke(doclint, t, doclintOpts, b);
            } else {
                // doclint.init(t, doclintOpts);
                docLintClass.getDeclaredMethod("init", JavacTask.class, String[].class)
                    .invoke(doclint, t, doclintOpts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void scan(DocLint doclint, TreePath treePath) {
        try {
            // doclint.scan(treePath)
            docLintClass.getDeclaredMethod("scan", TreePath.class).invoke(doclint, treePath);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
