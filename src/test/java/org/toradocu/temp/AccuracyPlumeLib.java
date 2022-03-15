package org.toradocu.temp;

import org.junit.Test;
import org.toradocu.testlib.TestgenTest;

public class AccuracyPlumeLib extends TestgenTest {
  private static final String PLUMELIB_SRC = "src/test/resources/src/plume-lib-1.1.0/java/src/";
  private static final String PLUMELIB_BIN = "src/test/resources/bin/plume-lib-1.1.0.jar";
  private static final String PLUMELIB_GOAL_DIR = "src/test/resources/goal-output/plume-lib-1.1.0/";

  public AccuracyPlumeLib() {
    super(PLUMELIB_SRC, PLUMELIB_BIN, PLUMELIB_GOAL_DIR);
  }

  @Test
  public void testFuzzyFloat() throws Exception {
    test("plume.FuzzyFloat");
  }

  @Test
  public void testMathMDE() throws Exception {
    test("plume.MathMDE");
  }

  @Test
  public void testEntryReader() throws Exception {
    test("plume.EntryReader");
  }

  @Test
  public void testWeakHasherMap() throws Exception {
    test("plume.WeakHasherMap");
  }

  @Test
  public void testRegexUtil() throws Exception {
    test("plume.RegexUtil");
  }

  @Test
  public void testWeakIdentityHashMap() throws Exception {
    test("plume.WeakIdentityHashMap");
  }

  @Test
  public void testTimeLimitProcess() throws Exception {
    test("plume.TimeLimitProcess");
  }

  @Test
  public void testEntryReaderFlnReader() throws Exception {
    test("plume.EntryReader$FlnReader");
  }

  @Test
  public void testUtilMDE() throws Exception {
    test("plume.UtilMDE");
  }

  @Test
  public void testArraysMDE() throws Exception {
    test("plume.ArraysMDE");
  }
}
