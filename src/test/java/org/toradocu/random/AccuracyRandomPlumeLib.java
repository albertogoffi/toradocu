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
  public void testFuzzyFloat() {
    test("plume.FuzzyFloat");
  }

  @Test
  public void testMathMDE() {
    test("plume.MathMDE");
  }

  @Test
  public void testEntryReader() {
    test("plume.EntryReader");
  }

  @Test
  public void testWeakHasherMap() {
    test("plume.WeakHasherMap");
  }

  @Test
  public void testRegexUtil() {
    test("plume.RegexUtil");
  }

  @Test
  public void testWeakIdentityHashMap() {
    test("plume.WeakIdentityHashMap");
  }

  @Test
  public void testTimeLimitProcess() {
    test("plume.TimeLimitProcess");
  }

//  @Test // Test ignored: support for inner classes is not available in Toradocu 0.1.
  public void testEntryReaderFlnReader() {
    test("plume.EntryReader$FlnReader");
  }

  @Test
  public void testStrTok() {
    test("plume.StrTok");
  }

  @Test
  public void testUtilMDE() {
    test("plume.UtilMDE");
  }

  @Test
  public void testArraysMDE() {
    test("plume.ArraysMDE");
  }

  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
