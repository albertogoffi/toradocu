export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home" 
export JAVA_OPTIONS="-Xmx2g"

# This script executes the test cases that correspond to the experiments.

# General test suite.
#TESTS='test --tests org.toradocu.accuracy.Accuracy*'
#ISSTA18 Paper test suite (10% classes per project).
#TESTS='issta18'
TESTS='testgen'

# The following is to run specific tests.
#TESTS_PREFIX="org.toradocu.accuracy.PrecisionRecall"
#TESTS='--tests '$TESTS_PREFIX'CommonsCollections4 --tests '$TESTS_PREFIX'CommonsMath3 --tests '$TESTS_PREFIX'Guava19 --tests '$TESTS_PREFIX'JGraphT --tests '$TESTS_PREFIX'PlumeLib'

# Run Toradocu and collect statistics
# ./gradlew --rerun-tasks --info $TESTS
./gradlew --info $TESTS

echo Done. See results at build/reports/tests/$TESTS/index.html, and see test cases at generated-tests/testgen-experiments-results/test-generation-data