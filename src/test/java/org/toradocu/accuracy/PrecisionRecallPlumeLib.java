package org.toradocu.accuracy;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallPlumeLib extends AbstractPrecisionRecallTestSuite {

  private static final String PLUMELIB_SRC = "src/test/resources/src/plume-lib-1.1.0/java/src/";
  private static final String PLUMELIB_BIN = "src/test/resources/bin/plume-lib-1.1.0.jar";
  private static final String PLUMELIB_GOAL_DIR = "src/test/resources/goal-output/plume-lib-1.1.0/";

  public PrecisionRecallPlumeLib() {
    super(PLUMELIB_SRC, PLUMELIB_BIN, PLUMELIB_GOAL_DIR);
  }

  @Test
  public void testArraysMDE() throws Exception {
    test("plume.ArraysMDE", 0.888, 0.842, 0, 1, 0, 0);
  }

  @Test
  public void testRegexUtil() throws Exception {
    test("plume.RegexUtil", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testFileCompiler() throws Exception {
    test("plume.FileCompiler", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testWeakIdentityHashMap() throws Exception {
    test("plume.WeakIdentityHashMap", 1, 1, 1, 1, 0, 0);
  }

  @Test
  public void testTimeLimitProcess() throws Exception {
    test("plume.TimeLimitProcess", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testWeakHasherMap() throws Exception {
    test("plume.WeakHasherMap", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testEntryReader() throws Exception {
    test("plume.EntryReader", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testEntryReaderFlnReader() throws Exception {
    test("plume.EntryReader$FlnReader", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testMathMDE() throws Exception {
    test("plume.MathMDE", 1, 1, 1, 0, 0.933, 1);
  }

  @Test
  public void testStrTok() throws Exception {
    test("plume.StrTok", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testUtilMDE() throws Exception {
    test("plume.UtilMDE", 1, 1, 1, 1, 0.333, 0.333);
  }
}
