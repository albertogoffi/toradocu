package org.toradocu.testlib;

public class TestCaseStats {

  private int correct = 0, wrong = 0, missing = 0;
  private final String identifier;

  TestCaseStats(String identifier) {
    this.identifier = identifier;
  }

  void incrementCorrect() {
    ++correct;
  }

  void incrementWrong() {
    ++wrong;
  }

  void incrementMissig() {
    missing++;
  }

  public int getCorrect() {
    return correct;
  }

  public int getMissing() {
    return missing;
  }

  public int getWrong() {
    return wrong;
  }

  public int numberOfConditions() {
    return correct + wrong + missing;
  }
}
