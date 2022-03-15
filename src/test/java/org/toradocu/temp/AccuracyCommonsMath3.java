package org.toradocu.temp;

import org.junit.Test;
import org.toradocu.testlib.TestgenTest;

public class AccuracyCommonsMath3 extends TestgenTest {

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
    test("org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizer");
  }

  @Test
  public void testRandomKey() throws Exception {
    test("org.apache.commons.math3.genetics.RandomKey");
  }

  @Test
  public void testAbstractSimplex() throws Exception {
    test("org.apache.commons.math3.optimization.direct.AbstractSimplex");
  }

  @Test
  public void testSum() throws Exception {
    test("org.apache.commons.math3.stat.descriptive.summary.Sum");
  }

  @Test
  public void testDerivativeStructure() throws Exception {
    test("org.apache.commons.math3.analysis.differentiation.DerivativeStructure");
  }

  @Test
  public void testBivariateGridInterpolator() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator");
  }

  @Test
  public void testLogisticDistribution() throws Exception {
    test("org.apache.commons.math3.distribution.LogisticDistribution");
  }

  @Test
  public void testInterval() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.oned.Interval");
  }

  @Test
  public void testIntervalsSet() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet");
  }

  @Test
  public void testRegion() throws Exception {
    test("org.apache.commons.math3.geometry.partitioning.Region");
  }

  @Test
  public void testVector() throws Exception {
    test("org.apache.commons.math3.geometry.Vector");
  }

  @Test
  public void testSymmLQ() throws Exception {
    test("org.apache.commons.math3.linear.SymmLQ");
  }

  @Test
  public void testContinuousOutputModel() throws Exception {
    test("org.apache.commons.math3.ode.ContinuousOutputModel");
  }

  @Test
  public void testEmbeddedRungeKuttaFieldIntegrator() throws Exception {
    test("org.apache.commons.math3.ode.nonstiff.EmbeddedRungeKuttaFieldIntegrator");
  }

  @Test
  public void testSimpleValueChecker() throws Exception {
    test("org.apache.commons.math3.optimization.SimpleValueChecker");
  }

  @Test
  public void testHaltonSequenceGenerator() throws Exception {
    test("org.apache.commons.math3.random.HaltonSequenceGenerator");
  }

  @Test
  public void testDBSCANClusterer() throws Exception {
    test("org.apache.commons.math3.stat.clustering.DBSCANClusterer");
  }

  @Test
  public void testPSquarePercentile() throws Exception {
    test("org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile");
  }

  @Test
  public void testSummaryStatistics() throws Exception {
    test("org.apache.commons.math3.stat.descriptive.SummaryStatistics");
  }

  @Test
  public void testKthSelector() throws Exception {
    test("org.apache.commons.math3.util.KthSelector");
  }

  @Test
  public void tesPreconditionedIterativeLinearSolver() throws Exception {
    test("org.apache.commons.math3.linear.PreconditionedIterativeLinearSolver");
  }

  @Test
  public void testSubLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine");
  }

  @Test
  public void testGaussian() throws Exception {
    test("org.apache.commons.math3.analysis.function.Gaussian");
  }

  @Test
  public void testUnivariateSolverUtils() throws Exception {
    test("org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils");
  }

  @Test
  public void testComplex() throws Exception {
    test("org.apache.commons.math3.complex.Complex");
  }

  @Test
  public void testSimpleCurveFitter() throws Exception {
    test("org.apache.commons.math3.fitting.SimpleCurveFitter");
  }

  @Test
  public void testAdaptiveStepsizeIntegrator() throws Exception {
    test("org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator");
  }

  @Test
  public void testPrimes() throws Exception {
    test("org.apache.commons.math3.primes.Primes");
  }

  @Test
  public void testBitsStreamGenerator() throws Exception {
    test("org.apache.commons.math3.random.BitsStreamGenerator");
  }

  @Test
  public void testRandomAdaptor() throws Exception {
    test("org.apache.commons.math3.random.RandomAdaptor");
  }

  @Test
  public void testRandomDataGenerator() throws Exception {
    test("org.apache.commons.math3.random.RandomDataGenerator");
  }

  @Test
  public void testArithmeticUtils() throws Exception {
    test("org.apache.commons.math3.util.ArithmeticUtils");
  }

  @Test
  public void testFastMath() throws Exception {
    test("org.apache.commons.math3.util.FastMath");
  }

  @Test
  public void testLogistic() throws Exception {
    test("org.apache.commons.math3.analysis.function.Logistic");
  }

  @Test
  public void testFunctionUtils() throws Exception {
    test("org.apache.commons.math3.analysis.FunctionUtils");
  }

  @Test
  public void testSimpsonIntegrator() throws Exception {
    test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator");
  }

  @Test
  public void testStepFunction() throws Exception {
    test("org.apache.commons.math3.analysis.function.StepFunction");
  }

  @Test
  public void testIterativeLegendreGaussIntegrator() throws Exception {
    test("org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator");
  }

  @Test
  public void testLinearInterpolator() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LinearInterpolator");
  }

  @Test
  public void testLoessInterpolator() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator");
  }

  @Test
  public void testPolynomialFunctionNewtonForm() throws Exception {
    test("org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm");
  }

  @Test
  public void testBinaryMutation() throws Exception {
    test("org.apache.commons.math3.genetics.BinaryMutation");
  }

  @Test
  public void testCycleCrossover() throws Exception {
    test("org.apache.commons.math3.genetics.CycleCrossover");
  }

  @Test
  public void testBigFraction() throws Exception {
    test("org.apache.commons.math3.fraction.BigFraction");
  }

  @Test
  public void testFraction() throws Exception {
    test("org.apache.commons.math3.fraction.Fraction");
  }

  @Test
  public void testLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Line");
  }

  @Test
  public void testVector3D() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.Vector3D");
  }

  @Test
  public void testS2Point() throws Exception {
    test("org.apache.commons.math3.geometry.spherical.twod.S2Point");
  }

  @Test
  public void testFieldRotation() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.FieldRotation");
  }

  @Test
  public void testRealVector() throws Exception {
    test("org.apache.commons.math3.linear.RealVector");
  }

  @Test
  public void testDivideDifferenceInterpolator() throws Exception {
    test("org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator");
  }

  @Test
  public void testBracketingNthOrderBrentSolver() throws Exception {
    test("org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver");
  }
}
