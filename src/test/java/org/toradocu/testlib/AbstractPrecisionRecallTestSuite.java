package org.toradocu.testlib;

import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.toradocu.extractor.BlockTag;
import org.toradocu.util.Stats;

/**
 * Represents an abstract test suite that uses precision and recall to measure relevance.
 *
 * <p>To create a new precision/recall test suite, create a new test class extending
 * `org.toradocu.testlib.AbstractPrecisionRecallTestSuite`.
 *
 * <p>In the newly created class, create a method for each target class. In the method add the
 * following line of code to execute Toradocu on the target class: {@code Stats stats =
 * test(<qualified_class_name>)}. A Stats object contains the results (e.g., precision, recall, ...)
 * of the execution of Toradocu on a given class. You probably want to add assertions on the
 * precision/recall values obtained. The existing test suites have examples of assertions on the
 * precision/recall values.
 */
public abstract class AbstractPrecisionRecallTestSuite {

  /**
   * Fuzz factor for approximate equality. Has nothing to do with precision in the "precision and
   * recall" sense.
   */
  private static final double PRECISION = 0.001;

  /** Directory where test results are saved. */
  static final String OUTPUT_DIR = "build/test-results";

  /** Keeps track of statistics on currently run tests. */
  private static final TestSuiteStats testSuiteStats = new TestSuiteStats();
  /** The directory containing the source files on which to run tests. */
  private final String sourceDirPath;
  /** The directory containing the binaries on which to run tests. */
  private final String binDirPath;
  /** The directory containing the goal output of the tests. */
  private final String goalOutputDirPath;

  /**
   * Constructs and initializes a precision recall test suite that will test Toradocu using the
   * files in the given directories.
   *
   * @param sourceDirPath the path to the sources of the library to test
   * @param binDirPath the path to the binaries of the library to test
   * @param goalOutputDirPath the path to the directory containing the goal output files
   */
  public AbstractPrecisionRecallTestSuite(
      String sourceDirPath, String binDirPath, String goalOutputDirPath) {
    this.sourceDirPath = sourceDirPath;
    this.binDirPath = binDirPath;
    this.goalOutputDirPath = goalOutputDirPath;
  }

  /** Creates the temporary directory if it does not exist. */
  @BeforeClass
  public static void setUp() throws IOException {
    new File(OUTPUT_DIR).mkdir();
  }

  /** Prints the results (i.e. statistics) of the test suite. */
  @AfterClass
  public static void tearDown() {
    System.out.println(
        "=== Test Suite ==="
            + "\nNumber of conditions: "
            + testSuiteStats.getTotalNumConditions()
            + "\nAverage precision on @return: "
            + String.format("%.2f", testSuiteStats.getPrecision(BlockTag.Kind.RETURN))
            + "\nAverage recall on @return: "
            + String.format("%.2f", testSuiteStats.getRecall(BlockTag.Kind.RETURN))
            + "\nAverage precision on @param: "
            + String.format("%.2f", testSuiteStats.getPrecision(BlockTag.Kind.PARAM))
            + "\nAverage recall on @param: "
            + String.format("%.2f", testSuiteStats.getRecall(BlockTag.Kind.PARAM))
            + "\nAverage precision on @throws: "
            + String.format("%.2f", testSuiteStats.getPrecision(BlockTag.Kind.THROWS))
            + "\nAverage recall on @throws: "
            + String.format("%.2f", testSuiteStats.getRecall(BlockTag.Kind.THROWS)));
  }

  /**
   * Computes precision and recall for the given target class and checks that precision and recall
   * are as expected for the given target class (for both @param and @throws tags).
   *
   * @param targetClass the fully qualified name of the class on which to run the test
   * @param throwsPrecision the expected precision for @throws tag translations
   * @param throwsRecall the expected recall for @throws tag translations
   * @param paramPrecision the expected precision for @param tag translations
   * @param paramRecall the expected recall for @param tag translations
   */
  protected void test(
      String targetClass,
      double throwsPrecision,
      double throwsRecall,
      double paramPrecision,
      double paramRecall,
      double returnPrecision,
      double returnRecall) {
    final Stats stats =
        PrecisionRecallTest.computePrecisionAndRecall(
            targetClass, sourceDirPath, binDirPath, goalOutputDirPath);
    testSuiteStats.addStats(stats);
    assertThat(
        "@throws precision is different than expected",
        stats.getPrecision(BlockTag.Kind.THROWS),
        closeTo(throwsPrecision, PRECISION));
    assertThat(
        "@throws recall is different than expected",
        stats.getRecall(BlockTag.Kind.THROWS),
        closeTo(throwsRecall, PRECISION));
    assertThat(
        "@param precision is different than expected",
        stats.getPrecision(BlockTag.Kind.PARAM),
        closeTo(paramPrecision, PRECISION));
    assertThat(
        "@param recall is different than expected",
        stats.getRecall(BlockTag.Kind.PARAM),
        closeTo(paramRecall, PRECISION));
    assertThat(
        "@return precision is different than expected",
        stats.getPrecision(BlockTag.Kind.RETURN),
        closeTo(returnPrecision, PRECISION));
    assertThat(
        "@return recall is different than expected",
        stats.getRecall(BlockTag.Kind.RETURN),
        closeTo(returnRecall, PRECISION));
  }
}
