package org.toradocu;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

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
    TestCaseStats stats = test("org.apache.commons.collections4.ArrayStack");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.75));
  }

  @Test
  public void bagUtilsTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.BagUtils");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
  }

  @Test
  public void closureUtilsTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.ClosureUtils");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), closeTo(0.895, PRECISION));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.68));
  }

  @Test
  public void collectionUtilsTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.CollectionUtils");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), closeTo(0.914, PRECISION));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.842, PRECISION));
  }

  @Test
  public void predicateUtilsTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.PredicateUtils");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), closeTo(0.733, PRECISION));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.733, PRECISION));
  }

  @Test
  public void queueUtilsTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.QueueUtils");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
  }

  @Test
  public void fixedOrderComparatorTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.comparators.FixedOrderComparator");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.666, PRECISION));
  }

  @Test
  public void synchronizedBagTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.bag.SynchronizedBag");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
  }

  @Test
  public void fluentIterableTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.FluentIterable");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.875, PRECISION));
  }

  @Test
  public void LRUMapTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.map.LRUMap");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), closeTo(0.625, PRECISION));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.468, PRECISION));
  }

  @Test
  public void allPredicateTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.functors.AllPredicate");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.714, PRECISION));
  }

  @Test
  public void andPredicateTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.functors.AndPredicate");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
  }

  @Test
  public void anyPredicateTest() throws Exception {
    TestCaseStats stats = test("org.apache.commons.collections4.functors.AnyPredicate");
    assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
    assertThat(RECALL_MESSAGE, stats.getRecall(), closeTo(0.714, PRECISION));
  }
}
