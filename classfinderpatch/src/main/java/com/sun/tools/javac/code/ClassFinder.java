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
package com.sun.tools.javac.code;

import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.util.Context;
import java.util.EnumSet;
import javax.tools.JavaFileObject;

public abstract class ClassFinder extends ClassReader {
    protected static final Context.Key<ClassFinder> classFinderKey = new Context.Key<>();

    protected boolean preferSource;

    public ClassFinder(Context context) {
        super(context, false);
    }

    public ClassFinder(Context context, boolean anything) {
        super(context, false);
    }

    protected abstract EnumSet<JavaFileObject.Kind> getPackageFileKinds();
    protected abstract void extraFileActions(Symbol.PackageSymbol pack, JavaFileObject fo);
}
