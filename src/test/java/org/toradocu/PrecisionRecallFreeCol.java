package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallFreeCol extends AbstractPrecisionRecallTestSuite {

  private static final String FREECOL_SRC = "src/test/resources/src/freecol-0.11.6/src/";
  private static final String FREECOL_BIN = "src/test/resources/bin/freecol-0.11.6.jar";
  private static final String FREECOL_GOAL_DIR = "src/test/resources/goal-output/freecol-0.11.6/";

  public PrecisionRecallFreeCol() {
    super(FREECOL_SRC, FREECOL_BIN, FREECOL_GOAL_DIR);
  }

  @Test
  public void testPlayer() throws Exception {
    test("net.sf.freecol.common.model.Player", 1, 1, 1, 1, 0.466, 0.411);
  }
}
