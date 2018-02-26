package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomGuava19 extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/guava-19.0-sources";
  private static final String EXPECTED = "src/test/resources/Guava-19/";

  @Test
  public void testUnicodeEscaper() {
    test("com.google.common.escape.UnicodeEscaper");
  }

  @Test
  public void testImmutableSortedMapFauxverideShim() {
    test("com.google.common.collect.ImmutableSortedMapFauxverideShim");
  }

  @Test
  public void testImmutableSortedMap() {
    test("com.google.common.collect.ImmutableSortedMap");
  }

  @Test
  public void testAtomicDoubleArray() {
    test("com.google.common.util.concurrent.AtomicDoubleArray");
  }

  @Test
  public void testFloats() {
    test("com.google.common.primitives.Floats");
  }

  @Test
  public void testArrayListMultimap() {
    test("com.google.common.collect.ArrayListMultimap");
  }

  @Test
  public void testCharMatcher() {
    test("com.google.common.base.CharMatcher");
  }

  @Test
  public void testConverter() {
    test("com.google.common.base.Converter");
  }

  @Test
  public void testSplitter() {
    test("com.google.common.base.Splitter");
  }

  @Test
  public void testThrowables() {
    test("com.google.common.base.Throwables");
  }

  @Test
  public void testCacheLoader() {
    test("com.google.common.cache.CacheLoader");
  }

  @Test
  public void testIterators() {
    test("com.google.common.collect.Iterators");
  }

  @Test
  public void testBloomFilter() {
    test("com.google.common.hash.BloomFilter");
  }

  @Test
  public void testConcurrentHashMultiset() {
    test("com.google.common.collect.ConcurrentHashMultiset");
  }

  @Test
  public void testDoubles() {
    test("com.google.common.primitives.Doubles");
  }

  @Test
  public void testMoreObjects() {
    test("com.google.common.base.MoreObjects");
  }

  @Test
  public void testShorts() {
    test("com.google.common.primitives.Shorts");
  }

  @Test
  public void testStrings() {
    test("com.google.common.base.Strings");
  }

  @Test
  public void testVerify() {
    test("com.google.common.base.Verify");
  }

  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
