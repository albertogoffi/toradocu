package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

/** Created by arianna on 07/08/17. */
public class AccuracyRandomCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSCOLLECTIONS_4_SRC =
      "src/test/resources/src/commons-collections4-4.1-src/src/main/java/";
  private static final String COMMONSCOLLECTIONS_4_BIN =
      "src/test/resources/bin/commons-collections4-4.1.jar";
  private static final String COMMONSCOLLECTIONS_4_GOAL_DIR =
      "src/test/resources/goal-output/commons-collections4-4.1/";

  public AccuracyRandomCommonsCollections4() {
    super(COMMONSCOLLECTIONS_4_SRC, COMMONSCOLLECTIONS_4_BIN, COMMONSCOLLECTIONS_4_GOAL_DIR);
  }

  @Test
  public void testIfClosure() throws Exception {
    test("org.apache.commons.collections4.functors.IfClosure", 1, 1, 1, 1, 1, 1);
  }
}
