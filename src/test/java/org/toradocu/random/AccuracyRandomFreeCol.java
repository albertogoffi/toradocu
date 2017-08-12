package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomFreeCol extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/freecol-0.11.6/src";
  private static final String EXPECTED = "src/test/resources/FreeCol/";

  @Test
  public void locatableTest() throws Exception {
    test("net.sf.freecol.common.model.Locatable");
  }

  @Test
  public void cashInTreasureTrainMessageTest() throws Exception {
    test("net.sf.freecol.common.networking.CashInTreasureTrainMessage");
  }

  @Test
  public void moveToMessageTest() throws Exception {
    test("net.sf.freecol.common.networking.MoveToMessage");
  }

  private void test(String clazz) {
    PrecisionRecallTest.test(clazz, SRC, EXPECTED);
  }
}
