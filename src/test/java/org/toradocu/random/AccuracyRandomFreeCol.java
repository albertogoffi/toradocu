package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomFreeCol extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/freecol-0.11.6/src";
  private static final String EXPECTED = "src/test/resources/FreeCol/";

  @Test
  public void playerTest() throws Exception {
    test("net.sf.freecol.common.model.Player");
  }

  @Test
  public void buildingTypeTest() throws Exception {
    test("net.sf.freecol.common.model.BuildingType");
  }

  @Test
  public void indianNationTypeTest() throws Exception {
    test("net.sf.freecol.common.model.IndianNationType");
  }

  @Test
  public void unitLocationTest() throws Exception {
    test("net.sf.freecol.common.model.UnitLocation");
  }

  @Test
  public void featureTest() throws Exception {
    test("net.sf.freecol.common.model.Feature");
  }

  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
