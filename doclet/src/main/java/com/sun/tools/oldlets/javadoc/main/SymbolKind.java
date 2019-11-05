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
import com.sun.tools.javac.util.Name;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            kind.setAccessible(true);
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
        for (Symbol s : arr) {
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
    static Symbol enterClass(ClassReader reader, Symtab syms, Name n) {
        try {
            if (ENTER_CLASS != null) {
                return (Symbol) ENTER_CLASS.invoke(reader, n);
            } else {
                Object base = SYMS_JAVA_BASE.get(syms);
                return (Symbol) SYMS_ENTER_CLASS.invoke(syms, base, n);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }
    private static final Method ENTER_PACKAGE;
    static {
        Method ep;
        try {
            ep = ClassReader.class.getMethod("enterPackage", Name.class);
        } catch (NoSuchMethodException ex) {
            ep = null;
        }
        ENTER_PACKAGE = ep;
    }
    static PackageSymbol enterPackage(ClassReader reader, DocEnv env, Name n) {
        try {
            return (PackageSymbol) ENTER_PACKAGE.invoke(env.reader, n);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }


    static Symbol.ClassSymbol loadClass(ClassReader reader, Symtab syms, Name n) {
        return (Symbol.ClassSymbol) enterClass(reader, syms, n);
    }

    private static final Field PACKAGES;
    private static final Field CLASSES;
    static {
        Field f;
        try {
            f = Symtab.class.getField("packages");
        } catch (NoSuchFieldException ex) {
            f = null;
        }
        PACKAGES = f;
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
            //        ModuleSymbol mod = syms.inferModule(nameImpl);
            //        PackageSymbol p = mod != null ? syms.getPackage(mod, nameImpl) : null;
            Map<?,?> packages = (Map<?,?>) PACKAGES.get(syms);
            PackageSymbol p = (PackageSymbol) packages.get(nameImpl);
            return p;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

    static Symbol.ClassSymbol getClass(Symtab syms, Name n) {
        try {
            //        ModuleSymbol mod = syms.inferModule(nameImpl);
            //        PackageSymbol p = mod != null ? syms.getPackage(mod, nameImpl) : null;
            Map<?,?> packages = (Map<?,?>) CLASSES.get(syms);
            return (ClassSymbol) packages.get(n);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
