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

import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.JavacMessages;
import com.sun.tools.javac.util.Name;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

enum SymbolKind {
    NIL(0),
    PCK(1),
    TYP(2),
    VAR(4),
    MTH(8),
    POLY(16),
    MDL(32),
    ERR(63);

    final int value;

    private SymbolKind(int v) {
        this.value = v;
    }

    private static final Field kind;
    static {
        try {
            kind = Symbol.class.getField("kind");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    boolean is(Symbol s) {
        int ordinal = kindValue(s);
        return (this.value & ordinal) != 0;
    }

    private static int kindValue(Symbol s) throws IllegalStateException {
        Object value;
        try {
            value = kind.get(s);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        int ordinal;
        if (value instanceof Enum) {
            final int at = ((Enum) value).ordinal();
            if (at == 0) {
                ordinal = 0;
            } else {
                ordinal = 1 << (at - 1);
            }
        } else {
            ordinal = (Integer) value;
        }
        return ordinal;
    }

    boolean same(Symbol s) {
        int ordinal = kindValue(s);
        return value == ordinal;
    }

    private static final Object RECURSIVE;
    private static final Object NON_RECURSIVE;
    private static final Method GET_SYMBOLS;
    private static final Field ELEMS;
    private static final Field SYM;
    private static final Field SIBLING;
    private static final Method NEXT;
    static {
        Object rec, nonRec, getSym;
        try {
            Class<? extends Enum> LookupKind = Class.forName("com.sun.tools.javac.code.Scope$LookupKind").asSubclass(Enum.class);
            rec = Enum.valueOf(LookupKind, "RECURSIVE");
            nonRec = Enum.valueOf(LookupKind, "NON_RECURSIVE");
            getSym = Scope.class.getMethod("getSymbols", LookupKind);
        } catch (ClassNotFoundException ex) {
            rec = null;
            nonRec = null;
            getSym = null;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        RECURSIVE = rec;
        NON_RECURSIVE = nonRec;
        GET_SYMBOLS = (Method) getSym;

        Field elems;
        Field sym;
        Field sibling;
        Method next;
        try {
            elems = Scope.class.getField("elems");
            Class<?> Entry = Class.forName("com.sun.tools.javac.code.Scope$Entry");
            sym = Entry.getField("sym");
            sibling = Entry.getField("sibling");
            next = Entry.getMethod("next");
        } catch (Exception ex) {
            elems = null;
            sym = null;
            sibling = null;
            next = null;
        }

        ELEMS = elems;
        SYM = sym;
        SIBLING = sibling;
        NEXT = next;
    }

    static Iterable<Symbol> getSymbolsByName(Scope members, boolean recursive, Name name) {
        List<Symbol> arr = new ArrayList<>();
        for (Symbol s : getSymbols(members, recursive)) {
            if (s.name.equals(name)) {
                arr.add(s);
            }
        }
        return arr;
    }

    static Iterable<Symbol> getSymbols(Scope members, boolean recursive) {
        try {
            if (GET_SYMBOLS != null) {
                return (Iterable<Symbol>) GET_SYMBOLS.invoke(members, recursive ? RECURSIVE : NON_RECURSIVE);
            } else {
                List<Symbol> arr = new ArrayList<>();
                Object e = ELEMS.get(members);
                while (e != null) {
                    arr.add(Symbol.class.cast(SYM.get(e)));
                    e = recursive ? NEXT.invoke(e) : SIBLING.get(e);
                }
                return arr;
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final Method ENTER_CLASS;
    private static final Method SYMS_ENTER_CLASS;
    private static final Field SYMS_JAVA_BASE;
    static {
        Method e;
        Method se;
        Field sjb;
        try {
            e = ClassReader.class.getMethod("enterClass", Name.class);
            se = null;
            sjb = null;
        } catch (NoSuchMethodException ex) {
            e = null;
            try {
                Class<?> ModuleSymbol = Class.forName("com.sun.tools.javac.code.Symbol$ModuleSymbol");
                se = Symtab.class.getMethod("enterClass", ModuleSymbol, Name.class);
                sjb = Symtab.class.getField("java_base");
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException ex2) {
                throw new IllegalStateException(ex2);
            }
        }
        ENTER_CLASS = e;
        SYMS_ENTER_CLASS = se;
        SYMS_JAVA_BASE = sjb;
    }
    static Symbol enterClass(DocEnv env, Symtab syms, Name n) {
        try {
            if (ENTER_CLASS != null) {
                Object reader = READER.get(env);
                return (Symbol) ENTER_CLASS.invoke(reader, n);
            } else {
                Object base = SYMS_JAVA_BASE.get(syms);
                return (Symbol) SYMS_ENTER_CLASS.invoke(syms, base, n);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }
    private static final Field READER;
    private static final Method ENTER_PACKAGE;
    static {
        Method ep;
        Field rf;
        try {
            ep = ClassReader.class.getMethod("enterPackage", Name.class);
            rf = DocEnv.class.getField("reader");
        } catch (ReflectiveOperationException ex) {
            ep = null;
            rf = null;
        }
        ENTER_PACKAGE = ep;
        READER = rf;
    }
    static PackageSymbol enterPackage(DocEnv env, Name n) {
        try {
            Object reader = READER.get(env);
            return (PackageSymbol) ENTER_PACKAGE.invoke(reader, n);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }


    static Symbol.ClassSymbol loadClass(DocEnv env, Symtab syms, Name n) {
        return (Symbol.ClassSymbol) enterClass(env, syms, n);
    }

    private static final Field PACKAGES;
    private static final Method INFER_MODULE;
    private static final Method GET_PACKAGE;
    private static final Method GET_CLASS;
    private static final Field CLASSES;
    static {
        Field f;
        Method i, p, c;
        try {
            f = Symtab.class.getField("packages");
            i = null;
            p = null;
            c = null;
        } catch (NoSuchFieldException ex) {
            try {
                i = Symtab.class.getMethod("inferModule", Name.class);
                p = Symtab.class.getMethod("getPackage", i.getReturnType(), Name.class);
                c = Symtab.class.getMethod("getClass", i.getReturnType(), Name.class);
                f = null;
            } catch (NoSuchMethodException ex1) {
                throw new IllegalStateException(ex1);
            }
        }
        PACKAGES = f;
        INFER_MODULE = i;
        GET_PACKAGE = p;
        GET_CLASS = c;
    }
    static {
        Field f;
        try {
            f = Symtab.class.getField("classes");
        } catch (NoSuchFieldException ex) {
            f = null;
        }
        CLASSES = f;
    }
    static Symbol.PackageSymbol lookupPackage(Symtab syms, Name nameImpl) {
        try {
            PackageSymbol p;
            if (PACKAGES != null) {
                Map<?,?> packages = (Map<?,?>) PACKAGES.get(syms);
                p = (PackageSymbol) packages.get(nameImpl);
            } else {
                Object mod = INFER_MODULE.invoke(syms, nameImpl);
                p = mod != null ? (PackageSymbol) GET_PACKAGE.invoke(syms, mod, nameImpl) : null;
            }
            return p;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static Symbol.ClassSymbol getClass(Symtab syms, Name nameImpl) {
        try {
            if (CLASSES != null) {
                Map<?,?> packages = (Map<?,?>) CLASSES.get(syms);
                return (ClassSymbol) packages.get(nameImpl);
            } else {
                Name packageImpl = Convert.packagePart(nameImpl);
                Object mod = INFER_MODULE.invoke(syms, packageImpl);
                return mod != null ? (Symbol.ClassSymbol) GET_CLASS.invoke(syms, mod, nameImpl) : null;
            }
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static void addResourceBundle(JavacMessages messages, String bundle) {
        for (Method m : JavacMessages.class.getMethods()) {
            if (m.getName().equals("add") && m.getParameterCount() == 1 && m.getParameterTypes()[0] != String.class) {
                try {
                    InvocationHandler handler = (___, __, args) -> {
                        Locale locale = (Locale) args[0];
                        return ResourceBundle.getBundle("com.sun.tools.oldlets.javadoc.main.javadoc", locale);
                    };
                    Object bundleProvider = Proxy.newProxyInstance(SymbolKind.class.getClassLoader(), m.getParameterTypes(), handler);
                    m.invoke(messages, bundleProvider);
                    return;
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        messages.add(bundle);
    }

    static <T> T invokeOrNull(Object thiz, String name, Object... args) {
        for (Method m : thiz.getClass().getMethods()) {
            if (name.equals(m.getName())) {
                try {
                    return (T) m.invoke(thiz, args);
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return null;
    }

    static <T> T invokeStaticOrNull(Class<?> type, String name, Object... args) {
        for (Method m : type.getMethods()) {
            if (name.equals(m.getName())) {
                try {
                    return (T) m.invoke(null, args);
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return null;
    }

    static <T> T getStaticOrElse(Class<?> type, String fieldName, T defaultValue) {
        try {
            return (T) type.getField(fieldName).get(null);
        } catch (ReflectiveOperationException ex) {
            return defaultValue;
        }
    }

    static <T> T getOrElse(Class<?> type, Object thiz, String fieldName, T defaultValue) {
        if (type == null) {
            type = thiz.getClass();
        }
        try {
            final Field f = type.getDeclaredField(fieldName);
            return (T) f.get(thiz);
        } catch (ReflectiveOperationException ex) {
            return defaultValue;
        }
    }

    static void setOrNothing(Object thiz, String fieldName, Object value) {
        try {
            thiz.getClass().getField(fieldName).set(thiz, value);
        } catch (ReflectiveOperationException ex) {
            // nothing
        }
    }
}
