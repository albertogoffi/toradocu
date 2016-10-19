package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.TestCaseStats;

public class PrecisionRecallCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSCOLLECTIONS_4_SRC =
      "src/test/resources/src/commons-collections4-4.1-src/commons-collections4-4.1-src/src/main/java";
  private static final String COMMONSCOLLECTIONS_4_BIN =
      "src/test/resources/bin/commons-collections4-4.1.jar";
  private static final String COMMONSCOLLECTIONS_4_EXPECTED_DIR =
      "src/test/resources/CommonsCollections-4.1/";

  public PrecisionRecallCommonsCollections4() {
    super(COMMONSCOLLECTIONS_4_SRC, COMMONSCOLLECTIONS_4_BIN, COMMONSCOLLECTIONS_4_EXPECTED_DIR);
  }

  @Test
  public void arrayStackTest() throws Exception {
    test("org.apache.commons.collections4.ArrayStack", 1.0, 0.75);
  }

  @Test
  public void bagUtilsTest() throws Exception {
    test("org.apache.commons.collections4.BagUtils", 1.0, 1.0);
  }

  @Test
  public void closureUtilsTest() throws Exception {
    test("org.apache.commons.collections4.ClosureUtils", 0.895, 0.68);
  }

  @Test
  public void collectionUtilsTest() throws Exception {
    test("org.apache.commons.collections4.CollectionUtils", 0.914, 0.842);
  }

  @Test
  public void predicateUtilsTest() throws Exception {
    test("org.apache.commons.collections4.PredicateUtils", 0.733, 0.733);
  }

  @Test
  public void queueUtilsTest() throws Exception {
    test("org.apache.commons.collections4.QueueUtils", 1.0, 1.0);
  }

  @Test
  public void fixedOrderComparatorTest() throws Exception {
    test("org.apache.commons.collections4.comparators.FixedOrderComparator", 1.0, 0.666);
  }

  @Test
  public void synchronizedBagTest() throws Exception {
    test("org.apache.commons.collections4.bag.SynchronizedBag", 1.0, 1.0);
  }

  @Test
  public void fluentIterableTest() throws Exception {
    test("org.apache.commons.collections4.FluentIterable", 1.0, 0.875);
  }

  @Test
  public void LRUMapTest() throws Exception {
    test("org.apache.commons.collections4.map.LRUMap", 0.625, 0.468);
  }

  @Test
  public void allPredicateTest() throws Exception {
    test("org.apache.commons.collections4.functors.AllPredicate", 1.0, 0.714);
  }

  @Test
  public void andPredicateTest() throws Exception {
    test("org.apache.commons.collections4.functors.AndPredicate", 1.0, 1.0);
  }

  @Test
  public void anyPredicateTest() throws Exception {
    test("org.apache.commons.collections4.functors.AnyPredicate", 1.0, 0.714);
  }
}
