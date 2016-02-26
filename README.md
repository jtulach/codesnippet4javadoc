# Codesnippet Javadoc Doclet

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

Add the doclet to your Maven Javadoc plugin configuration (as done [here](https://github.com/jtulach/codesnippet4javadoc/commit/16fd9cf7114d9ddc087cb3c0fcaec3d44acb2ed2#diff-74a104b8e241b27d093230d1c9a23dc4R16)):

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
       <version>0.3</version> <!-- or any newer version -->
     </docletArtifact>
     <!-- if you want to reference snippets from your test directory, also include -->
     <additionalparam>-snippetpath "${basedir}/src/test/java"</additionalparam>
    </configuration>
</plugin>
```


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
the sample described in **EmbeddingSampleCode** file - e.g. add yet another
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

## License

Feel free to use the Codesnippet Doclet binary to generate any public or private Javadoc. If you include the Codesnippet Doclet in your product or make modifications to it, please obey its *GPL 3.0* license.

## Projects Using Codesnippet Doclet

* [Truffle](https://github.com/graalvm/truffle) in its [Javadoc](http://lafo.ssw.uni-linz.ac.at/javadoc/truffle/latest/)
