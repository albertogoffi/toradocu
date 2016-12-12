package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSCOLLECTIONS_4_SRC =
      "src/test/resources/src/commons-collections4-4.1-src/src/main/java/";
  private static final String COMMONSCOLLECTIONS_4_BIN =
      "src/test/resources/bin/commons-collections4-4.1.jar";
  private static final String COMMONSCOLLECTIONS_4_GOAL_DIR =
      "src/test/resources/goal-output/commons-collections4-4.1/";

  public PrecisionRecallCommonsCollections4() {
    super(COMMONSCOLLECTIONS_4_SRC, COMMONSCOLLECTIONS_4_BIN, COMMONSCOLLECTIONS_4_GOAL_DIR);
  }

  @Test
  public void testArrayStack() throws Exception {
    test("org.apache.commons.collections4.ArrayStack", 1.0, 0.75);
  }

  @Test
  public void testBagUtils() throws Exception {
    test("org.apache.commons.collections4.BagUtils", 1.0, 1.0);
  }

  @Test
  public void testClosureUtils() throws Exception {
    test("org.apache.commons.collections4.ClosureUtils", 0.888, 0.695);
  }

  @Test
  public void testCollectionUtils() throws Exception {
    test("org.apache.commons.collections4.CollectionUtils", 0.964, 0.870);
  }

  @Test
  public void testPredicateUtils() throws Exception {
    test("org.apache.commons.collections4.PredicateUtils", 0.84, 0.724);
  }

  @Test
  public void testQueueUtils() throws Exception {
    test("org.apache.commons.collections4.QueueUtils", 1.0, 1.0);
  }

  @Test
  public void testFixedOrderComparator() throws Exception {
    test("org.apache.commons.collections4.comparators.FixedOrderComparator", 1.0, 0.571);
  }

  @Test
  public void testSynchronizedBag() throws Exception {
    test("org.apache.commons.collections4.bag.SynchronizedBag", 1.0, 1.0);
  }

  @Test
  public void testFluentIterable() throws Exception {
    test("org.apache.commons.collections4.FluentIterable", 1.0, 0.866);
  }

  @Test
  public void testLRUMap() throws Exception {
    test("org.apache.commons.collections4.map.LRUMap", 0.824, 0.482);
  }

  @Test
  public void testAllPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AllPredicate", 1.0, 0.75);
  }

  @Test
  public void testAndPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AndPredicate", 1.0, 1.0);
  }

  @Test
  public void testAnyPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AnyPredicate", 1.0, 0.75);
  }
}
