package org.toradocu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;
import org.toradocu.testlib.TestCaseStats;

public class PrecisionRecallGuava19 extends AbstractPrecisionRecallTestSuite {

	private static final String GUAVA_19_SRC = "src/test/resources/guava-19.0-sources";
	private static final String GUAVA_EXPECTED_DIR = "src/test/resources/Guava-19/";
	
	@Test
	public void arrayListMultimapTest() throws Exception {
		TestCaseStats stats = test("com.google.common.collect.ArrayListMultimap");
//		assertEquals(1, stats.getPrecision(), 0);
//		assertEquals(1, stats.getRecall(), 0);
	}
	
	@Test
	public void concurrentHashMultisetTest() throws Exception {
		TestCaseStats stats = test("com.google.common.collect.ConcurrentHashMultiset");
//		assertEquals(0.81, stats.getPrecision(), PRECISION);
//		assertEquals(0.75, stats.getRecall(), 0);
	}
	
	@Test
	public void doublesTest() throws Exception {
		TestCaseStats stats = test("com.google.common.primitives.Doubles");
//		assertEquals(0.75, stats.getPrecision(), 0);
//		assertEquals(0.75, stats.getRecall(), 0);
	}
	
	@Test
	public void floatsTest() throws Exception {
		TestCaseStats stats = test("com.google.common.primitives.Floats");
//		assertEquals(0.75, stats.getPrecision(), 0);
//		assertEquals(0.75, stats.getRecall(), 0);
	}
	
	@Test
	public void moreObjectsTest() throws Exception {
		TestCaseStats stats = test("com.google.common.base.MoreObjects");
//		assertEquals(1, stats.getPrecision(), 0);
//		assertEquals(1, stats.getRecall(), 0);
	}
	
	@Test
	public void shortsTest() throws Exception {
		TestCaseStats stats = test("com.google.common.primitives.Shorts");
//		assertEquals(0.75, stats.getPrecision(), 0);
//		assertEquals(0.5, stats.getRecall(), 0);
	}
	
	@Test
	public void stringsTest() throws Exception {
		TestCaseStats stats = test("com.google.common.base.Strings");
//		assertEquals(1, stats.getPrecision(), 0);
//		assertEquals(1, stats.getRecall(), 0);
	}
	
	@Test
	public void verifyTest() throws Exception {
		TestCaseStats stats = test("com.google.common.base.Verify");
//		assertEquals(1, stats.getPrecision(), 0);
//		assertEquals(1, stats.getRecall(), 0);
	}
	
	@Test
	public void atomicDoubleArrayTest() throws Exception {
		TestCaseStats stats = test("com.google.common.util.concurrent.AtomicDoubleArray");
//		assertEquals(1, stats.getPrecision(), 0);
//		assertEquals(1, stats.getRecall(), 0);
	}
	
	private TestCaseStats test(String targetClass) {
		TestCaseStats stats = PrecisionRecallTest.test(targetClass, GUAVA_19_SRC, GUAVA_EXPECTED_DIR);
		testSuiteStats.addTest(stats);
		return stats;
	}
}
