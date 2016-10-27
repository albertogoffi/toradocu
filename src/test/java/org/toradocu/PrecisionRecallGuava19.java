package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallGuava19 extends AbstractPrecisionRecallTestSuite {

  private static final String GUAVA_19_SRC = "src/test/resources/src/guava-19.0-sources";
  private static final String GUAVA_19_BIN = "src/test/resources/bin/guava-19.0.jar";
  private static final String GUAVA_EXPECTED_DIR = "src/test/resources/goal-output/guava-19.0/";

  public PrecisionRecallGuava19() {
    super(GUAVA_19_SRC, GUAVA_19_BIN, GUAVA_EXPECTED_DIR);
  }

  @Test
  public void testArrayListMultimap() throws Exception {
    test("com.google.common.collect.ArrayListMultimap", 1.0, 1.0);
  }

  @Test
  public void testConcurrentHashMultiset() throws Exception {
    test("com.google.common.collect.ConcurrentHashMultiset", 0.818, 0.692);
  }

  @Test
  public void testDoubles() throws Exception {
    test("com.google.common.primitives.Doubles", 0.75, 0.75);
  }

  @Test
  public void testFloats() throws Exception {
    test("com.google.common.primitives.Floats", 0.75, 0.75);
  }

  @Test
  public void testMoreObjects() throws Exception {
    test("com.google.common.base.MoreObjects", 1.0, 1.0);
  }

  @Test
  public void testShorts() throws Exception {
    test("com.google.common.primitives.Shorts", 0.75, 0.5);
  }

  @Test
  public void testStrings() throws Exception {
    test("com.google.common.base.Strings", 1.0, 1.0);
  }

  @Test
  public void testVerify() throws Exception {
    test("com.google.common.base.Verify", 1.0, 1.0);
  }

  @Test
  public void testAtomicDoubleArray() throws Exception {
    test("com.google.common.util.concurrent.AtomicDoubleArray", 1.0, 1.0);
  }
}
