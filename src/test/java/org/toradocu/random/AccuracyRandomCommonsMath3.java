package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/commons-math3-3.6.1-src/src/main/java";
  private static final String EXPECTED = "src/test/resources/CommonsMath-3.6.1/";

  @Test
  public void univariateMultiStartOptimizerTest() {
    test("org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizer");
  }

  @Test
  public void randomKeyTest() {
    test("org.apache.commons.math3.genetics.RandomKey");
  }

  @Test
  public void abstractSimplexTest() {
    test("org.apache.commons.math3.optimization.direct.AbstractSimplex");
  }

  @Test
  public void sumTest() {
    test("org.apache.commons.math3.stat.descriptive.summary.Sum");
  }

  @Test
  public void testDerivativeStructure() {
    test("org.apache.commons.math3.analysis.differentiation.DerivativeStructure");
  }

  @Test
  public void testBivariateGridInterpolator() {
    test("org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator");
  }

  @Test
  public void testLogisticDistribution() {
    test("org.apache.commons.math3.distribution.LogisticDistribution");
  }

  @Test
  public void testAbstractListChromosome() {
    test("org.apache.commons.math3.genetics.AbstractListChromosome");
  }

  @Test
  public void testInterval() {
    test("org.apache.commons.math3.geometry.euclidean.oned.Interval");
  }

  @Test
  public void testIntervalsSet() {
    test("org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet");
  }

  @Test
  public void testRegion() {
    test("org.apache.commons.math3.geometry.partitioning.Region");
  }

  @Test
  public void testVector() {
    test("org.apache.commons.math3.geometry.Vector");
  }

  @Test
  public void testSymmLQ() {
    test("org.apache.commons.math3.linear.SymmLQ");
  }

  @Test
  public void testContinuousOutputModel() {
    test("org.apache.commons.math3.ode.ContinuousOutputModel");
  }

  @Test
  public void testEmbeddedRungeKuttaFieldIntegrator() {
    test("org.apache.commons.math3.ode.nonstiff.EmbeddedRungeKuttaFieldIntegrator");
  }

  @Test
  public void testSimpleValueChecker() {
    test("org.apache.commons.math3.optimization.SimpleValueChecker");
  }

  @Test
  public void testHaltonSequenceGenerator() {
    test("org.apache.commons.math3.random.HaltonSequenceGenerator");
  }

  @Test
  public void testDBSCANClusterer() {
    test("org.apache.commons.math3.stat.clustering.DBSCANClusterer");
  }

  @Test
  public void testPSquarePercentile() {
    test("org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile");
  }

  @Test
  public void testSummaryStatistics() {
    test("org.apache.commons.math3.stat.descriptive.SummaryStatistics");
  }

  @Test
  public void testKthSelector() {
    test("org.apache.commons.math3.util.KthSelector");
  }

  @Test
  public void tesPreconditionedIterativeLinearSolver() {
    test("org.apache.commons.math3.linear.PreconditionedIterativeLinearSolver");
  }

  @Test
  public void testSubLine() {
    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine");
  }

  @Test
  public void testGaussian() {
    test("org.apache.commons.math3.analysis.function.Gaussian");
  }

  @Test
  public void testUnivariateSolverUtils() {
    test("org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils");
  }

  @Test
  public void testComplex() {
    test("org.apache.commons.math3.complex.Complex");
  }

  @Test
  public void testSimpleCurveFitter() {
    test("org.apache.commons.math3.fitting.SimpleCurveFitter");
  }

  @Test
  public void testAdaptiveStepsizeIntegrator() {
    test("org.apache.commons.math3.ode.nonstiff.AdaptiveStepsizeIntegrator");
  }

  @Test
  public void testPrimes() {
    test("org.apache.commons.math3.primes.Primes");
  }

  @Test
  public void testBitsStreamGenerator() {
    test("org.apache.commons.math3.random.BitsStreamGenerator");
  }

  @Test
  public void testRandomAdaptor() {
    test("org.apache.commons.math3.random.RandomAdaptor");
  }

  @Test
  public void testRandomDataGenerator() {
    test("org.apache.commons.math3.random.RandomDataGenerator");
  }

  @Test
  public void testArithmeticUtils() {
    test("org.apache.commons.math3.util.ArithmeticUtils");
  }

  @Test
  public void testFastMath() {
    test("org.apache.commons.math3.util.FastMath");
  }

  @Test
  public void testLogistic() {
    test("org.apache.commons.math3.analysis.function.Logistic");
  }

  @Test
  public void testFunctionUtils() {
    test("org.apache.commons.math3.analysis.FunctionUtils");
  }

  @Test
  public void testSimpsonIntegrator() {
    test("org.apache.commons.math3.analysis.integration.SimpsonIntegrator");
  }

  @Test
  public void testStepFunction() {
    test("org.apache.commons.math3.analysis.function.StepFunction");
  }

  @Test
  public void testIterativeLegendreGaussIntegrator() {
    test("org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator");
  }

  @Test
  public void testLinearInterpolator() {
    test("org.apache.commons.math3.analysis.interpolation.LinearInterpolator");
  }

  @Test
  public void testLoessInterpolator() {
    test("org.apache.commons.math3.analysis.interpolation.LoessInterpolator");
  }

  @Test
  public void testPolynomialFunctionNewtonForm() {
    test("org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm");
  }

  @Test
  public void testBinaryMutation() {
    test("org.apache.commons.math3.genetics.BinaryMutation");
  }

  @Test
  public void testCycleCrossover() {
    test("org.apache.commons.math3.genetics.CycleCrossover");
  }

  @Test
  public void testBigFraction() {
    test("org.apache.commons.math3.fraction.BigFraction");
  }

  @Test
  public void testFraction() {
    test("org.apache.commons.math3.fraction.Fraction");
  }

  @Test
  public void testLine() {
    test("org.apache.commons.math3.geometry.euclidean.threed.Line");
  }

  @Test
  public void testVector3D() {
    test("org.apache.commons.math3.geometry.euclidean.threed.Vector3D");
  }

  @Test
  public void testS2Point() {
    test("org.apache.commons.math3.geometry.spherical.twod.S2Point");
  }

  @Test
  public void testFieldRotation() {
    test("org.apache.commons.math3.geometry.euclidean.threed.FieldRotation");
  }

  @Test
  public void testRealVector() {
    test("org.apache.commons.math3.linear.RealVector");
  }

  @Test
  public void testDivideDifferenceInterpolator() {
    test("org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator");
  }

  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
