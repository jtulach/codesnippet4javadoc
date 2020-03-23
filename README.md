# Codesnippet Javadoc Doclet

[![Build Status](https://travis-ci.org/jtulach/codesnippet4javadoc.svg?branch=master)](https://travis-ci.org/jtulach/codesnippet4javadoc)

Say farewell to **broken** or **outdated samples** in your Javadoc! The *Codesnippet Doclet* helps you include real code snippets in the documentation ensuring they are **always compilable**. If you make the samples part of your test suite, even ensuring they **execute properly**.

Use *org.apidesign.javadoc.codesnippet.Doclet* to **increase quality** of your Javadoc! The doclet uses the same infrastructure as was used when publishing [Practical API Design](http://practical.apidesign.org) and [20 API Paradoxes](http://buy.apidesign.org) books making sure **all code samples** were **correct**, **compilable** and printed with **pretty syntax** coloring.

## How does it work?

The Codesnippet Doclet introduces new tag **codesnippet** that allows you to reference real code snippets in your project. Identify the snippets in your code and then reference them from a Javadoc:

```java
/** My sample class.
 * {@codesnippet sample1}
 * Rest of the text.
 */
public class SampleClass {
    private SampleClass() {
    }

    private static void sample1() {
        // BEGIN: sample1
        int x = 42;
        // END: sample1
    }
}
```

The rendered Javadoc for the class will include:
```
My sample class.
int x = 42;
Rest of the text.
```

Identify important pieces of code and add line comment **BEGIN: samplename** before start of each snippet. Put **END: samplename** or **FINISH: samplename** at the end of the code snippet. Then you can reference the snippet in Javadoc with
the **@codesnippet** tag.

Having correct samples in Javadoc has never been easier!

## Use in a Maven Project

The bits of the [codesnippet-doclet](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22codesnippet-doclet%22)
are being uploaded to [Maven central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22codesnippet-doclet%22).
Add the doclet to your Maven Javadoc plugin configuration
(as done [here](https://github.com/jtulach/codesnippet4javadoc/commit/16fd9cf7114d9ddc087cb3c0fcaec3d44acb2ed2#diff-74a104b8e241b27d093230d1c9a23dc4R16)):

```xml
<plugin>
   <groupId>org.apache.maven.plugins</groupId>
   <artifactId>maven-javadoc-plugin</artifactId>
   <version>2.10.3</version>
   <configuration>
     <doclet>org.apidesign.javadoc.codesnippet.Doclet</doclet>
     <docletArtifact>
       <groupId>org.apidesign.javadoc</groupId>
       <artifactId>codesnippet-doclet</artifactId>
       <version>0.54</version> <!-- or any newer version -->
     </docletArtifact>
     <!-- if you want to reference snippets from your test directory, also include -->
     <additionalparam>-snippetpath src/test/java</additionalparam>
    </configuration>
</plugin>
```

## Use in a Gradle Project

Add the doclet to your Gradle javadoc configuration

```groovy
configurations {
    snippetdoclet
}

dependencies {
    snippetdoclet group: 'org.apidesign.javadoc', name: 'codesnippet-doclet', version: '0.54'
}

javadoc {
    options.doclet = "org.apidesign.javadoc.codesnippet.Doclet"
    options.docletpath = configurations.snippetdoclet.files.asType(List)
    options.addStringOption "snippetpath", "src/test/java"
}
```

## Use with JDK11, JDK13 & co.

The Codesnippet doclet supports JDK11, JDK13 and newer as well as JDK8. The old
Javadoc API has been removed in JDK13, but it seems I found a way
to support older as well as new style.

## Use with Command Line Javadoc Tool

Get the Codesnippet Doclet binary. Preferrably from the [Maven Central](http://search.maven.org/#search|ga|1|codesnippet-doclet). Invoke your Javadoc as usually plus add following parameters:

```bash
$ javadoc \
  -doclet org.apidesign.javadoc.codesnippet.Doclet \
  -docletpath path/to/downloaded/codesnippet-doclet.jar \
  -snippetpath src/test:src/sample # in case you want to pick the samples from other locations as well
```

## Embed Snippets in API files

You may prefer to include code snippets into the same files as your API to
improve life of people who browse the source in an IDE. In such case follow
the sample described in [EmbeddingSampleCode file](https://github.com/jtulach/codesnippet4javadoc/blob/515fdd141c8caed9d86afce859afb15a81054f7f/testing/src/main/java/org/apidesign/javadoc/testing/EmbeddingSampleCode.java) - e.g. add yet another
class to the end of your file, give it a special name (for example My**Snippet**)
and put code snippet there.

In addition to that you can reference your class as `{@link MySnippet}` if you
pass in additional parameter to specify format of your snippet classes:
```bash
$ javadoc \
  -snippetclasses .*Snippet.*
```
The doclet will then convert all links to classes that match such pattern
into appropriate code snippets.

You may want to exclude these sample classes from the final *JAR* file. The
easiest way to do so is to configure your JAR packager to ignore such files:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>2.6</version>
    <configuration>
        <excludes>
            <exclude>**/*Snippet*.class</exclude>
        </excludes>
    </configuration>
</plugin>
```
Real life example is available [here](https://github.com/jtulach/codesnippet4javadoc/blob/515fdd141c8caed9d86afce859afb15a81054f7f/testing/pom.xml#L45).

## Maximum line length
By default Codesnippet will raise an error when the line length of the snippet exceeds 80 characters. This default
can be altered by specifying the maximum line length as parameter, like demonstrated below.
```bash
$ javadoc \
  -doclet org.apidesign.javadoc.codesnippet.Doclet \
  -docletpath path/to/downloaded/codesnippet-doclet.jar \
  -maxLineLength 120
```

## Verify @since tag

Quality of an API documentation can be increased if one requires that
*every API element has a @since tag*. To verify such statement
an automated tool is needed. This doclet is such tool. Just pass
`verifysincepresent` parameter
```bash
$ javadoc \
  -doclet org.apidesign.javadoc.codesnippet.Doclet \
  -docletpath path/to/downloaded/codesnippet-doclet.jar \
  -verifysincepresent
```
and warning will be printed for every element without the **@since** tag.

## Hide @Deprecated Classes

The code snippet doclet can, since version 0.11, exclude Javadoc elements annotated by some annotation from the Javadoc.
This is especially useful with `java.lang.Deprecated` annotation, by using:

```bash
$ javadoc \
  -doclet org.apidesign.javadoc.codesnippet.Doclet \
  -docletpath path/to/downloaded/codesnippet-doclet.jar \
  -hiddingannotation java.lang.Deprecated
```

one can eliminate deprecated fields and methods from the Javadoc and also hide classes and interfaces from the Javadoc
overview (however their individual HTML pages still remain in Javadoc for those who keep permanent links to them). One
can use the `-hiddingannotation` parameter with other annotations as well and even repeat the parameter multiple times
to hide multiple annotations at once.

## Linking types

The doclet searches existing `import` statements in the code snippet and tries to link the Java files in the package. 
When a matching type is found, the identifier is wrapped with `@link`. Note that the linking is best-effort only and when 
the type is not found, a warning message is logged `Tag @link: reference not found: <classname>`. 

You can suppress the warning by passing the parameter `-suppressMissingLinkWarnings` as shown below. This parameter will 
convert the warning to an informational message, so, you can continue to see the types that are missing links and not 
fail the build on warnings if `failOnWarnings` flag is set in your javadoc configuration.

```bash
$ javadoc \
  -doclet org.apidesign.javadoc.codesnippet.Doclet \
  -docletpath path/to/downloaded/codesnippet-doclet.jar \
  -suppressMissingLinkWarnings
```

## License

Feel free to use the Codesnippet Doclet binary to generate any public or private Javadoc. If you include the Codesnippet Doclet in your product or make modifications to it, please obey its *GPL 3.0* license.

## Projects Using Codesnippet Doclet

* [Truffle](https://github.com/oracle/graal/tree/master/truffle#readme) in its [Javadoc](http://www.graalvm.org/truffle/javadoc/)
* [Graal](https://github.com/oracle/graal/) in its [Graph I/O API](http://www.graalvm.org/graphio/javadoc/org/graalvm/graphio/package-summary.html)
* [Apache HTML/Java API](https://github.com/apache/incubator-netbeans-html4j#readme) in its [Javadoc](http://bits.netbeans.org/html+java/1.7/net/java/html/BrwsrCtx.html#execute-java.lang.Runnable-)
