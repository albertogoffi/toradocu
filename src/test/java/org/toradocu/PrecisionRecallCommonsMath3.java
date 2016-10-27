package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSMATH_3_SRC =
      "src/test/resources/src/commons-math3-3.6.1-src/src/main/java";
  private static final String COMMONSMATH_3_BIN = "src/test/resources/bin/commons-math3-3.6.1.jar";
  private static final String COMMONSMATH_3_EXPECTED_DIR =
      "src/test/resources/expected-output/commons-math3-3.6.1/";

  public PrecisionRecallCommonsMath3() {
    super(COMMONSMATH_3_SRC, COMMONSMATH_3_BIN, COMMONSMATH_3_EXPECTED_DIR);
  }

  @Test
  public void testGaussian() throws Exception {
    test("org.apache.commons.math3.analysis.function.Gaussian", 1.0, 1.0);
  }

  @Test
  public void testLogistic() throws Exception {
    test("org.apache.commons.math3.analysis.function.Logistic", 1.0, 1.0);
  }

  @Test
  public void testFunctionUtils() throws Exception {
    test("org.apache.commons.math3.analysis.FunctionUtils", 0.0, 0.0);
  }

  @Test
  public void testSimpsonIntegrator() throws Exception {
    test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator", 1.0, 0.357);
  }

  @Test
  public void testStepFunction() throws Exception {
    test("org.apache.commons.math3.analysis.function.StepFunction", 0.5, 0.4);
  }

  @Test
  public void testIterativeLegendreGaussIntegrator() throws Exception {
    test(
        "org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator",
        1.0,
        0.733);
  }

  @Test
  public void testLinearInterpolator() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LinearInterpolator", 0.666, 0.4);
  }

  @Test
  public void testLoessInterpolator() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator", 1.0, 0.6);
  }

  @Test
  public void testPolynomialFunctionNewtonForm() throws Exception {
    test(
        "org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm", 0.625, 0.454);
  }

  @Test
  public void testBinaryMutation() throws Exception {
    test("org.apache.commons.math3.genetics.BinaryMutation", 1.0, 1.0);
  }

  @Test
  public void testCycleCrossover() throws Exception {
    test("org.apache.commons.math3.genetics.CycleCrossover", 0.5, 0.25);
  }

  @Test
  public void testBigFraction() throws Exception {
    test("org.apache.commons.math3.fraction.BigFraction", 0.791, 0.703);
  }

  @Test
  public void testFraction() throws Exception {
    test("org.apache.commons.math3.fraction.Fraction", 0.91, 0.87);
  }

  @Test
  public void testLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Line", 0.0, 0.0);
  }

  @Test
  public void testSubLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine", 1.0, 0.25);
  }

  @Test
  public void testVector3D() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Vector3D", 1.0, 0.5);
  }

  @Test
  public void testS2Point() throws Exception {
    test("org.apache.commons.math3.geometry.spherical.twod.S2Point", 0.666, 0.666);
  }

  @Test
  public void testFieldRotation() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.FieldRotation", 0.5, 0.375);
  }
}
