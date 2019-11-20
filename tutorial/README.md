# Tutorial

This tutorial illustrates how Toradocu can discover
faults in a software system or incorrect assertions in its test suite.

1. Compile Toradocu and download required dependencies: `./gradlew tutorial`

2. Move to the tutorial directory: `cd tutorial`

   The directory `tutorial/src` contains the source code of the system
   under test:  a toy class [`net.Connection`](https://github.com/albertogoffi/toradocu/blob/master/tutorial/src/net/Connection.java) that represents a network
   connection.  The class offers the method `open` to establish a new
   connection, `isOpen` to check the connection status, and `send` to send
   a message using the opened connection.  Take a moment to read their
   [Javadoc documentation](https://github.com/albertogoffi/toradocu/blob/master/tutorial/src/net/Connection.java).

   The directory `tutorial/test` contains the developer-written test
   suite.
   The method `net.Connection#open` is called by the test case `net.ConnectionTest#open`, while
   the method `net.Connection#send` is called by the test case `net.ConnectionTest#send`.

3. Compile the source code of the system under test: `javac src/net/Connection.java`

4. Compile and execute the test suite:
   ```
   javac -cp junit-4.12.jar:src test/net/ConnectionTest.java
   java -cp junit-4.12.jar:hamcrest-core-1.3.jar:test:src:junit-4.12.jar org.junit.runner.JUnitCore net.ConnectionTest
   ```
   The test suite terminates with 1 failure, printing
   ```
   There was 1 failure:
   1) send(net.ConnectionTest)
   java.lang.NullPointerException
           at net.Connection.send(Connection.java:29)
           ... [many more lines of output]
   ```
   The failing test case invoked the method `send` with a `null` argument.
   A failing test usually reveals a bug in the system under test.
   However,
   inspecting the Javadoc for the `send` method indicates that it is correct:
   its intended behavior to throw a
   `NullPointerException` when its argument is `null`.
   In other words, the test failure is a
   false positive: the test is incorrect but the code under test is correct.

   Toradocu can fix the test automatically.

5. Run Toradocu to convert Javadoc comments into assertions, where the
   assertions are expressed as aspects in the directory `aspects`:
   ```
   java -jar ../build/libs/toradocu-1.0-all.jar \
   --target-class net.Connection \
   --test-class net.ConnectionTest \
   --source-dir src \
   --class-dir src \
   --aspects-output-dir aspects \
   --oracle-generation true
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

6. Compile the generated aspects and weave them into the test suite of the system under test:
   ```
   javac -g -cp aspectjrt-1.8.9.jar:src:junit-4.12.jar aspects/*.java
   java -cp aspectjtools-1.8.9.jar:aspectjweaver-1.8.9.jar:aspectjrt-1.8.9.jar:junit-4.12.jar:src org.aspectj.tools.ajc.Main -inpath aspects:test -outjar weaved_testsuite.jar -showWeaveInfo -source 1.8 -target 1.8
   ```
   The AspectJ tool outputs some information starting with `Join point ...`.

7. Run the test suite augmented with Toradocu's oracles:
   ```
   java -cp junit-4.12.jar:hamcrest-core-1.3.jar:src:weaved_testsuite.jar:aspectjrt-1.8.9.jar org.junit.runner.JUnitCore net.ConnectionTest
   ```
   There are two things to notice about the test results.
    1. The test that previously failed incorrectly (a false positive alarm)
       now passes:  Toradocu has corrected the incorrect test.
    2. A different test fails.  That test case had been incorrect (a missed
       alarm or false negative), but the test case is now correct,
       and its failure indicates a defect in the program under test.

   ```
   There was 1 failure:
   1) open(net.ConnectionTest)
   java.lang.AssertionError: Triggered aspect: Aspect_1 (ConnectionTest.java:13) -> Failure: Expected exception not thrown. Expected exceptions were: java.lang.IllegalStateException
           ... [many more lines of output]
   ```
   As the report says, the method `net.Connection#open` should have raised
   an `IllegalStateException` when invoked on an open connection, according
   to its Javadoc specification.
   In other words, Toradocu discovered a previously unknown bug in the software
   under test, transforming a test case that was not failing (false negative) into a failing
   test case (true positive).

## Run Toradocu on Multiple Classes

You can run Toradocu on about 500 real-world methods taken from
open source projects like
[Google Guava](https://github.com/google/guava) and
[Apache Commons Math](https://commons.apache.org/proper/commons-math/)

To generate aspects for these methods in directory `aspects/`, run:
```
./gradlew test --tests "org.toradocu.accuracy.Accuracy*" -Dorg.toradocu.generator=true
```
## ISSTA 2018 Artifact Evaluation

More instructions to run the latest official release of Toradocu (a.k.a JDoctor) can be found in the Wiki:

https://github.com/albertogoffi/toradocu/wiki/ISSTA-2018----Artifact-Evaluation
