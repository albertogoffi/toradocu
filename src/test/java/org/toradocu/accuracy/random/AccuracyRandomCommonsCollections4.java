package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyRandomCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSCOLLECTIONS_4_SRC =
      "src/test/resources/src/commons-collections4-4.1-src/src/main/java/";
  private static final String COMMONSCOLLECTIONS_4_BIN =
      "src/test/resources/bin/commons-collections4-4.1.jar";
  private static final String COMMONSCOLLECTIONS_4_GOAL_DIR =
      "src/test/resources/goal-output/random/commons-collections4-4.1/";

  public AccuracyRandomCommonsCollections4() {
    super(COMMONSCOLLECTIONS_4_SRC, COMMONSCOLLECTIONS_4_BIN, COMMONSCOLLECTIONS_4_GOAL_DIR);
  }

  @Test
  public void testIfClosure() throws Exception {
    test("org.apache.commons.collections4.functors.IfClosure", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testLazyMap() throws Exception {
    test("org.apache.commons.collections4.map.LazyMap", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testFactoryUtils() throws Exception {
    test("org.apache.commons.collections4.FactoryUtils", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testDefaultMapEntry() throws Exception {
    test("org.apache.commons.collections4.keyvalue.DefaultMapEntry", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testCollectionBag() throws Exception {
    test("org.apache.commons.collections4.bag.CollectionBag", 1, 1, 1, 1, 1, 1);
  }
}
