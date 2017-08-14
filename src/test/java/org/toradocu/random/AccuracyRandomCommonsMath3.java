package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomCommonsMath3 extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/commons-math3-3.6.1-src/src/main/java";
  private static final String EXPECTED = "src/test/resources/CommonsMath-3.6.1/";

  @Test
  public void univariateMultiStartOptimizerTest() throws Exception {
    test("org.apache.commons.math3.optimization.univariate.UnivariateMultiStartOptimizer");
  }

  @Test
  public void randomKeyTest() throws Exception {
    test("org.apache.commons.math3.genetics.RandomKey");
  }

  @Test
  public void abstractSimplexTest() throws Exception {
    test("org.apache.commons.math3.optimization.direct.AbstractSimplex");
  }

  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
