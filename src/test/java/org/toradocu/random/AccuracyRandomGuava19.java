package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomGuava19 extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/guava-19.0-sources";
  private static final String EXPECTED = "src/test/resources/Guava-19/";

  @Test
  public void floatsTest() throws Exception {
    test("com.google.common.primitives.Floats");
  }

  @Test
  public void unicodeEscaperTest() throws Exception {
    test("com.google.common.escape.UnicodeEscaper");
  }

  @Test
  public void atomicDoubleArrayTest() throws Exception {
    test("com.google.common.util.concurrent.AtomicDoubleArray");
  }

  private void test(String clazz) {
    PrecisionRecallTest.test(clazz, SRC, EXPECTED);
  }
}
