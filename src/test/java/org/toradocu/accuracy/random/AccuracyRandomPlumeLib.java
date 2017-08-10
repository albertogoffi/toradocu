package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

/** Created by arianna on 07/08/17. */
public class AccuracyRandomPlumeLib extends AbstractPrecisionRecallTestSuite {
  private static final String PLUMELIB_SRC = "src/test/resources/src/plume-lib-1.1.0/java/src/";
  private static final String PLUMELIB_BIN = "src/test/resources/bin/plume-lib-1.1.0.jar";
  private static final String PLUMELIB_GOAL_DIR =
      "src/test/resources/goal-output/random/plume-lib-1.1.0/";

  public AccuracyRandomPlumeLib() {
    super(PLUMELIB_SRC, PLUMELIB_BIN, PLUMELIB_GOAL_DIR);
  }

  @Test
  public void testRandomSelector() throws Exception {
    test("plume.RandomSelector", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testPair() throws Exception {
    test("plume.Pair", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testFileCompiler() throws Exception {
    test("plume.FileCompiler", 1, 1, 1, 1, 1, 1);
  }
}
