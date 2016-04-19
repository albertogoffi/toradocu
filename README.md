# Toradocu
Toradocu - automated generation of test oracles from Javadoc documentation

Toradocu generates test oracles from the Javadoc documentation of a
class. Toradocu and its internals are described in the paper *Automatic
Generation of Oracles for Exceptional Behaviors* by Alberto Goffi, Alessandra
Gorla, Michael D. Ernst, and Mauro Pezzè (presented at [ISSTA 2016](https://issta2016.cispa.saarland)).

Toradocu takes the source code of a class as input and produces a set of
[aspects](https://eclipse.org/aspectj/).

## Building Toradocu
To compile Toradocu run the command `./gradlew build -x test`.

The command `./gradlew shadowJar` creates a jar package that includes 
Toradocu as well as all the needed dependencies. This will create the file 
`build/libs/toradocu-0.1-all.jar`.

## Running Toradocu
Toradocu is a command line tool. To get the list of parameters (asterics
indicate mandatory parameters) execute

	  java -cp toradocu-0.1-all.jar org.toradocu.Toradocu --help

Internally Toradocu executes the `javadoc` tool. Every option starting with `-J`
will be passed to the `javadoc` tool. For example, you have to specify the path
to your sources with `-J-sourcepath=...`. You can customize the behavior of the
`javadoc` tool using all its options.

A typical Toradocu invocation looks like this

	java -cp toradocu-0.1-all.jar org.toradocu.Toradocu
   	--targetClass mypackage.MyClass
   	--outputDir output
   	--testClass mypackage.Test
   	-J-d=output/javadoc
   	-J-sourcepath=project/src

## Notes for Developers

### Toradocu Dependencies
* `lib/tools-jdk1.8.0_72.jar`: the custom doclet depends on this jar that is
  part of the standard JDK distribution (original name: tools.jar).
  Other dependencies are listed in `build.gradle` and are automatically
  downloaded during the building process.

### Get the standard doclet source code
1. Download the source code from
   [here](http://hg.openjdk.java.net/jdk8/jdk8/langtools/tags)
   2. Standard doclet is in `/src/share/classes/com/sun/tools/doclets/`
   3. Change package declarations with the command:
      ``perl -pi -e 's/com.sun.tools.doclets/org.newpackge/g' `find . -name
      “*.java”` ``
