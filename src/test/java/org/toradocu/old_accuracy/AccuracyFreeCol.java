package org.toradocu.old_accuracy;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyFreeCol extends AbstractPrecisionRecallTestSuite {

  private static final String FREECOL_SRC = "src/test/resources/src/freecol-0.11.6/src/";
  private static final String FREECOL_BIN = "src/test/resources/bin/freecol-0.11.6.jar";
  private static final String FREECOL_GOAL_DIR = "src/test/resources/goal-output/freecol-0.11.6/";

  public AccuracyFreeCol() {
    super(FREECOL_SRC, FREECOL_BIN, FREECOL_GOAL_DIR);
  }

  @Test
  public void testPlayer() throws Exception {
    test("net.sf.freecol.common.model.Player", 1, 1, 1, 1, 0.357, 0.714);
  }

  @Test
  public void testPlayerActivePredicate() throws Exception {
    test("net.sf.freecol.common.model.Player$ActivePredicate", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testUnit() throws Exception {
    test("net.sf.freecol.common.model.Unit", 1, 1, 0, 1, 0.083, 0.125);
  }
}
