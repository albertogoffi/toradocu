package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSMATH_3_SRC =
      "src/test/resources/src/commons-math3-3.6.1-src/src/main/java";
  private static final String COMMONSMATH_3_BIN = "src/test/resources/bin/commons-math3-3.6.1.jar";
  private static final String COMMONSMATH_3_GOAL_DIR =
      "src/test/resources/goal-output/commons-math3-3.6.1/";

  public PrecisionRecallCommonsMath3() {
    super(COMMONSMATH_3_SRC, COMMONSMATH_3_BIN, COMMONSMATH_3_GOAL_DIR);
  }

  @Test
  public void testGaussian() throws Exception {
    test("org.apache.commons.math3.analysis.function.Gaussian", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testUnivariateSolverUtils() throws Exception {
    test("org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils", 1, 1, 1, 0, 1, 1);
  }

  @Test
  public void testComplex() throws Exception {
    test("org.apache.commons.math3.complex.Complex", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSimpleCurveFitter() throws Exception {
    test("org.apache.commons.math3.fitting.SimpleCurveFitter", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testAdaptiveStepsizeIntegrator() throws Exception {
    test("org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator", 1, 1, 1, 0.8, 1, 1);
  }

  @Test
  public void testPrimes() throws Exception {
    test("org.apache.commons.math3.primes.Primes", 1, 1, 1, 1, 0, 0);
  }

  @Test
  public void testBitsStreamGenerator() throws Exception {
    test("org.apache.commons.math3.random.BitsStreamGenerator", 1, 0.666, 1, 1, 1, 1);
  }

  @Test
  public void testRandomAdaptor() throws Exception {
    test("org.apache.commons.math3.random.RandomAdaptor", 1, 1, 1, 0.5, 1, 1);
  }

  @Test
  public void testRandomDataGenerator() throws Exception {
    test("org.apache.commons.math3.random.RandomDataGenerator", 1, 0.941, 1, 1, 1, 1);
  }

  @Test
  public void testArithmeticUtils() throws Exception {
    test("org.apache.commons.math3.util.ArithmeticUtils", 1, 0.947, 1, 0.857, 1, 0.666);
  }

  @Test
  public void testFastMath() throws Exception {
    test("org.apache.commons.math3.util.FastMath", 1, 1, 1, 1, 1, 0.444);
  }

  @Test
  public void testLogistic() throws Exception {
    test("org.apache.commons.math3.analysis.function.Logistic", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testFunctionUtils() throws Exception {
    test("org.apache.commons.math3.analysis.FunctionUtils", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testSimpsonIntegrator() throws Exception {
    test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator", 1, 0.666, 1, 1, 1, 1);
  }

  @Test
  public void testStepFunction() throws Exception {
    test("org.apache.commons.math3.analysis.function.StepFunction", 1, 0.5, 1, 1, 1, 1);
  }

  @Test
  public void testIterativeLegendreGaussIntegrator() throws Exception {
    test(
        "org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator",
        1,
        0.5,
        1,
        1,
        1,
        1);
  }

  @Test
  public void testLinearInterpolator() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LinearInterpolator", 1, 0.5, 1, 1, 1, 1);
  }

  @Test
  public void testLoessInterpolator() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator", 1, 0.125, 1, 1, 1, 1);
  }

  @Test
  public void testPolynomialFunctionNewtonForm() throws Exception {
    test(
        "org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm",
        1,
        0.666,
        1,
        1,
        1,
        1);
  }

  @Test
  public void testBinaryMutation() throws Exception {
    test("org.apache.commons.math3.genetics.BinaryMutation", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testCycleCrossover() throws Exception {
    test("org.apache.commons.math3.genetics.CycleCrossover", 1, 0.333, 1, 1, 1, 1);
  }

  @Test
  public void testBigFraction() throws Exception {
    test("org.apache.commons.math3.fraction.BigFraction", 0.714, 0.625, 1, 1, 1, 1);
  }

  @Test
  public void testFraction() throws Exception {
    test("org.apache.commons.math3.fraction.Fraction", 0.857, 0.857, 1, 1, 1, 1);
  }

  @Test
  public void testLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Line", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testSubLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testVector3D() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Vector3D", 1, 0.333, 1, 1, 1, 1);
  }

  @Test
  public void testS2Point() throws Exception {
    test("org.apache.commons.math3.geometry.spherical.twod.S2Point", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testFieldRotation() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.FieldRotation", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testRealVector() throws Exception {
    test("org.apache.commons.math3.linear.RealVector", 1, 0.348, 1, 1, 1, 1);
  }

  @Test
  public void testDivideDifferenceInterpolator() throws Exception {
    test(
        "org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator",
        1,
        0.5,
        1,
        1,
        1,
        1);
  }
}
