package org.toradocu.random;

import java.util.Map;
import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;
import org.toradocu.testlib.TestCaseStats;

public class AccuracyRandomCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/commons-collections4-4.1-src/src/main/java";
  private static final String EXPECTED = "src/test/resources/CommonsCollections-4.1/";

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
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
