package org.toradocu.testgen;

import org.junit.Test;
import org.toradocu.testlib.TestgenTest;

public class AccuracyCommonsCollections4 extends TestgenTest {

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
    test("org.apache.commons.collections4.functors.IfClosure");
  }

  @Test
  public void testLazyMap() throws Exception {
    test("org.apache.commons.collections4.map.LazyMap");
  }

  @Test
  public void testFactoryUtils() throws Exception {
    test("org.apache.commons.collections4.FactoryUtils");
  }

  @Test
  public void testDefaultMapEntry() throws Exception {
    test("org.apache.commons.collections4.keyvalue.DefaultMapEntry");
  }

  @Test
  public void testCollectionBag() throws Exception {
    test("org.apache.commons.collections4.bag.CollectionBag");
  }

  @Test
  public void testKeyAnalyzer() throws Exception {
    test("org.apache.commons.collections4.trie.KeyAnalyzer");
  }

  @Test
  public void testHashedMap() throws Exception {
    test("org.apache.commons.collections4.map.HashedMap");
  }

  @Test
  public void testArrayStack() throws Exception {
    test("org.apache.commons.collections4.ArrayStack");
  }

  @Test
  public void testBagUtils() throws Exception {
    test("org.apache.commons.collections4.BagUtils");
  }

  @Test
  public void testClosureUtils() throws Exception {
    test("org.apache.commons.collections4.ClosureUtils");
  }

  @Test
  public void testCollectionUtils() throws Exception {
    test("org.apache.commons.collections4.CollectionUtils");
  }

  @Test
  public void testPredicateUtils() throws Exception {
    test("org.apache.commons.collections4.PredicateUtils");
  }

  @Test
  public void testQueueUtils() throws Exception {
    test("org.apache.commons.collections4.QueueUtils");
  }

  @Test
  public void testFixedOrderComparator() throws Exception {
    test("org.apache.commons.collections4.comparators.FixedOrderComparator");
  }

  @Test
  public void testSynchronizedBag() throws Exception {
    test("org.apache.commons.collections4.bag.SynchronizedBag");
  }

  @Test
  public void testFluentIterable() throws Exception {
    test("org.apache.commons.collections4.FluentIterable");
  }

  @Test
  public void testLRUMap() throws Exception {
    test("org.apache.commons.collections4.map.LRUMap");
  }

  @Test
  public void testAllPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AllPredicate");
  }

  @Test
  public void testAndPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AndPredicate");
  }

  @Test
  public void testAnyPredicate() throws Exception {
    test("org.apache.commons.collections4.functors.AnyPredicate");
  }
}
