package org.toradocu.temp;

import org.junit.Test;
import org.toradocu.testlib.TestgenTest;

public class AccuracyFreeCol extends TestgenTest {

  private static final String FREECOL_SRC = "src/test/resources/src/freecol-0.11.6/src/";
  private static final String FREECOL_BIN = "src/test/resources/bin/freecol-0.11.6.jar";
  private static final String FREECOL_GOAL_DIR = "src/test/resources/goal-output/freecol-0.11.6/";

  public AccuracyFreeCol() {
    super(FREECOL_SRC, FREECOL_BIN, FREECOL_GOAL_DIR);
  }

  @Test
  public void testPlayer() {
    test("net.sf.freecol.common.model.Player");
  }

  @Test
  public void testPlayerActivePredicate() {
    test("net.sf.freecol.common.model.Player$ActivePredicate");
  }

  @Test
  public void testUnit() {
    test("net.sf.freecol.common.model.Unit");
  }
}
