package org.toradocu.testlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.toradocu.util.Stats;

/**
 * TestSuiteStats computes the mean and standard deviation of the precision and recall of a
 * collection of testStats.
 */
class TestSuiteStats {

  /** A list of statistics for individual test cases in a test suite. */
  private final List<Stats> testStats = Collections.synchronizedList(new ArrayList<>());

  private double precision = 0, recall = 0, precisionStdDeviation = 0, recallStdDeviation = 0;
  private int totalNumConditions = 0;

  /**
   * Adds a test case to the list of test cases used to compute statistics for the test suite. Added
   * test cases are not incorporated into the test suite statistics until computeResults is called.
   *
   * @param stats the statistics for the test case
   */
  void addStats(Stats stats) {
    testStats.add(stats);
  }

  /**
   * Computes statistics for this test suite, incorporating statistics from any added test cases.
   */
  void computeResults() {
    this.precision = computePrecision();
    this.precisionStdDeviation = computePrecisionStdDeviation();
    this.recall = computeRecall();
    this.recallStdDeviation = computeRecallStdDeviation();
    this.totalNumConditions = computeTotalNumConditions();
  }

  /**
   * Returns the average precision of the testStats.
   *
   * @return the average precision of the testStats
   */
  double getPrecision() {
    return precision;
  }

  /**
   * Returns the average recall of the testStats.
   *
   * @return the average recall of the testStats
   */
  double getRecall() {
    return recall;
  }

  /**
   * Returns the standard deviation of the precision of the testStats.
   *
   * @return the standard deviation of the precision of the testStats
   */
  double getPrecisionStdDeviation() {
    return precisionStdDeviation;
  }

  /**
   * Returns the standard deviation of the recall of the testStats.
   *
   * @return the standard deviation of the recall of the testStats
   */
  double getRecallStdDeviation() {
    return recallStdDeviation;
  }

  /**
   * Returns the F-measure of the testStats.
   *
   * @return the F-measure of the testStats
   */
  double getFMeasure() {
    return (2 * getPrecision() * getRecall()) / (getPrecision() + getRecall());
  }

  /**
   * Returns the total number of conditions in the testStats.
   *
   * @return the total number of conditions in the testStats
   */
  int getTotalNumConditions() {
    return totalNumConditions;
  }

  /**
   * Computes and returns the average precision of the testStats.
   *
   * @return the average precision of the testStats
   */
  private double computePrecision() {
    double precision = 0;
    for (Stats testStat : testStats) {
      precision += testStat.getPrecision();
    }
    return precision / testStats.size();
  }

  /**
   * Computes and returns the average recall of the testStats.
   *
   * @return the average recall of the testStats
   */
  private double computeRecall() {
    double recall = 0;
    for (Stats testStat : testStats) {
      recall += testStat.getRecall();
    }
    return recall / testStats.size();
  }

  /**
   * Computes and returns the standard deviation of the precision of the testStats.
   *
   * @return the standard deviation of the precision of the testStats
   */
  private double computePrecisionStdDeviation() {
    double deviation = 0;
    for (Stats testStat : testStats) {
      deviation += Math.pow(testStat.getPrecision() - precision, 2);
    }
    return Math.sqrt(deviation / testStats.size());
  }

  /**
   * Computes and returns the standard deviation of the recall of the testStats.
   *
   * @return the standard deviation of the recall of the testStats
   */
  private double computeRecallStdDeviation() {
    double deviation = 0;
    for (Stats testStat : testStats) {
      deviation += Math.pow(testStat.getRecall() - recall, 2);
    }
    return Math.sqrt(deviation / testStats.size());
  }

  /**
   * Computes and returns the total number of conditions in the testStats.
   *
   * @return the total number of conditions in the testStats
   */
  private int computeTotalNumConditions() {
    int conditions = 0;
    for (Stats testStat : testStats) {
      conditions += testStat.getNumberOfConditions();
    }
    return conditions;
  }
}
