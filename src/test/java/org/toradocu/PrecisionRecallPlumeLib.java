package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;
import org.toradocu.testlib.TestCaseStats;

import java.util.Map;

public class PrecisionRecallPlumeLib extends AbstractPrecisionRecallTestSuite {

  private static final String PLUMELIB_SRC = "src/test/resources/plume-lib-1.1.0/java/src";
  private static final String PLUMELIB_EXPECTED_DIR = "src/test/resources/PlumeLib-1.1.0/";

  @Test
  public void arraysMDETest() throws Exception {
    test("plume.ArraysMDE");
  }

  @Test
  public void fileCompilerTest() throws Exception {
    test("plume.FileCompiler");
  }

  @Test
  public void regexUtilTest() throws Exception {
    test("plume.RegexUtil");
  }

  @Test
  public void weakIdentityHashMap() throws Exception {
    test("plume.WeakIdentityHashMap");
  }

  private Map<String, TestCaseStats> test(String targetClass) {
    Map<String, TestCaseStats> stats =
        PrecisionRecallTest.test(targetClass, PLUMELIB_SRC, PLUMELIB_EXPECTED_DIR);
    testSuiteStats.addTest(stats);
    return stats;
  }
}
