package org.toradocu;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.TestCaseStats;

public class PrecisionRecallCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSMATH_3_SRC =
      "src/test/resources/src/commons-math3-3.6.1-src/commons-math3-3.6.1-src/src/main/java";
  private static final String COMMONSMATH_3_BIN = "src/test/resources/bin/commons-math3-3.6.1.jar";
  private static final String COMMONSMATH_3_EXPECTED_DIR = "src/test/resources/CommonsMath-3.6.1/";

  public PrecisionRecallCommonsMath3() {
    super(COMMONSMATH_3_SRC, COMMONSMATH_3_BIN, COMMONSMATH_3_EXPECTED_DIR);
  }

  @Test
  public void gaussianTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.math3.analysis.function.Gaussian");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
  }

  @Test
  public void logisticTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.math3.analysis.function.Logistic");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
  }

  @Test
  public void functionUtilsTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.math3.analysis.FunctionUtils");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
  }

  @Test
  public void baseAbstractUnivariateIntegratorTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
  }

  @Test
  public void stepFunctionTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.math3.analysis.function.StepFunction");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(0.8));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.8));
  }

  @Test
  public void iterativeLegendreGaussIntegratorTest() throws Exception {
    TestCaseStats stats =
        test("org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), closeTo(0.533, PRECISION));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.533, PRECISION));
  }

  @Test
  public void linearInterpolationTest() throws Exception {
    TestCaseStats stats =
        test("org.apache.commons.math3.analysis.interpolation.LinearInterpolator");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(0.6));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.6));
  }

  @Test
  public void loessInterpolatorTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), closeTo(0.473, PRECISION));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.473, PRECISION));
  }

  @Test
  public void polynomialFunctionNewtonFormTest() throws Exception {
    TestCaseStats stats =
        test("org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), closeTo(0.818, PRECISION));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.818, PRECISION));
  }

  @Test
  public void binaryMutationTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.math3.genetics.BinaryMutation");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(0.5));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.5));
  }

  @Test
  public void cycleCrossoverTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.math3.genetics.CycleCrossover");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(0.75));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.75));
  }
}
