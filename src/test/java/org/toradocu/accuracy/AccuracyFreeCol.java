package org.toradocu.accuracy;

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
  public void testPlayer() {
    test("net.sf.freecol.common.model.Player", 1, 1, 0, 1, 0.5, 0.571);
  }

  @Test
  public void testPlayerActivePredicate() {
    test("net.sf.freecol.common.model.Player$ActivePredicate", 1, 1, 1, 1, 0, 0);
  }

  @Test
  public void testUnit() {
    test("net.sf.freecol.common.model.Unit", 1, 0.333, 0, 1, 0.1, 0.125);
  }
}
