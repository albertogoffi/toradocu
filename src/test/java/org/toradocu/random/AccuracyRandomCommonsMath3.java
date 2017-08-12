package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String SRC =
      "src/test/resources/commons-math3-3.6.1-src/src/main/java";
  private static final String EXPECTED =
      "src/test/resources/CommonsMath-3.6.1/";

  @Test
  public void curveFitterTest() throws Exception {
    test("org.apache.commons.math3.fitting.CurveFitter");
  }

  @Test
  public void univariateMultiStartOptimizerTest() throws Exception {
    test("org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizer");
  }

  @Test
  public void clustererTest() throws Exception {
    test("org.apache.commons.math3.ml.clustering.Clusterer");
  }

  private void test(String clazz) {
    PrecisionRecallTest.test(clazz, SRC, EXPECTED);
  }
}
