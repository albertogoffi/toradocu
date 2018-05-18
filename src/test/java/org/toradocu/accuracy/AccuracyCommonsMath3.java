package org.toradocu.accuracy;

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
  public void testGaussian() {
    test("org.apache.commons.math3.analysis.function.Gaussian", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testUnivariateSolverUtils() {
    test("org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils", 1, 1, 1, 1, 0, 1);
  }

  @Test
  public void testComplex() {
    test("org.apache.commons.math3.complex.Complex", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSimpleCurveFitter() {
    test("org.apache.commons.math3.fitting.SimpleCurveFitter", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testAdaptiveStepsizeIntegrator() {
    test("org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testPrimes() {
    test("org.apache.commons.math3.primes.Primes", 1, 1, 1, 1, 0.5, 0.5);
  }

  @Test
  public void testBitsStreamGenerator() {
    test("org.apache.commons.math3.random.BitsStreamGenerator", 1, 0.666, 1, 1, 1, 1);
  }

  @Test
  public void testRandomAdaptor() {
    test("org.apache.commons.math3.random.RandomAdaptor", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testRandomDataGenerator() {
    test("org.apache.commons.math3.random.RandomDataGenerator", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testArithmeticUtils() {
    test("org.apache.commons.math3.util.ArithmeticUtils", 1, 0.947, 1, 0.857, 0.857, 1);
  }

  @Test
  public void testFastMath() {
    test("org.apache.commons.math3.util.FastMath", 1, 1, 1, 1, 0.888, 1);
  }

  @Test
  public void testLogistic() {
    test("org.apache.commons.math3.analysis.function.Logistic", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testFunctionUtils() {
    test("org.apache.commons.math3.analysis.FunctionUtils", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testSimpsonIntegrator() {
    test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator", 0.5, 0.333, 1, 1, 1, 1);
  }

  @Test
  public void testStepFunction() {
    test("org.apache.commons.math3.analysis.function.StepFunction", 1, 0.5, 1, 1, 1, 1);
  }

  @Test
  public void testIterativeLegendreGaussIntegrator() {
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
  public void testLinearInterpolator() {
    test("org.apache.commons.math3.analysis.interpolation.LinearInterpolator", 1, 0.5, 1, 1, 1, 1);
  }

  @Test
  public void testLoessInterpolator() {
    test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator", 1, 0.25, 0, 1, 1, 1);
  }

  @Test
  public void testPolynomialFunctionNewtonForm() {
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
  public void testBinaryMutation() {
    test("org.apache.commons.math3.genetics.BinaryMutation", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testCycleCrossover() {
    test("org.apache.commons.math3.genetics.CycleCrossover", 1, 0.333, 1, 1, 1, 1);
  }

  @Test
  public void testBigFraction() {
    test("org.apache.commons.math3.fraction.BigFraction", 1, 0.625, 1, 1, 1, 1);
  }

  @Test
  public void testFraction() {
    test("org.apache.commons.math3.fraction.Fraction", 1, 0.857, 1, 1, 1, 1);
  }

  @Test
  public void testLine() {
    test("org.apache.commons.math3.geometry.euclidean.threed.Line", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSubLine() {
    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testVector3D() {
    test("org.apache.commons.math3.geometry.euclidean.threed.Vector3D", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testS2Point() {
    test("org.apache.commons.math3.geometry.spherical.twod.S2Point", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testFieldRotation() {
    test("org.apache.commons.math3.geometry.euclidean.threed.FieldRotation", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testRealVector() {
    test("org.apache.commons.math3.linear.RealVector", 1, 0.421, 1, 1, 1, 1);
  }

  @Test
  public void testDivideDifferenceInterpolator() {
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
  public void testUnivariateMultiStartOptimizer() {
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
  public void testRandomKey() {
    test("org.apache.commons.math3.genetics.RandomKey", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testAbstractSimplex() {
    test("org.apache.commons.math3.optimization.direct.AbstractSimplex", 0.5, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSum() {
    test("org.apache.commons.math3.stat.descriptive.summary.Sum", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testDerivativeStructure() {
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
  public void testBivariateGridInterpolator() {
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
  public void testLogisticDistribution() {
    test("org.apache.commons.math3.distribution.LogisticDistribution", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testAbstractListChromosome() {
    test("org.apache.commons.math3.genetics.AbstractListChromosome", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testInterval() {
    test("org.apache.commons.math3.geometry.euclidean.oned.Interval", 1, 1, 1, 1, 1, 0);
  }

  @Test
  public void testIntervalsSet() {
    test("org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testRegion() {
    test("org.apache.commons.math3.geometry.partitioning.Region", 1, 1, 1, 1, 0, 0);
  }

  @Test
  public void testVector() {
    test("org.apache.commons.math3.geometry.Vector", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSymmLQ() {
    test("org.apache.commons.math3.linear.SymmLQ", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testContinuousOutputModel() {
    test("org.apache.commons.math3.ode.ContinuousOutputModel", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testEmbeddedRungeKuttaFieldIntegrator() {
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
  public void testSimpleValueChecker() {
    test("org.apache.commons.math3.optimization.SimpleValueChecker", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testHaltonSequenceGenerator() {
    test("org.apache.commons.math3.random.HaltonSequenceGenerator", 0.75, 0.75, 1, 1, 1, 1);
  }

  @Test
  public void testDBSCANClusterer() {
    test("org.apache.commons.math3.stat.clustering.DBSCANClusterer", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testPSquarePercentile() {
    test("org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile", 0, 0, 1, 1, 1, 1);
  }

  @Test
  public void testSummaryStatistics() {
    test("org.apache.commons.math3.stat.descriptive.SummaryStatistics", 1, 1, 1, 1, 1, 0);
  }

  @Test
  public void testKthSelector() {
    test("org.apache.commons.math3.util.KthSelector", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void tesPreconditionedIterativeLinearSolver() {
    test("org.apache.commons.math3.linear.PreconditionedIterativeLinearSolver", 1, 1, 1, 1, 1, 1);
  }
}
