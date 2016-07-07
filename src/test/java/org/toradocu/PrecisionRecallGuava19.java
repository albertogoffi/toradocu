package org.toradocu;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.toradocu.testlib.AbstracPrecisionRecallTestSuite;
import org.toradocu.testlib.TestCaseStats;

public class PrecisionRecallGuava19 extends AbstracPrecisionRecallTestSuite {

	private static final String GUAVA_19_SRC = "src/test/resources/guava-19.0-sources";
	private static final String GUAVA_EXPECTED_DIR = "src/test/resources/Guava-19/";
	
	@Before
    public void init() {
		setSourceDir(GUAVA_19_SRC);
		setExpectedOutputDir(GUAVA_EXPECTED_DIR);
	}
	
	@Test
	public void arrayListMultimapTest() throws Exception {
		TestCaseStats stats = test("com.google.common.collect.ArrayListMultimap");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
	}
	
	@Test
	public void concurrentHashMultisetTest() throws Exception {
		TestCaseStats stats = test("com.google.common.collect.ConcurrentHashMultiset");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), closeTo(0.81, PRECISION));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.75));
	}
	
	@Test
	public void doublesTest() throws Exception {
		TestCaseStats stats = test("com.google.common.primitives.Doubles");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(0.75));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.75));
	}
	
	@Test
	public void floatsTest() throws Exception {
		TestCaseStats stats = test("com.google.common.primitives.Floats");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(0.75));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.75));
	}
	
	@Test
	public void moreObjectsTest() throws Exception {
		TestCaseStats stats = test("com.google.common.base.MoreObjects");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
	}
	
	@Test
	public void shortsTest() throws Exception {
		TestCaseStats stats = test("com.google.common.primitives.Shorts");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(0.75));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(0.5));
	}
	
	@Test
	public void stringsTest() throws Exception {
		TestCaseStats stats = test("com.google.common.base.Strings");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
	}
	
	@Test
	public void verifyTest() throws Exception {
		TestCaseStats stats = test("com.google.common.base.Verify");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
	}
	
	@Test
	public void atomicDoubleArrayTest() throws Exception {
		TestCaseStats stats = test("com.google.common.util.concurrent.AtomicDoubleArray");
		assertThat(PRECISION_MESSAGE, stats.getPrecision(), is(1.0));
		assertThat(RECALL_MESSAGE, stats.getRecall(), is(1.0));
	}
}
