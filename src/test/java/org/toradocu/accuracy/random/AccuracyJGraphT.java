package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyJGraphT extends AbstractPrecisionRecallTestSuite {
  private static final String JGRAPHT_SRC = "src/test/resources/src/jgrapht-core-0.9.2-sources/";
  private static final String JGRAPHT_BIN = "src/test/resources/bin/jgrapht-core-0.9.2.jar";
  private static final String JGRAPHT_GOAL_DIR =
      "src/test/resources/goal-output/random/jgrapht-core-0.9.2/";

  public AccuracyJGraphT() {
    super(JGRAPHT_SRC, JGRAPHT_BIN, JGRAPHT_GOAL_DIR);
  }

  @Test
  public void testCompleteGraphGenerator() throws Exception {
    test("org.jgrapht.generate.CompleteGraphGenerator", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testGraphDelegator() throws Exception {
    test("org.jgrapht.graph.GraphDelegator", 1, 1, 1, 1, 1, 1);
  }
}
