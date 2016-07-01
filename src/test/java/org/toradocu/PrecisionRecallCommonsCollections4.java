package org.toradocu;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.toradocu.testlib.AbstracPrecisionRecallTestSuite;
import org.toradocu.testlib.TestCaseStats;

public class PrecisionRecallCommonsCollections4 extends AbstracPrecisionRecallTestSuite {

	private static final String COMMONSCOLLECTIONS_4_SRC = "src/test/resources/commons-collections4-4.1-src/src/main/java";
	private static final String COMMONSCOLLECTIONS_4_EXPECTED_DIR = "src/test/resources/CommonsCollections-4.1/";
	
	@Before
    public void init() {
		setSourceDir(COMMONSCOLLECTIONS_4_SRC);
		setExpectedOutputDir(COMMONSCOLLECTIONS_4_EXPECTED_DIR);
	}
	
	@Test
	public void arrayStackTest() throws Exception {
		TestCaseStats stats = test("org.apache.commons.collections4.ArrayStack");
		assertEquals(1, stats.getPrecision(), 0);
		assertEquals(0.75, stats.getRecall(), 0);
	}
	
	@Test
	public void bagUtilsTest() throws Exception {
		TestCaseStats stats = test("org.apache.commons.collections4.BagUtils");
		assertEquals(1, stats.getPrecision(), 0);
		assertEquals(1, stats.getRecall(), 0);
	}
	
	@Test
	public void closureUtilsTest() throws Exception {
		TestCaseStats stats = test("org.apache.commons.collections4.ClosureUtils");
		assertEquals(0.79, stats.getPrecision(), PRECISION);
		assertEquals(0.60, stats.getRecall(), PRECISION);
	}
	
	@Test
	public void collectionUtilsTest() throws Exception {
		TestCaseStats stats = test("org.apache.commons.collections4.CollectionUtils");
		assertEquals(0.71, stats.getPrecision(), PRECISION);
		assertEquals(0.54, stats.getRecall(), PRECISION);
	}
	
	@Test
	public void predicateUtilsTest() throws Exception {
		TestCaseStats stats = test("org.apache.commons.collections4.PredicateUtils");
		assertEquals(0.72, stats.getPrecision(), PRECISION);
		assertEquals(0.69, stats.getRecall(), PRECISION);
	}
	
	@Test
	public void queueUtilsTest() throws Exception {
		TestCaseStats stats = test("org.apache.commons.collections4.QueueUtils");
		assertEquals(1, stats.getPrecision(), 0);
		assertEquals(1, stats.getRecall(), 0);
	}
	
// =====================
	
	@Test
	public void fixedOrderComparatorTest() throws Exception {
		TestCaseStats stats = test("org.apache.commons.collections4.comparators.FixedOrderComparator");
		assertEquals(0.83, stats.getPrecision(), PRECISION);
		assertEquals(0.55, stats.getRecall(), PRECISION);
	}
}
