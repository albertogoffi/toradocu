package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;
import org.toradocu.testlib.TestCaseStats;

import java.util.Map;

public class PrecisionRecallFreeCol extends AbstractPrecisionRecallTestSuite {

  private static final String FREECOL_SRC =
      "src/test/resources/freecol-0.11.6/src";
  private static final String FREECOL_EXPECTED_DIR = "src/test/resources/FreeCol/";

  @Test
  public void playerTest() throws Exception {
    test("net.sf.freecol.common.model.Player");
  }

  private Map<String, TestCaseStats> test(String targetClass) {
    Map<String, TestCaseStats> stats =
        PrecisionRecallTest.test(targetClass, FREECOL_SRC, FREECOL_EXPECTED_DIR);
    testSuiteStats.addTest(stats);
    return stats;
  }
}
