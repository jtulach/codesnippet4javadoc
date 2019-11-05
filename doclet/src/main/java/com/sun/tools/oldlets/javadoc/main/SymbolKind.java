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

import com.sun.tools.javac.code.Symbol;
import java.lang.reflect.Field;

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
}
