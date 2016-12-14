# Tutorial

This step-by-step tutorial illustrates how Toradocu can be used to discover
faults in a software system under test, and to fix assertions in an existing test suite.
This tutorial covers the test of a toy class `net.Connection` that represents a network
connection. The class offers the method `open` to establish a new connection, `isOpen` to check
the connection status, and `send` to send a message using the opened connection.

1. Compile Toradocu and download required dependencies: `./gradlew tutorial`

2. Move to the tutorial directory: `cd tutorial`

   The directory `tutorial/src` contains the source code of the system under test,
   while the directory `tutorial/test` contains the source code of the developer-written test
   suite. Please, take some time to read the source code of the class `net.ConnectionTest` and of
   the test suite `net.ConnectionTest`.

   The method `net.Connection#open` is called by the test case `net.ConnectionTest#open`, while
   the method `net.Connection#send` is called by the test case `net.ConnectionTest#send`.

3. Compile the source code of the system under test: `javac src/net/Connection.java`

4. Compile and execute the test suite
   ```
   javac -cp junit-4.12.jar:src test/net/ConnectionTest.java
   java -cp junit-4.12.jar:hamcrest-core-1.3.jar:test:src org.junit.runner.JUnitCore net.ConnectionTest
   ```
   The test suite terminates with 1 failure, printing
   ```
   There was 1 failure:
   1) send(net.ConnectionTest)
   java.lang.NullPointerException
           at net.Connection.send(Connection.java:29)
           ... [many more lines of output]
   ```
   Without Toradocu, you would normally have to look at the code of the failing test case, and
   inspect the code of the method under test to understand the cause of the problem. In this
   case, the test case failed because the method `send` has been invoked with a `null` argument.
   Inspecting the source code you can realize that the expected behavior is exactly to throw a
   `NullPointerException` when its argument is `null`. In other words, the test suite produced a
   false positive: a test failure with a *correct* code under test.

   At this point you have two options:
     1. Remove the failing test case from the test suite. The downside is that the exceptional
        behavior of the method `send` is no longer tested. Generally speaking, testing less (cover
        less software behaviors) is not a good choice.
     2. Fix the test case and/or the assertions in the test case, so that the `NullPointerException`
        is the expected behavior and the test case does not fail any longer.

   Toradocu can fix your tests automatically.

5. Run Toradocu to convert Javadoc comments into assertions, where the
   assertions are expressed as aspects in the directory `aspects`.
   ```
   java -jar ../build/libs/toradocu-1.0-all.jar \
   --target-class net.Connection \
   --test-class net.ConnectionTest \
   --source-dir src \
   --class-dir src \
   --aspects-output-dir aspects
   ```
   Toradocu also prints the output of its condition translation phase, which is a JSON data
   structure.

   Each generated aspect has two important parts:
   1. The pointcut definition (the method the aspect is referring to).
   ```java
   @Around("call(void net.Connection.send(java.lang.String)) && within(net.ConnectionTest)")
   public Object advice(ProceedingJoinPoint jp) throws Throwable {
   ...
   ```
   2. The conditions for which the method is expecting to throw an exception.
   ```java
      ...
      // @throws NullPointerException if message is null
      if (((java.lang.String) args[0]) == null) {
        try {
          expectedExceptions.add(Class.forName("java.lang.NullPointerException"));
      ...
   ```

6. Compile the generated aspects and weave them into the test suite of the system under test
   ```
   javac -g -cp aspectjrt-1.8.9.jar:src:junit-4.12.jar aspects/Aspect_*.java
   java -cp aspectjtools-1.8.9.jar:aspectjweaver-1.8.9.jar:aspectjrt-1.8.9.jar:src org.aspectj.tools.ajc.Main -inpath aspects:test -outjar weaved_testsuite.jar -showWeaveInfo
   ```
   The AspectJ tool outputs some information starting with `Join point ...`.

7. Run the test suite augmented with Toradocu's oracles
   ```
   java -cp junit-4.12.jar:hamcrest-core-1.3.jar:src:weaved_testsuite.jar:aspectjrt-1.8.9.jar org.junit.runner.JUnitCore net.ConnectionTest
   ```
   The test suite execution still terminates with one failure, but this time is referring to a
   different test case. What happened is that fixing the assertions, *Toradocu eliminated the
   false positive failure* (transformed a false positive into a true negative), and let another
   test case fail:

   ```
   There was 1 failure:
   1) open(net.ConnectionTest)
   java.lang.AssertionError: Triggered aspect: Aspect_1 (ConnectionTest.java:13) -> Failure: Expected exception not thrown. Expected exceptions were: java.lang.IllegalStateException
           ... [many more lines of output]
   ```
   As the report says, *wrongly* the method `net.Connection#open` did not raise any exception
   even though an `IllegalStateException` was expected. The method `net.Connection#open` is
   supposed to throw an IllegalStateException if invoked on an open connection, but it does not.
   In other words, Toradocu allowed you to discover a previously unknown bug in the software
   under test, transforming a test case that was not failing (false negative) into a failing
   test case (true positive).

## Run Toradocu on Multiple Classes

Precision and recall of Toradocu are measured on a fairly large test suite. The test suite runs
Toradocu on several classes of real open source projects like
[Google Guava](https://github.com/google/guava) and
[Apache Commons Math](https://commons.apache.org/proper/commons-math/). The total amount of Java
methods whose comments are analyzed by Toradocu is around 300.

The following command runs Toradocu precision/recall test suite keeping the aspects generation
enabled (it is off by default during precision/recall measurement.)
```
./gradlew test --tests "org.toradocu.PrecisionRecall*" -Dorg.toradocu.generator=true
```
Generated aspects are saved in the folder `aspects` in the project root folder.
