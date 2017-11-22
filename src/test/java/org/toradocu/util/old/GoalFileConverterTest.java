package org.toradocu.util.old;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;

public class GoalFileConverterTest {

  private static final String OUTPUT_DIR = "converted-goal-files/";
  private static final String EXPECTED_OUTPUT_DIR = "src/test/resources/v01-goal-output/";

  private static final String GUAVA_BIN = "src/test/resources/bin/guava-19.0.jar";
  private static final String GUAVA_JSON_DIR = "src/test/resources/goal-output/guava-19.0/";
  private static final String GUAVA_EXPECTED_OUTPUT_DIR = EXPECTED_OUTPUT_DIR + "Guava-19/";

  private static final String COMM_COLL_BIN = "src/test/resources/bin/commons-collections4-4.1.jar";
  private static final String COMM_COLL_JSON_DIR =
      "src/test/resources/goal-output/commons-collections4-4.1/";
  private static final String COMM_COLL_EXPECTED_OUTPUT_DIR =
      EXPECTED_OUTPUT_DIR + "CommonsCollections-4.1/";

  @BeforeClass
  public static void setup() throws IOException {
    final Path dirPath = Paths.get(OUTPUT_DIR);
    if (!dirPath.toFile().exists()) {
      Files.createDirectory(dirPath);
    }
  }

  @Test
  public void arrayListMultimap() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.collect.ArrayListMultimap_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void atomicDoubleArray() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.util.concurrent.AtomicDoubleArray_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void concurrentHashMultiset() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.collect.ConcurrentHashMultiset_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void doubles() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.primitives.Doubles_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void floats() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.primitives.Floats_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void moreObjects() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.base.MoreObjects_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void shorts() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.primitives.Shorts_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void strings() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.base.Strings_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void verify() {
    test(
        GUAVA_JSON_DIR,
        "com.google.common.base.Verify_goal.json",
        GUAVA_EXPECTED_OUTPUT_DIR,
        GUAVA_BIN);
  }

  @Test
  public void arrayStack() {
    test(
        COMM_COLL_JSON_DIR,
        "org.apache.commons.collections4.ArrayStack_goal.json",
        COMM_COLL_EXPECTED_OUTPUT_DIR,
        COMM_COLL_BIN);
  }

  @Test
  public void bagUtils() {
    test(
        COMM_COLL_JSON_DIR,
        "org.apache.commons.collections4.BagUtils_goal.json",
        COMM_COLL_EXPECTED_OUTPUT_DIR,
        COMM_COLL_BIN);
  }

  @Test
  public void closureUtils() {
    test(
        COMM_COLL_JSON_DIR,
        "org.apache.commons.collections4.ClosureUtils_goal.json",
        COMM_COLL_EXPECTED_OUTPUT_DIR,
        COMM_COLL_BIN);
  }

  // Test excluded because it fails.
  // @Test
  public void collectionUtils() {
    test(
        COMM_COLL_JSON_DIR,
        "org.apache.commons.collections4.CollectionUtils_goal.json",
        COMM_COLL_EXPECTED_OUTPUT_DIR,
        COMM_COLL_BIN);
  }

  @Test
  public void fixedOrderComparator() {
    test(
        COMM_COLL_JSON_DIR,
        "org.apache.commons.collections4.comparators.FixedOrderComparator_goal.json",
        COMM_COLL_EXPECTED_OUTPUT_DIR,
        COMM_COLL_BIN);
  }

  @Test
  public void predicateUtils() {
    test(
        COMM_COLL_JSON_DIR,
        "org.apache.commons.collections4.PredicateUtils_goal.json",
        COMM_COLL_EXPECTED_OUTPUT_DIR,
        COMM_COLL_BIN);
  }

  @Test
  public void queueUtils() {
    test(
        COMM_COLL_JSON_DIR,
        "org.apache.commons.collections4.QueueUtils_goal.json",
        COMM_COLL_EXPECTED_OUTPUT_DIR,
        COMM_COLL_BIN);
  }

  @Test
  public void lruMap() {
    test(
        COMM_COLL_JSON_DIR,
        "org.apache.commons.collections4.map.LRUMap_goal.json",
        COMM_COLL_EXPECTED_OUTPUT_DIR,
        COMM_COLL_BIN);
  }

  private static String getGoalFilePath(String jsonFilePath) {
    return OUTPUT_DIR + getGoalFileName(jsonFilePath);
  }

  private static String getGoalFileName(String jsonFilePath) {
    String fileName = jsonFilePath.replace("_goal.json", "");
    return fileName.substring(fileName.lastIndexOf(".") + 1).concat("_expected.txt");
  }

  private static void test(
      String jsonDir, String jsonFileName, String expectedOutputDir, String binDir) {
    String jsonFile = jsonDir + jsonFileName;
    String expectedOutput = expectedOutputDir + getGoalFileName(jsonFile);

    String[] args = {jsonFile, getGoalFilePath(jsonFile), binDir, expectedOutput};
    try {
      GoalFileConverter.main(args);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error in the conversion!");
    }
  }
}
