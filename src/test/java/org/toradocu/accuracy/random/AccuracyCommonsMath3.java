package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSMATH_3_SRC =
      "src/test/resources/src/commons-math3-3.6.1-src/src/main/java";
  private static final String COMMONSMATH_3_BIN = "src/test/resources/bin/commons-math3-3.6.1.jar";
  private static final String COMMONSMATH_3_GOAL_DIR =
      "src/test/resources/goal-output/random/commons-math3-3.6.1/";

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
  public void testAbstractListChromosome() throws Exception {
    test("org.apache.commons.math3.genetics.AbstractListChromosome", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testInterval() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.oned.Interval", 1, 1, 1, 1, 1, 0);
  }

  @Test
  public void testIntervalsSet() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.oned.IntervalsSet", 1, 1, 1, 0.5, 1, 1);
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
    test("org.apache.commons.math3.random.HaltonSequenceGenerator", 0.666, 0.5, 1, 1, 1, 1);
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

  //  @Test
  //  public void testSubLine() throws Exception {
  //    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine", 1, 1, 1, 1, 1, 1);
  //  }

}
