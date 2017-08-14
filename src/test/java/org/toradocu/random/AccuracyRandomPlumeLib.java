package org.toradocu.random;

import java.util.Map;
import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;
import org.toradocu.testlib.TestCaseStats;

public class AccuracyRandomPlumeLib extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/plume-lib-1.1.0/java/src";
  private static final String EXPECTED = "src/test/resources/PlumeLib-1.1.0/";

  @Test
  public void fuzzyFloatTest() throws Exception {
    test("plume.FuzzyFloat");
  }

  @Test
  public void entryReaderTest() throws Exception {
    test("plume.EntryReader");
  }

  @Test
  public void mathMDETest() throws Exception {
    test("plume.MathMDE");
  }


  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
