package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallVariablesComparison extends AbstractPrecisionRecallTestSuite {

  private static final String VARIABLES_COMPARISON_MOCK_SRC =
      "src/test/resources/src/variables-comparison-mock-src/src/";
  private static final String VARIABLES_COMPARISON_MOCK_BIN =
      "src/test/resources/bin/variables-comparison-mock.jar";
  private static final String VARIABLES_COMPARISON_MOCK_GOAL_DIR =
      "src/test/resources/goal-output/variables-comparison-mock/";

  public PrecisionRecallVariablesComparison() {
    super(
        VARIABLES_COMPARISON_MOCK_SRC,
        VARIABLES_COMPARISON_MOCK_BIN,
        VARIABLES_COMPARISON_MOCK_GOAL_DIR);
  }

  @Test
  public void testVariablesComparison() throws Exception {
    test("comparing.VariablesComparison", 1, 1, 1, 1, 1, 1);
  }
}
