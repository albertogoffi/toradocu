package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;
import org.toradocu.testlib.TestCaseStats;

import java.util.Map;

public class PrecisionRecallCommonsCollections4 extends AbstractPrecisionRecallTestSuite {

  private static final String COMMONSCOLLECTIONS_4_SRC =
      "src/test/resources/commons-collections4-4.1-src/src/main/java";
  private static final String COMMONSCOLLECTIONS_4_EXPECTED_DIR =
      "src/test/resources/CommonsCollections-4.1/";

  @Test
  public void arrayStackTest() throws Exception {
    Map<String, TestCaseStats> stats = test("org.apache.commons.collections4.ArrayStack");
    //		assertEquals(1, stats.getPrecision(), 0);
    //		assertEquals(0.75, stats.getRecall(), 0);
  }

  @Test
  public void bagUtilsTest() throws Exception {
    Map<String, TestCaseStats> stats = test("org.apache.commons.collections4.BagUtils");
    //		assertEquals(1, stats.getPrecision(), 0);
    //		assertEquals(1, stats.getRecall(), 0);
  }

  @Test
  public void closureUtilsTest() throws Exception {
    Map<String, TestCaseStats> stats = test("org.apache.commons.collections4.ClosureUtils");
    //		assertEquals(0.88, stats.getPrecision(), PRECISION);
    //		assertEquals(0.60, stats.getRecall(), PRECISION);
  }

  @Test
  public void collectionUtilsTest() throws Exception {
    Map<String, TestCaseStats> stats = test("org.apache.commons.collections4.CollectionUtils");
    //		assertEquals(0.66, stats.getPrecision(), PRECISION);
    //		assertEquals(0.49, stats.getRecall(), PRECISION);
  }

  @Test
  public void predicateUtilsTest() throws Exception {
    Map<String, TestCaseStats> stats = test("org.apache.commons.collections4.PredicateUtils");
    //		assertEquals(0.72, stats.getPrecision(), PRECISION);
    //		assertEquals(0.69, stats.getRecall(), PRECISION);
  }

  @Test
  public void queueUtilsTest() throws Exception {
    Map<String, TestCaseStats> stats = test("org.apache.commons.collections4.QueueUtils");
    //		assertEquals(1, stats.getPrecision(), 0);
    //		assertEquals(1, stats.getRecall(), 0);
  }

  // =====================

  @Test
  public void fixedOrderComparatorTest() throws Exception {
    Map<String, TestCaseStats> stats =
        test("org.apache.commons.collections4.comparators.FixedOrderComparator");
    //		assertEquals(1, stats.getPrecision(), 0);
    //		assertEquals(0.66, stats.getRecall(), PRECISION);
  }

  @Test
  public void allPredicateTest() throws Exception {
    test("org.apache.commons.collections4.functors.AllPredicate");
  }

  @Test
  public void andPredicateTest() throws Exception {
    test("org.apache.commons.collections4.functors.AndPredicate");
  }

  @Test
  public void anyPredicateTest() throws Exception {
    test("org.apache.commons.collections4.functors.AnyPredicate");
  }

  @Test
  public void fluentIterableTest() throws Exception {
    test("org.apache.commons.collections4.FluentIterable");
  }

  @Test
  public void lruMapTest() throws Exception {
    test("org.apache.commons.collections4.map.LRUMap");
  }

  @Test
  public void synchronizedBagTest() throws Exception {
    test("org.apache.commons.collections4.bag.SynchronizedBag");
  }

  private Map<String, TestCaseStats> test(String targetClass) {
    Map<String, TestCaseStats> stats =
        PrecisionRecallTest.test(
            targetClass, COMMONSCOLLECTIONS_4_SRC, COMMONSCOLLECTIONS_4_EXPECTED_DIR);
    testSuiteStats.addTest(stats);
    return stats;
  }
}
