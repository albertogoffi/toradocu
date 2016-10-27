package org.toradocu.testlib;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Represents an abstract test suite that uses precision and recall to measure relevance.
 *
 * To create a new precision/recall test suite, create a new test class extending
 * `org.toradocu.testlib.AbstractPrecisionRecallTestSuite`.
 *
 * In the newly created class, create a method for each target class. In the method add
 * the following line of code to execute Toradocu on the target class:
 * {@code TestCaseStats stats = test(<qualified_class_name>)}. A TestCaseStats object
 * contains the results (e.g., precision, recall, ...) of the execution of Toradocu on
 * a given class. You probably want to add assertions on the precision/recall values obtained.
 * The existing test suites have examples of assertions on the precision/recall values.
 */
public abstract class AbstractPrecisionRecallTestSuite {

  /** Fuzz factor for approximate equality.  Has nothing to do with precision in the "precision and recall" sense. */
  protected static final double PRECISION = 0.001;
  public static final String OUTPUT_DIR = "build/test-results";

  /** Keeps track of statistics on currently run tests. */
  private static final TestSuiteStats testSuiteStats = new TestSuiteStats();
  /** The directory containing the source files on which to run tests. */
  private final String sourceDirPath;
  /** The directory containing the binaries on which to run tests. */
  private final String binDirPath;
  /** The directory containing the goal output of the tests. */
  private final String goalOutputDirPath;

  /**
   * Constructs and initializes a precision recall test suite that will test
   * Toradocu using the files in the given directories.
   *
   * @param sourceDirPath the path to the sources of the library to test
   * @param binDirPath the path to the binaries of the library to test
   * @param goalOutputDirPath the path to the directory containing the
   *                              goal output files
   */
  public AbstractPrecisionRecallTestSuite(
      String sourceDirPath, String binDirPath, String goalOutputDirPath) {
    this.sourceDirPath = sourceDirPath;
    this.binDirPath = binDirPath;
    this.goalOutputDirPath = goalOutputDirPath;
  }

  /**
   * Creates the temporary directory if it does not exist.
   */
  @BeforeClass
  public static void setUp() throws IOException {
    Files.createDirectories(Paths.get(OUTPUT_DIR));
  }

  /**
   * Prints the results (i.e. statistics) of the test suite.
   */
  @AfterClass
  public static void tearDown() {
    testSuiteStats.computeResults();
    System.out.println(
        "=== Test Suite ==="
            + "\nAverage precision: "
            + String.format("%.2f", testSuiteStats.getPrecision())
            + ", Std deviation: "
            + String.format("%.2f", testSuiteStats.getPrecisionStdDeviation())
            + "\nAverage recall: "
            + String.format("%.2f", testSuiteStats.getRecall())
            + ", Std deviation: "
            + String.format("%.2f", testSuiteStats.getRecallStdDeviation())
            + "\nF-measure: "
            + String.format("%.2f", testSuiteStats.getFMeasure()));
  }

  /**
   * Computes precision and recall for the given target class.
   *
   * @param targetClass the fully qualified name of the class on which to run the test
   * @return the statistics for the test
   */
  protected TestCaseStats computePrecisionAndRecall(String targetClass) {
    TestCaseStats stats =
        PrecisionRecallTest.computePrecisionAndRecall(
            targetClass, sourceDirPath, binDirPath, goalOutputDirPath);
    testSuiteStats.addStats(stats);
    return stats;
  }

  /**
   * Tests that precision and recall are as expected for the given target class.
   *
   * @param targetClass the fully qualified name of the class on which to run the test
   * @param precision the expected precision
   * @param recall the expected recall
   */
  protected void test(String targetClass, double precision, double recall) {
    TestCaseStats stats = computePrecisionAndRecall(targetClass);
    assertThat(
        "Precision is different than expected",
        stats.getPrecision(),
        closeTo(precision, PRECISION));
    assertThat("Recall is different than expected", stats.getRecall(), closeTo(recall, PRECISION));
  }
}
