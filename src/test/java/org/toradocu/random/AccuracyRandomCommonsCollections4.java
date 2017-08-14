package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSCOLLECTIONS_4_SRC =
      "src/test/resources/commons-collections4-4.1-src/src/main/java";
  private static final String COMMONSCOLLECTIONS_4_EXPECTED_DIR =
      "src/test/resources/CommonsCollections-4.1/";

  @Test
  public void ifClosureTest() throws Exception {
    test("org.apache.commons.collections4.functors.IfClosure");
  }

  @Test
  public void lazyMapTest() throws Exception {
    test("org.apache.commons.collections4.map.LazyMap");
  }

  @Test
  public void factoryUtilsTest() throws Exception {
    test("org.apache.commons.collections4.FactoryUtils");
  }

  private void test(String clazz) {
    PrecisionRecallTest.test(clazz, COMMONSCOLLECTIONS_4_SRC, COMMONSCOLLECTIONS_4_EXPECTED_DIR);
  }
}
