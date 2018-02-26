package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/commons-collections4-4.1-src/src/main/java";
  private static final String EXPECTED = "src/test/resources/CommonsCollections-4.1/";

  @Test
  public void ifClosureTest() {
    test("org.apache.commons.collections4.functors.IfClosure");
  }

  @Test
  public void lazyMapTest() {
    test("org.apache.commons.collections4.map.LazyMap");
  }

  @Test
  public void factoryUtilsTest() {
    test("org.apache.commons.collections4.FactoryUtils");
  }

  @Test
  public void defaultMapEntryTest() {
    test("org.apache.commons.collections4.keyvalue.DefaultMapEntry");
  }

  @Test
  public void collectionBagTest() {
    test("org.apache.commons.collections4.bag.CollectionBag");
  }

  @Test
  public void testKeyAnalyzer() {
    test("org.apache.commons.collections4.trie.KeyAnalyzer");
  }

  @Test
  public void testHashedMap() {
    test("org.apache.commons.collections4.map.HashedMap");
  }

  @Test
  public void testArrayStack() {
    test("org.apache.commons.collections4.ArrayStack");
  }

  @Test
  public void testBagUtils() {
    test("org.apache.commons.collections4.BagUtils");
  }

  @Test
  public void testClosureUtils() {
    test("org.apache.commons.collections4.ClosureUtils");
  }

  @Test
  public void testCollectionUtils() {
    test("org.apache.commons.collections4.CollectionUtils");
  }

  @Test
  public void testPredicateUtils() {
    test("org.apache.commons.collections4.PredicateUtils");
  }

  @Test
  public void testQueueUtils() {
    test("org.apache.commons.collections4.QueueUtils");
  }

  @Test
  public void testFixedOrderComparator() {
    test("org.apache.commons.collections4.comparators.FixedOrderComparator");
  }

  @Test
  public void testSynchronizedBag() {
    test("org.apache.commons.collections4.bag.SynchronizedBag");
  }

  @Test
  public void testFluentIterable() {
    test("org.apache.commons.collections4.FluentIterable");
  }

  @Test
  public void testLRUMap() {
    test("org.apache.commons.collections4.map.LRUMap");
  }

  @Test
  public void testAllPredicate() {
    test("org.apache.commons.collections4.functors.AllPredicate");
  }

  @Test
  public void testAndPredicate() {
    test("org.apache.commons.collections4.functors.AndPredicate");
  }

  @Test
  public void testAnyPredicate() {
    test("org.apache.commons.collections4.functors.AnyPredicate");
  }

  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
