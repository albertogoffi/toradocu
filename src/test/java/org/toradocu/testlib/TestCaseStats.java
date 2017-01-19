package org.toradocu.testlib;

public class TestCaseStats {

  private int correct = 0, wrong = 0, missing = 0;

  public void incrementCorrect() {
    ++correct;
  }

  public void incrementWrong() {
    ++wrong;
  }

  public void incrementMissig() {
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
