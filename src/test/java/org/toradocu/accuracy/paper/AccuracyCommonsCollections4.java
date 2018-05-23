package org.toradocu.accuracy.paper;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSCOLLECTIONS_4_SRC =
      "src/test/resources/src/commons-collections4-4.1-src/src/main/java/";
  private static final String COMMONSCOLLECTIONS_4_BIN =
      "src/test/resources/bin/commons-collections4-4.1.jar";
  private static final String COMMONSCOLLECTIONS_4_GOAL_DIR =
      "src/test/resources/goal-output/commons-collections4-4.1/";

  public AccuracyCommonsCollections4() {
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

  @Test
  public void testKeyAnalyzer() throws Exception {
    test("org.apache.commons.collections4.trie.KeyAnalyzer", 1, 1, 1, 1, 0, 1);
  }

  @Test
  public void testHashedMap() throws Exception {
    test("org.apache.commons.collections4.map.HashedMap", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testArrayStack() throws Exception {
    test("org.apache.commons.collections4.ArrayStack", 1, 0.75, 1, 1, 1, 0.4);
  }

  @Test
  public void testBagUtils() throws Exception {
    test("org.apache.commons.collections4.BagUtils", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testClosureUtils() throws Exception {
    test("org.apache.commons.collections4.ClosureUtils", 1, 0.695, 1, 1, 1, 1);
  }

  @Test
  public void testCollectionUtils() throws Exception {
    test("org.apache.commons.collections4.CollectionUtils", 1, 0.935, 1, 1, 0.428, 0.25);
  }

  @Test
  public void testPredicateUtils() throws Exception {
    test("org.apache.commons.collections4.PredicateUtils", 1, 0.862, 1, 1, 0, 1);
  }

  @Test
  public void testQueueUtils() throws Exception {
    test("org.apache.commons.collections4.QueueUtils", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testFixedOrderComparator() throws Exception {
    test("org.apache.commons.collections4.comparators.FixedOrderComparator", 1, 0.571, 1, 1, 1, 0);
  }

  @Test
  public void testSynchronizedBag() throws Exception {
    test("org.apache.commons.collections4.bag.SynchronizedBag", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testFluentIterable() throws Exception {
    test("org.apache.commons.collections4.FluentIterable", 1, 0.866, 1, 1, 1, 0);
  }

  @Test
  public void testLRUMap() throws Exception {
    test("org.apache.commons.collections4.map.LRUMap", 0.833, 0.833, 1, 1, 1, 1);
  }

  @Test
  public void testAllPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AllPredicate", 1, 0.75, 1, 1, 1, 1);
  }

  @Test
  public void testAndPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AndPredicate", 1, 1, 1, 1, 0, 1);
  }

  @Test
  public void testAnyPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AnyPredicate", 1, 0.75, 1, 1, 1, 1);
  }
}
