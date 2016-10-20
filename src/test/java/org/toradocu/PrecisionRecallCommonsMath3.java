package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.TestCaseStats;

public class PrecisionRecallCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSMATH_3_SRC =
      "src/test/resources/src/commons-math3-3.6.1-src/src/main/java";
  private static final String COMMONSMATH_3_BIN = "src/test/resources/bin/commons-math3-3.6.1.jar";
  private static final String COMMONSMATH_3_EXPECTED_DIR =
      "src/test/resources/expected-output/CommonsMath-3.6.1/";

  public PrecisionRecallCommonsMath3() {
    super(COMMONSMATH_3_SRC, COMMONSMATH_3_BIN, COMMONSMATH_3_EXPECTED_DIR);
  }

  @Test
  public void gaussianTest() throws Exception {
    test("org.apache.commons.math3.analysis.function.Gaussian", 1.0, 1.0);
  }

  @Test
  public void logisticTest() throws Exception {
    test("org.apache.commons.math3.analysis.function.Logistic", 1.0, 1.0);
  }

  @Test
  public void functionUtilsTest() throws Exception {
    test("org.apache.commons.math3.analysis.FunctionUtils", 0.0, 0.0);
  }

  @Test
  public void baseAbstractUnivariateIntegratorTest() throws Exception {
    test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator", 1.0, 0.357);
  }

  @Test
  public void stepFunctionTest() throws Exception {
    test("org.apache.commons.math3.analysis.function.StepFunction", 0.5, 0.4);
  }

  @Test
  public void iterativeLegendreGaussIntegratorTest() throws Exception {
    test(
        "org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator",
        1.0,
        0.733);
  }

  @Test
  public void linearInterpolatorTest() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LinearInterpolator", 1.0, 0.4);
  }

  @Test
  public void loessInterpolatorTest() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator", 1.0, 0.6);
  }

  @Test
  public void polynomialFunctionNewtonFormTest() throws Exception {
    test(
        "org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm", 0.625, 0.454);
  }

  @Test
  public void binaryMutationTest() throws Exception {
    test("org.apache.commons.math3.genetics.BinaryMutation", 1.0, 1.0);
  }

  @Test
  public void cycleCrossoverTest() throws Exception {
    test("org.apache.commons.math3.genetics.CycleCrossover", 0.5, 0.25);
  }
}
