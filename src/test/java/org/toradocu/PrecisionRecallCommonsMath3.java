package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;
import org.toradocu.testlib.TestCaseStats;

import java.util.Map;

public class PrecisionRecallCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONS_MATH_3_SRC =
      "src/test/resources/commons-math3-3.6.1-src/src/main/java";
  private static final String COMMONS_MATH_EXPECTED_DIR = "src/test/resources/CommonsMath-3.6.1/";

  @Test
  public void adaptiveStepsizeIntegratorTest() throws Exception {
    test("org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator");
  }

  @Test
  public void arithmeticUtilsTest() throws Exception {
    test("org.apache.commons.math3.util.ArithmeticUtils");
  }

  @Test
  public void bigFractionTest() throws Exception {
    test("org.apache.commons.math3.fraction.BigFraction");
  }

  @Test
  public void binaryMutationTest() throws Exception {
    test("org.apache.commons.math3.genetics.BinaryMutation");
  }

  @Test
  public void bitsStreamGeneratorTest() throws Exception {
    test("org.apache.commons.math3.random.BitsStreamGenerator");
  }

  @Test
  public void complexTest() throws Exception {
    test("org.apache.commons.math3.complex.Complex");
  }

  @Test
  public void cycleCrossoverTest() throws Exception {
    test("org.apache.commons.math3.genetics.CycleCrossover");
  }

  @Test
  public void dividedDifferenceInterpolatorTest() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator");
  }

  @Test
  public void fastMathTest() throws Exception {
    test("org.apache.commons.math3.util.FastMath");
  }

  @Test
  public void fieldRotationTest() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.FieldRotation");
  }

  @Test
  public void fractionTest() throws Exception {
    test("org.apache.commons.math3.fraction.Fraction");
  }

  @Test
  public void functionUtilsTest() throws Exception {
    test("org.apache.commons.math3.analysis.FunctionUtils");
  }

  @Test
  public void gaussianTest() throws Exception {
    test("org.apache.commons.math3.analysis.function.Gaussian");
  }

  @Test
  public void iterativeLegendreGaussIntegratorTest() throws Exception {
    test("org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator");
  }

  @Test
  public void lineTest() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Line");
  }

  @Test
  public void linearInterpolatorTest() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LinearInterpolator");
  }

  @Test
  public void loessInterpolatorTest() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator");
  }

  @Test
  public void logisticTest() throws Exception {
    test("org.apache.commons.math3.analysis.function.Logistic");
  }

  @Test
  public void polynomialFunctionNewtonFormTest() throws Exception {
    test("org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm");
  }

  @Test
  public void primesTest() throws Exception {
    test("org.apache.commons.math3.primes.Primes");
  }

  @Test
  public void randomAdaptorTest() throws Exception {
    test("org.apache.commons.math3.random.RandomAdaptor");
  }

  @Test
  public void randomDataGeneratorTest() throws Exception {
    test("org.apache.commons.math3.random.RandomDataGenerator");
  }

  @Test
  public void realVectorTest() throws Exception {
    test("org.apache.commons.math3.linear.RealVector");
  }

  @Test
  public void s2PointTest() throws Exception {
    test("org.apache.commons.math3.geometry.spherical.twod.S2Point");
  }

  @Test
  public void simpleCurveFitterTest() throws Exception {
    test("org.apache.commons.math3.fitting.SimpleCurveFitter");
  }

  @Test
  public void SimpsonIntegratorTest() throws Exception {
    test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator");
  }

  @Test
  public void stepFunctionTest() throws Exception {
    test("org.apache.commons.math3.analysis.function.StepFunction");
  }

  @Test
  public void subLineTest() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine");
  }

  @Test
  public void univariateSolverUtilsTest() throws Exception {
    test("org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils");
  }

  @Test
  public void vector3DTest() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Vector3D");
  }

  private Map<String, TestCaseStats> test(String targetClass) {
    Map<String, TestCaseStats> stats =
        PrecisionRecallTest.test(targetClass, COMMONS_MATH_3_SRC, COMMONS_MATH_EXPECTED_DIR);
    testSuiteStats.addTest(stats);
    return stats;
  }
}
