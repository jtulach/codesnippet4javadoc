/*
 * Copyright (c) 1997, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.oldlets.javadoc.main;

import com.sun.javadoc.LanguageVersion;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.main.OptionHelper;
import com.sun.tools.javac.main.OptionHelper.GrumpyHelper;
import com.sun.tools.javac.util.ClientCodeException;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;

import static com.sun.tools.javac.code.Flags.*;
import com.sun.tools.javac.util.Context.Key;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Main program of Javadoc.
 * Previously named "Main".
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 * @since 1.2
 * @author Robert Field
 * @author Neal Gafter (rewrite)
 */
public class Start extends ToolOption.Helper {
    /** Context for this invocation. */
    private final Context context;

    private final String defaultDocletClassName;
    private final ClassLoader docletParentClassLoader;

    private static final String javadocName = "javadoc";

    private static final String standardDocletClassName =
        "com.sun.tools.doclets.standard.Standard";

    private final long defaultFilter = PUBLIC | PROTECTED;

    private final Messager messager;

    private DocletInvoker docletInvoker;

    /**
     * In API mode, exceptions thrown while calling the doclet are
     * propagated using ClientCodeException.
     */
    private boolean apiMode;

    private JavaFileManager fileManager;

    public Start(String programName,
          PrintWriter errWriter,
          PrintWriter warnWriter,
          PrintWriter noticeWriter,
          String defaultDocletClassName) {
        this(programName, errWriter, warnWriter, noticeWriter, defaultDocletClassName, null);
    }

    public Start(PrintWriter pw) {
        this(javadocName, pw, pw, pw, standardDocletClassName);
    }

    public Start(String programName,
          PrintWriter errWriter,
          PrintWriter warnWriter,
          PrintWriter noticeWriter,
          String defaultDocletClassName,
          ClassLoader docletParentClassLoader) {
        context = new Context();
        messager = new Messager(context, programName, errWriter, warnWriter, noticeWriter);
        this.defaultDocletClassName = defaultDocletClassName;
        this.docletParentClassLoader = docletParentClassLoader;
    }

    public Start(String programName, String defaultDocletClassName) {
        this(programName, defaultDocletClassName, null);
    }

    public Start(String programName, String defaultDocletClassName,
          ClassLoader docletParentClassLoader) {
        context = new Context();
        messager = new Messager(context, programName);
        this.defaultDocletClassName = defaultDocletClassName;
        this.docletParentClassLoader = docletParentClassLoader;
    }

    public Start(String programName, ClassLoader docletParentClassLoader) {
        this(programName, standardDocletClassName, docletParentClassLoader);
    }

    public Start(String programName) {
        this(programName, standardDocletClassName);
    }

    public Start(ClassLoader docletParentClassLoader) {
        this(javadocName, docletParentClassLoader);
    }

    public Start() {
        this(javadocName);
    }

    public Start(Context context) {
        this.context = Objects.requireNonNull(context);
        apiMode = true;
        defaultDocletClassName = standardDocletClassName;
        docletParentClassLoader = null;

        Log log = context.get(Log.logKey);
        if (log instanceof Messager)
            messager = (Messager) log;
        else {
            Key<PrintWriter> errKey = SymbolKind.getStaticOrElse(Log.class, "errKey", Log.outKey);
            PrintWriter out = context.get(errKey);
            messager = (out == null) ? new Messager(context, javadocName)
                    : new Messager(context, javadocName, out, out, out);
        }
    }

    /**
     * Usage
     */
    @Override
    void usage() {
        usage(true);
    }

    void usage(boolean exit) {
        usage("main.usage", "-help", "main.usage.foot", exit);
    }

    @Override
    void Xusage() {
        Xusage(true);
    }

    void Xusage(boolean exit) {
        usage("main.Xusage", "-X", "main.Xusage.foot", exit);
    }

    private void usage(String main, String doclet, String foot, boolean exit) {
        // RFE: it would be better to replace the following with code to
        // write a header, then help for each option, then a footer.
        messager.notice(main);

        // let doclet print usage information (does nothing on error)
        if (docletInvoker != null) {
            // RFE: this is a pretty bad way to get the doclet to show
            // help info. Moreover, the output appears on stdout,
            // and <i>not</i> on any of the standard streams passed
            // to javadoc, and in particular, not to the noticeWriter
            // But, to fix this, we need to fix the Doclet API.
            docletInvoker.optionLength(doclet);
        }

        if (foot != null)
            messager.notice(foot);

        if (exit) exit();
    }

    /**
     * Exit
     */
    private void exit() {
        messager.exit();
    }


    /**
     * Main program - external wrapper
     */
    public int begin(String... argv) {
        boolean ok = begin(null, argv, Collections.emptySet());
        return ok ? 0 : 1;
    }

    public boolean begin(Class<?> docletClass, Iterable<String> options, Iterable<? extends JavaFileObject> fileObjects) {
        Collection<String> opts = new ArrayList<>();
        for (String opt: options) opts.add(opt);
        return begin(docletClass, opts.toArray(new String[opts.size()]), fileObjects);
    }

    private boolean begin(Class<?> docletClass, String[] options, Iterable<? extends JavaFileObject> fileObjects) {
        boolean failed = false;

        try {
            failed = !parseAndExecute(docletClass, options, fileObjects);
        } catch (Messager.ExitJavadoc exc) {
            // ignore, we just exit this way
        } catch (OutOfMemoryError ee) {
            messager.error(Messager.NOPOS, "main.out.of.memory");
            failed = true;
        } catch (ClientCodeException e) {
            // simply rethrow these exceptions, to be caught and handled by JavadocTaskImpl
            throw e;
        } catch (Error ee) {
            ee.printStackTrace(System.err);
            messager.error(Messager.NOPOS, "main.fatal.error");
            failed = true;
        } catch (Exception ee) {
            ee.printStackTrace(System.err);
            messager.error(Messager.NOPOS, "main.fatal.exception");
            failed = true;
        } finally {
            if (fileManager != null) {
                boolean isAutoClose = SymbolKind.getOrElse(null, fileManager, "autoClose", false);
                if (isAutoClose) {
                    try {
                        fileManager.close();
                    } catch (IOException ignore) {
                    }
                }
            }
            messager.exitNotice();
            messager.flush();
        }
        failed |= messager.nerrors() > 0;
        failed |= rejectWarnings && messager.nwarnings() > 0;
        return !failed;
    }

    /**
     * Main program - internal
     */
    private boolean parseAndExecute(
            Class<?> docletClass,
            String[] argv,
            Iterable<? extends JavaFileObject> fileObjects) throws IOException {
        long tm = System.currentTimeMillis();

        ListBuffer<String> javaNames = new ListBuffer<>();

        // Preprocess @file arguments
        argv = CommandLine.parse(Arrays.asList(argv)).toArray(new String[0]);


        fileManager = context.get(JavaFileManager.class);

        setDocletInvoker(docletClass, fileManager, argv);

        compOpts = Options.instance(context);
        // Make sure no obsolete source/target messages are reported
        compOpts.put("-Xlint:-options", "-Xlint:-options");

        // Parse arguments
        for (int i = 0 ; i < argv.length ; i++) {
            String arg = argv[i];

            ToolOption o = ToolOption.get(arg);
            if (o != null) {
                // hack: this restriction should be removed
                if (o == ToolOption.LOCALE && i > 0)
                    usageError("main.locale_first");

                try {
                    if (o.hasArg) {
                        oneArg(argv, i++);
                        o.process(this, argv[i]);
                    } else {
                        setOption(arg);
                        o.process(this);
                    }
                } catch (IllegalArgumentException e) {
                    usageError("main.option.invalid.value", e.getMessage());
                }
            } else if (arg.equals("-XDaccessInternalAPI")) {
                // pass this hidden option down to the doclet, if it wants it
                if (docletInvoker.optionLength("-XDaccessInternalAPI") == 1) {
                    setOption(arg);
                }
            } else if (arg.startsWith("-XD")) {
                // hidden javac options
                String s = arg.substring("-XD".length());
                int eq = s.indexOf('=');
                String key = (eq < 0) ? s : s.substring(0, eq);
                String value = (eq < 0) ? s : s.substring(eq+1);
                compOpts.put(key, value);
            }
            // call doclet for its options
            // other arg starts with - is invalid
            else if (arg.startsWith("-")) {
                int optionLength;
                optionLength = docletInvoker.optionLength(arg);
                if (optionLength < 0) {
                    // error already displayed
                    exit();
                } else if (optionLength == 0) {
                    // option not found
                    usageError("main.invalid_flag", arg);
                } else {
                    // doclet added option
                    if ((i + optionLength) > argv.length) {
                        usageError("main.requires_argument", arg);
                    }
                    ListBuffer<String> args = new ListBuffer<>();
                    for (int j = 0; j < optionLength-1; ++j) {
                        args.append(argv[++i]);
                    }
                    setOption(arg, args.toList());
                }
            } else {
                javaNames.append(arg);
            }
        }

        if (fileManager == null) {
            JavacFileManager.preRegister(context);
            fileManager = context.get(JavaFileManager.class);
            SymbolKind.setOrNothing(fileManager, "autoClose", true);
        }
        SymbolKind.invokeOrNull(fileManager, "handleOptions", fileManagerOpts);

        try {
            Class<?> Arguments = Class.forName("com.sun.tools.javac.main.Arguments");
            Object arguments = SymbolKind.invokeStaticOrNull(Arguments, "instance", context);
            SymbolKind.invokeOrNull(arguments, "init", messager.programName);
            SymbolKind.invokeOrNull(arguments, "allowEmpty");
            SymbolKind.invokeOrNull(arguments, "validate");
        } catch (ClassNotFoundException ex) {
            // missing on JDK8
        }

        String platformString = compOpts.get("--release");

        if (platformString != null) {
            if (compOpts.isSet("-source")) {
                usageError("main.release.bootclasspath.conflict", "-source");
            }
            Option bootClassPath = ToolOption.findOption("BOOT_CLASS_PATH", "BOOTCLASSPATH");
            if (fileManagerOpts.containsKey(bootClassPath)) {
                usageError("main.release.bootclasspath.conflict", bootClassPath.name());
            }

            JavaFileManager platformFM;
            try {
                Class<?> PlatformDescription = Class.forName("com.sun.tools.javac.platform.PlatformDescription");
                Class<?> PlatformUtils = Class.forName("com.sun.tools.javac.platform.PlatformUtils");
                Object platformDescription = SymbolKind.invokeStaticOrNull(PlatformUtils, "lookupPlatformDescription", platformString);

                if (platformDescription == null) {
                    usageError("main.unsupported.release.version", platformString);
                }

                compOpts.put(Option.SOURCE, SymbolKind.invokeOrNull(platformDescription, "getSourceVersion"));

                putIntoContext(PlatformDescription, platformDescription);

                platformFM = SymbolKind.invokeOrNull(platformDescription, "getFileManager");
            } catch (ClassNotFoundException ex) {
                // not on JDK8
                platformFM = null;
            }

            try {
                Class<?> DelegatingJavaFileManager = Class.forName("com.sun.tools.javac.main.DelegatingJavaFileManager");
                SymbolKind.invokeStaticOrNull(DelegatingJavaFileManager, "installReleaseFileManager", context, platformFM, fileManager);
            } catch (ClassNotFoundException ex) {
                // missing on JDK8
            }
        }

        compOpts.notifyListeners();

        if (javaNames.isEmpty() && subPackages.isEmpty() && isEmpty(fileObjects)) {
            usageError("main.No_packages_or_classes_specified");
        }

        if (!docletInvoker.validOptions(options.toList())) {
            // error message already displayed
            exit();
        }

        JavadocTool comp = JavadocTool.make0(context);
        if (comp == null) return false;

        if (showAccess == null) {
            setFilter(defaultFilter);
        }

        LanguageVersion languageVersion = docletInvoker.languageVersion();
        RootDocImpl root = comp.getRootDocImpl(
                docLocale,
                encoding,
                showAccess,
                javaNames.toList(),
                options.toList(),
                fileObjects,
                breakiterator,
                subPackages.toList(),
                excludedPackages.toList(),
                docClasses,
                // legacy?
                languageVersion == null || languageVersion == LanguageVersion.JAVA_1_1,
                quiet);

        // release resources
        comp = null;

        // pass off control to the doclet
        boolean ok = root != null;
        if (ok) ok = docletInvoker.start(root);

        // We're done.
        if (compOpts.get("-verbose") != null) {
            tm = System.currentTimeMillis() - tm;
            messager.notice("main.done_in", Long.toString(tm));
        }

        return ok;
    }

    private <T> void putIntoContext(Class<T> type, Object value) {
        context.put(type, type.cast(value));
    }

    private <T> boolean isEmpty(Iterable<T> iter) {
        return !iter.iterator().hasNext();
    }

    /**
     * Init the doclet invoker.
     * The doclet class may be given explicitly, or via the -doclet option in
     * argv.
     * If the doclet class is not given explicitly, it will be loaded from
     * the file manager's DOCLET_PATH location, if available, or via the
     * -doclet path option in argv.
     * @param docletClass The doclet class. May be null.
     * @param fileManager The file manager used to get the class loader to load
     * the doclet class if required. May be null.
     * @param argv Args containing -doclet and -docletpath, in case they are required.
     */
    private void setDocletInvoker(Class<?> docletClass, JavaFileManager fileManager, String[] argv) {
        boolean exportInternalAPI = false;
        String docletClassName = null;
        String docletPath = null;

        // Parse doclet specifying arguments
        for (int i = 0 ; i < argv.length ; i++) {
            String arg = argv[i];
            if (arg.equals(ToolOption.DOCLET.opt)) {
                oneArg(argv, i++);
                if (docletClassName != null) {
                    usageError("main.more_than_one_doclet_specified_0_and_1",
                               docletClassName, argv[i]);
                }
                docletClassName = argv[i];
            } else if (arg.equals(ToolOption.DOCLETPATH.opt)) {
                oneArg(argv, i++);
                if (docletPath == null) {
                    docletPath = argv[i];
                } else {
                    docletPath += File.pathSeparator + argv[i];
                }
            } else if (arg.equals("-XDaccessInternalAPI")) {
                exportInternalAPI = true;
            }
        }

        if (docletClass != null) {
            // TODO, check no -doclet, -docletpath
            docletInvoker = new DocletInvoker(messager, docletClass, apiMode, exportInternalAPI);
        } else {
            if (docletClassName == null) {
                docletClassName = defaultDocletClassName;
            }

            // attempt to find doclet
            docletInvoker = new DocletInvoker(messager, fileManager,
                    docletClassName, docletPath,
                    docletParentClassLoader,
                    apiMode,
                    exportInternalAPI);
        }
    }

    /**
     * Set one arg option.
     * Error and exit if one argument is not provided.
     */
    private void oneArg(String[] args, int index) {
        if ((index + 1) < args.length) {
            setOption(args[index], args[index+1]);
        } else {
            usageError("main.requires_argument", args[index]);
        }
    }

    @Override
    void usageError(String key, Object... args) {
        messager.error(Messager.NOPOS, key, args);
        usage(true);
    }

    /**
     * indicate an option with no arguments was given.
     */
    private void setOption(String opt) {
        String[] option = { opt };
        options.append(option);
    }

    /**
     * indicate an option with one argument was given.
     */
    private void setOption(String opt, String argument) {
        String[] option = { opt, argument };
        options.append(option);
    }

    /**
     * indicate an option with the specified list of arguments was given.
     */
    private void setOption(String opt, List<String> arguments) {
        String[] args = new String[arguments.length() + 1];
        int k = 0;
        args[k++] = opt;
        for (List<String> i = arguments; i.nonEmpty(); i=i.tail) {
            args[k++] = i.head;
        }
        options.append(args);
    }

    @Override
    OptionHelper getOptionHelper() {
        return new GrumpyHelper(messager) {
            @Override
            public String get(com.sun.tools.javac.main.Option option) {
                return compOpts.get(option);
            }

            @Override
            public void put(String name, String value) {
                compOpts.put(name, value);
            }
        };
    }
}
