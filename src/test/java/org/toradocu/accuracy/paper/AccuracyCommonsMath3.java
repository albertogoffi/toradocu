package org.toradocu.accuracy.paper;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSMATH_3_SRC =
      "src/test/resources/src/commons-math3-3.6.1-src/src/main/java";
  private static final String COMMONSMATH_3_BIN = "src/test/resources/bin/commons-math3-3.6.1.jar";
  private static final String COMMONSMATH_3_GOAL_DIR =
      "src/test/resources/goal-output/commons-math3-3.6.1/";

  public AccuracyCommonsMath3() {
    super(COMMONSMATH_3_SRC, COMMONSMATH_3_BIN, COMMONSMATH_3_GOAL_DIR);
  }

  @Test
  public void testUnivariateMultiStartOptimizer() throws Exception {
    test(
        "org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizer",
        1,
        1,
        0,
        1,
        1,
        1);
  }

  @Test
  public void testRandomKey() throws Exception {
    test("org.apache.commons.math3.genetics.RandomKey", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testAbstractSimplex() throws Exception {
    test("org.apache.commons.math3.optimization.direct.AbstractSimplex", 0.5, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSum() throws Exception {
    test("org.apache.commons.math3.stat.descriptive.summary.Sum", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testDerivativeStructure() throws Exception {
    test(
        "org.apache.commons.math3.analysis.differentiation.DerivativeStructure",
        1,
        0.5,
        1,
        1,
        1,
        1);
  }

  @Test
  public void testBivariateGridInterpolator() throws Exception {
    test(
        "org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator",
        1,
        1,
        1,
        1,
        1,
        1);
  }

  @Test
  public void testLogisticDistribution() throws Exception {
    test("org.apache.commons.math3.distribution.LogisticDistribution", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testInterval() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.oned.Interval", 1, 1, 1, 1, 1, 0);
  }

  @Test
  public void testIntervalsSet() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testRegion() throws Exception {
    test("org.apache.commons.math3.geometry.partitioning.Region", 1, 1, 1, 1, 0, 0);
  }

  @Test
  public void testVector() throws Exception {
    test("org.apache.commons.math3.geometry.Vector", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSymmLQ() throws Exception {
    test("org.apache.commons.math3.linear.SymmLQ", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testContinuousOutputModel() throws Exception {
    test("org.apache.commons.math3.ode.ContinuousOutputModel", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testEmbeddedRungeKuttaFieldIntegrator() throws Exception {
    test(
        "org.apache.commons.math3.ode.nonstiff.EmbeddedRungeKuttaFieldIntegrator",
        1,
        1,
        1,
        1,
        1,
        1);
  }

  @Test
  public void testSimpleValueChecker() throws Exception {
    test("org.apache.commons.math3.optimization.SimpleValueChecker", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testHaltonSequenceGenerator() throws Exception {
    test("org.apache.commons.math3.random.HaltonSequenceGenerator", 0.75, 0.75, 1, 1, 1, 1);
  }

  @Test
  public void testDBSCANClusterer() throws Exception {
    test("org.apache.commons.math3.stat.clustering.DBSCANClusterer", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testPSquarePercentile() throws Exception {
    test("org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile", 0, 0, 1, 1, 1, 1);
  }

  @Test
  public void testSummaryStatistics() throws Exception {
    test("org.apache.commons.math3.stat.descriptive.SummaryStatistics", 1, 1, 1, 1, 1, 0);
  }

  @Test
  public void testKthSelector() throws Exception {
    test("org.apache.commons.math3.util.KthSelector", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void tesPreconditionedIterativeLinearSolver() throws Exception {
    test("org.apache.commons.math3.linear.PreconditionedIterativeLinearSolver", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSubLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testGaussian() throws Exception {
    test("org.apache.commons.math3.analysis.function.Gaussian", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testUnivariateSolverUtils() throws Exception {
    test("org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils", 1, 1, 1, 1, 0, 1);
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
    test("org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testPrimes() throws Exception {
    test("org.apache.commons.math3.primes.Primes", 1, 1, 1, 1, 0.5, 0.5);
  }

  @Test
  public void testBitsStreamGenerator() throws Exception {
    test("org.apache.commons.math3.random.BitsStreamGenerator", 1, 0.666, 1, 1, 1, 1);
  }

  @Test
  public void testRandomAdaptor() throws Exception {
    test("org.apache.commons.math3.random.RandomAdaptor", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testRandomDataGenerator() throws Exception {
    test("org.apache.commons.math3.random.RandomDataGenerator", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testArithmeticUtils() throws Exception {
    test("org.apache.commons.math3.util.ArithmeticUtils", 1, 0.947, 1, 0.857, 0.857, 1);
  }

  @Test
  public void testFastMath() throws Exception {
    test("org.apache.commons.math3.util.FastMath", 1, 1, 1, 1, 0.888, 1);
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
    test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator", 0.5, 0.333, 1, 1, 1, 1);
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
    test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator", 1, 0.25, 0, 1, 1, 1);
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
    test("org.apache.commons.math3.fraction.BigFraction", 1, 0.625, 1, 1, 1, 1);
  }

  @Test
  public void testFraction() throws Exception {
    test("org.apache.commons.math3.fraction.Fraction", 1, 0.857, 1, 1, 1, 1);
  }

  @Test
  public void testLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Line", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testVector3D() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Vector3D", 1, 0, 1, 1, 1, 1);
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
    test("org.apache.commons.math3.linear.RealVector", 1, 0.421, 1, 1, 1, 1);
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

  @Test
  public void testBracketingNthOrderBrentSolver() throws Exception {
    test(
        "org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver",
        1,
        1,
        1,
        1,
        1,
        1);
  }
}
