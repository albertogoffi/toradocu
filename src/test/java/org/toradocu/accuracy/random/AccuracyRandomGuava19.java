package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

/** Created by arianna on 07/08/17. */
public class AccuracyRandomGuava19 extends AbstractPrecisionRecallTestSuite {

  private static final String GUAVA_19_SRC = "src/test/resources/src/guava-19.0-sources/";
  private static final String GUAVA_19_BIN = "src/test/resources/bin/guava-19.0.jar";
  private static final String GUAVA_GOAL_DIR = "src/test/resources/goal-output/guava-19.0/";

  public AccuracyRandomGuava19() {
    super(GUAVA_19_SRC, GUAVA_19_BIN, GUAVA_GOAL_DIR);
  }

  @Test
  public void testFloats() throws Exception {
    test("com.google.common.primitives.Floats", 0.75, 0.75, 1, 1, 1, 1);
  }

  @Test
  public void testUnicodeEscaper() throws Exception {
    test("com.google.common.escape.UnicodeEscaper", 0.75, 0.75, 1, 1, 1, 1);
  }
}
