package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyPlumeLib extends AbstractPrecisionRecallTestSuite {
  private static final String PLUMELIB_SRC = "src/test/resources/src/plume-lib-1.1.0/java/src/";
  private static final String PLUMELIB_BIN = "src/test/resources/bin/plume-lib-1.1.0.jar";
  private static final String PLUMELIB_GOAL_DIR =
      "src/test/resources/goal-output/random/plume-lib-1.1.0/";

  public AccuracyPlumeLib() {
    super(PLUMELIB_SRC, PLUMELIB_BIN, PLUMELIB_GOAL_DIR);
  }

  @Test
  public void testFuzzyFloat() throws Exception {
    test("plume.FuzzyFloat", 1, 1, 1, 1, 1, 0.8);
  }
}
