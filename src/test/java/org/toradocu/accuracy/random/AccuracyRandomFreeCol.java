package org.toradocu.accuracy.random;

import org.junit.Test;
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

  @Test
  public void testPlayer() throws Exception {
    test("net.sf.freecol.common.model.Player", 1, 1, 0, 1, 0.214, 0.5);
  }

  @Test
  public void testBuildingType() throws Exception {
    test("net.sf.freecol.common.model.BuildingType", 1, 1, 0, 1, 0, 0);
  }

  @Test
  public void testIndianNationType() throws Exception {
    test("net.sf.freecol.common.model.IndianNationType", 1, 1, 1, 1, 1, 1);
  }
}
