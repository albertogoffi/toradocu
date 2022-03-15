package org.toradocu.temp;

import org.junit.Test;
import org.toradocu.testlib.TestgenTest;

public class AccuracyGuava19 extends TestgenTest {

  private static final String GUAVA_19_SRC = "src/test/resources/src/guava-19.0-sources/";
  private static final String GUAVA_19_BIN = "src/test/resources/bin/guava-19.0.jar";
  private static final String GUAVA_GOAL_DIR = "src/test/resources/goal-output/guava-19.0/";

  public AccuracyGuava19() {
    super(GUAVA_19_SRC, GUAVA_19_BIN, GUAVA_GOAL_DIR);
  }

  @Test
  public void testUnicodeEscaper() throws Exception {
    test("com.google.common.escape.UnicodeEscaper");
  }

  @Test
  public void testImmutableSortedMapFauxverideShim() throws Exception {
    test("com.google.common.collect.ImmutableSortedMapFauxverideShim");
  }

  @Test
  public void testImmutableSortedMap() throws Exception {
    test("com.google.common.collect.ImmutableSortedMap");
  }

  @Test
  public void testAtomicDoubleArray() throws Exception {
    test("com.google.common.util.concurrent.AtomicDoubleArray");
  }

  @Test
  public void testFloats() throws Exception {
    test("com.google.common.primitives.Floats");
  }

  @Test
  public void testArrayListMultimap() throws Exception {
    test("com.google.common.collect.ArrayListMultimap");
  }

  @Test
  public void testCharMatcher() throws Exception {
    test("com.google.common.base.CharMatcher");
  }

  @Test
  public void testConverter() throws Exception {
    test("com.google.common.base.Converter");
  }

  @Test
  public void testSplitter() throws Exception {
    test("com.google.common.base.Splitter");
  }

  @Test
  public void testThrowables() throws Exception {
    test("com.google.common.base.Throwables");
  }

  @Test
  public void testCacheLoader() throws Exception {
    test("com.google.common.cache.CacheLoader");
  }

  @Test
  public void testIterators() throws Exception {
    test("com.google.common.collect.Iterators");
  }

  @Test
  public void testBloomFilter() throws Exception {
    test("com.google.common.hash.BloomFilter");
  }

  @Test
  public void testConcurrentHashMultiset() throws Exception {
    test("com.google.common.collect.ConcurrentHashMultiset");
  }

  @Test
  public void testDoubles() throws Exception {
    test("com.google.common.primitives.Doubles");
  }

  @Test
  public void testMoreObjects() throws Exception {
    test("com.google.common.base.MoreObjects");
  }

  @Test
  public void testShorts() throws Exception {
    test("com.google.common.primitives.Shorts");
  }

  @Test
  public void testStrings() throws Exception {
    test("com.google.common.base.Strings");
  }

  @Test
  public void testVerify() throws Exception {
    test("com.google.common.base.Verify");
  }
}
