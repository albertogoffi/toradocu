package org.toradocu.old_accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyFreeCol extends AbstractPrecisionRecallTestSuite {

  private static final String FREECOL_SRC = "src/test/resources/src/freecol-0.11.6/src/";
  private static final String FREECOL_BIN = "src/test/resources/bin/freecol-0.11.6.jar";
  private static final String FREECOL_GOAL_DIR =
      "src/test/resources/goal-output/random/freecol-0.11.6/";

  public AccuracyFreeCol() {
    super(FREECOL_SRC, FREECOL_BIN, FREECOL_GOAL_DIR);
  }

  @Test
  public void testPlayer() throws Exception {
    test("net.sf.freecol.common.model.Player", 1, 1, 1, 1, 0.357, 0.714);
  }

  @Test
  public void testBuildingType() throws Exception {
    test("net.sf.freecol.common.model.BuildingType", 1, 1, 1, 1, 0, 0);
  }

  @Test
  public void testIndianNationType() throws Exception {
    test("net.sf.freecol.common.model.IndianNationType", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testUnitLocation() throws Exception {
    test("net.sf.freecol.common.model.UnitLocation", 1, 1, 1, 1, 1, 0);
  }

  @Test
  public void testFeature() throws Exception {
    test("net.sf.freecol.common.model.Feature", 1, 1, 1, 1, 1, 1);
  }
  //
  //  @Test
  //  public void testImprovementMission() throws Exception {
  //    test("net.sf.freecol.common.model.mission.ImprovementMission", 1, 1, 1, 1, 1, 1);
  //  }
  //
  //  @Test
  //  public void testClearSpecialityMessage() throws Exception {
  //    test("net.sf.freecol.common.networking.ClearSpecialityMessage", 1, 1, 1, 1, 1, 1);
  //  }
  //
  //  @Test
  //  public void testCurrentPlayerNetworkRequestHandler() throws Exception {
  //    test("net.sf.freecol.common.networking.CurrentPlayerNetworkRequestHandler", 1, 1, 1, 1, 1, 1);
  //  }
  //
  //  @Test
  //  public void testDisbandUnitMessage() throws Exception {
  //    test("net.sf.freecol.common.networking.DisbandUnitMessage", 1, 1, 1, 1, 1, 1);
  //  }
  //
  //  @Test
  //  public void testLoadingSavegameDialog() throws Exception {
  //    test("net.sf.freecol.client.gui.panel.LoadingSavegameDialog", 1, 1, 1, 1, 1, 1);
  //  }
  //
  //  @Test
  //  public void testHighScore() throws Exception {
  //    test("net.sf.freecol.common.model.HighScore", 1, 1, 1, 1, 1, 1);
  //  }
  //
  //  @Test
  //  public void testPioneeringMission() throws Exception {
  //    test("net.sf.freecol.server.ai.mission.PioneeringMission", 1, 1, 1, 1, 1, 1);
  //  }
  //
  //  @Test
  //  public void testMissionaryMessage() throws Exception {
  //    test("net.sf.freecol.common.networking.MissionaryMessage", 1, 1, 1, 1, 1, 1);
  //  }
  //
  //  @Test
  //  public void testAIObject() throws Exception {
  //    test("net.sf.freecol.server.ai.AIObject", 1, 1, 1, 1, 1, 1);
  //  }
}
