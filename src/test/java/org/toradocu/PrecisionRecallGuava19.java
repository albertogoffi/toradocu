package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallGuava19 extends AbstractPrecisionRecallTestSuite {

  private static final String GUAVA_19_SRC = "src/test/resources/src/guava-19.0-sources/";
  private static final String GUAVA_19_BIN = "src/test/resources/bin/guava-19.0.jar";
  private static final String GUAVA_GOAL_DIR = "src/test/resources/goal-output/guava-19.0/";

  public PrecisionRecallGuava19() {
    super(GUAVA_19_SRC, GUAVA_19_BIN, GUAVA_GOAL_DIR);
  }

  @Test
  public void testArrayListMultimap() throws Exception {
    test("com.google.common.collect.ArrayListMultimap", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testCharMatcher() throws Exception {
    test("com.google.common.base.CharMatcher", 1, 1, 0, 0, 1, 1);
  }

  @Test
  public void testConverter() throws Exception {
    test("com.google.common.base.Converter", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSplitter() throws Exception {
    test("com.google.common.base.Splitter", 1, 1, 1, 0, 1, 1);
  }

  @Test
  public void testThrowables() throws Exception {
    test("com.google.common.base.Throwables", 1, 1, 1, 0, 1, 1);
  }

  @Test
  public void testCacheLoader() throws Exception {
    test("com.google.common.cache.CacheLoader", 1, 1, 1, 0, 1, 1);
  }

  @Test
  public void testIterators() throws Exception {
    test("com.google.common.collect.Iterators", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testBloomFilter() throws Exception {
    test("com.google.common.hash.BloomFilter", 0, 0, 1, 1, 1, 1);
  }

  @Test
  public void testHashing() throws Exception {
    test("com.google.common.hash.Hashing", 1, 0, 1, 0, 1, 1);
  }

  @Test
  public void testConcurrentHashMultiset() throws Exception {
    test("com.google.common.collect.ConcurrentHashMultiset", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testDoubles() throws Exception {
    test("com.google.common.primitives.Doubles", 0.75, 0.75, 1, 0, 1, 1);
  }

  @Test
  public void testFloats() throws Exception {
    test("com.google.common.primitives.Floats", 0.75, 0.75, 1, 1, 1, 1);
  }

  @Test
  public void testMoreObjects() throws Exception {
    test("com.google.common.base.MoreObjects", 1, 1, 1, 1, 0, 0);
  }

  @Test
  public void testShorts() throws Exception {
    test("com.google.common.primitives.Shorts", 0.6, 0.5, 1, 0, 1, 1);
  }

  @Test
  public void testStrings() throws Exception {
    test("com.google.common.base.Strings", 1, 1, 1, 0, 0.333, 0.25);
  }

  @Test
  public void testVerify() throws Exception {
    test("com.google.common.base.Verify", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testAtomicDoubleArray() throws Exception {
    test("com.google.common.util.concurrent.AtomicDoubleArray", 1, 1, 1, 1, 1, 1);
  }
}
