package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

/** Created by arianna on 07/08/17. */
public class AccuracyRandomCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSMATH_3_SRC =
      "src/test/resources/src/commons-math3-3.6.1-src/src/main/java";
  private static final String COMMONSMATH_3_BIN = "src/test/resources/bin/commons-math3-3.6.1.jar";
  private static final String COMMONSMATH_3_GOAL_DIR =
      "src/test/resources/goal-output/random/commons-math3-3.6.1/";

  public AccuracyRandomCommonsMath3() {
    super(COMMONSMATH_3_SRC, COMMONSMATH_3_BIN, COMMONSMATH_3_GOAL_DIR);
  }

  @Test
  public void testCurveFitter() throws Exception {
    test("org.apache.commons.math3.fitting.CurveFitter", 1, 1, 1, 1, 1, 1);
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
  public void testClusterer() throws Exception {
    test("org.apache.commons.math3.ml.clustering.Clusterer", 1, 1, 1, 1, 1, 1);
  }
}
