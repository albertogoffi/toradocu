package org.toradocu.accuracy.random;

import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

/** Created by arianna on 07/08/17. */
public class AccuracyRandomFreeCol extends AbstractPrecisionRecallTestSuite {

  private static final String FREECOL_SRC = "src/test/resources/src/freecol-0.11.6/src/";
  private static final String FREECOL_BIN = "src/test/resources/bin/freecol-0.11.6.jar";
  private static final String FREECOL_GOAL_DIR =
      "src/test/resources/goal-output/random/freecol-0.11.6/";

  public AccuracyRandomFreeCol() {
    super(FREECOL_SRC, FREECOL_BIN, FREECOL_GOAL_DIR);
  }
}
