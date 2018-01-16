package org.toradocu.accuracy.random;

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
  public void testSubLine() throws Exception {
    test("org.apache.commons.math3.geometry.euclidean.threed.SubLine", 1, 1, 1, 1, 1, 1);
  }
}
