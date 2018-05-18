package org.toradocu.accuracy;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyGuava19 extends AbstractPrecisionRecallTestSuite {

  private static final String GUAVA_19_SRC = "src/test/resources/src/guava-19.0-sources/";
  private static final String GUAVA_19_BIN = "src/test/resources/bin/guava-19.0.jar";
  private static final String GUAVA_GOAL_DIR = "src/test/resources/goal-output/guava-19.0/";

  public AccuracyGuava19() {
    super(GUAVA_19_SRC, GUAVA_19_BIN, GUAVA_GOAL_DIR);
  }

  @Test
  public void testArrayListMultimap() {
    test("com.google.common.collect.ArrayListMultimap", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testCharMatcher() {
    test("com.google.common.base.CharMatcher", 1, 1, 1, 0, 0, 1);
  }

  @Test
  public void testConverter() {
    test("com.google.common.base.Converter", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testSplitter() {
    test("com.google.common.base.Splitter", 1, 1, 1, 0, 1, 1);
  }

  @Test
  public void testThrowables() {
    test("com.google.common.base.Throwables", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testCacheLoader() {
    test("com.google.common.cache.CacheLoader", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testIterators() {
    test("com.google.common.collect.Iterators", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testBloomFilter() {
    test("com.google.common.hash.BloomFilter", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testHashing() {
    test("com.google.common.hash.Hashing", 1, 1, 1, 0, 1, 1);
  }

  @Test
  public void testConcurrentHashMultiset() {
    test("com.google.common.collect.ConcurrentHashMultiset", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testDoubles() {
    test("com.google.common.primitives.Doubles", 0.75, 0.75, 1, 1, 1, 1);
  }

  @Test
  public void testFloats() {
    test("com.google.common.primitives.Floats", 0.75, 0.75, 1, 1, 1, 1);
  }

  @Test
  public void testMoreObjects() {
    test("com.google.common.base.MoreObjects", 1, 1, 1, 1, 0, 0);
  }

  @Test
  public void testShorts() {
    test("com.google.common.primitives.Shorts", 0.75, 0.5, 1, 1, 1, 1);
  }

  @Test
  public void testStrings() {
    test("com.google.common.base.Strings", 1, 1, 1, 0.5, 0.25, 0.25);
  }

  @Test
  public void testVerify() {
    test("com.google.common.base.Verify", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testAtomicDoubleArray() {
    test("com.google.common.util.concurrent.AtomicDoubleArray", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testUnicodeEscaper() {
    test("com.google.common.escape.UnicodeEscaper", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testImmutableSortedMapFauxverideShim() {
    test("com.google.common.collect.ImmutableSortedMapFauxverideShim", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testImmutableSortedMap() {
    test("com.google.common.collect.ImmutableSortedMap", 1, 1, 1, 1, 1, 1);
  }
}
