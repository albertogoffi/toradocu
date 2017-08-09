package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

/** Created by arianna on 07/08/17. */
public class AccuracyRandomJGraphT extends AbstractPrecisionRecallTestSuite {
  private static final String JGRAPHT_SRC = "src/test/resources/src/jgrapht-core-0.9.2-sources/";
  private static final String JGRAPHT_BIN = "src/test/resources/bin/jgrapht-core-0.9.2.jar";
  private static final String JGRAPHT_GOAL_DIR =
      "src/test/resources/goal-output/jgrapht-core-0.9.2/";

  public AccuracyRandomJGraphT() {
    super(JGRAPHT_SRC, JGRAPHT_BIN, JGRAPHT_GOAL_DIR);
  }

  @Test
  public void testMinSourceSinkCut() throws Exception {
    test("org.jgrapht.alg.MinSourceSinkCut", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testRankingPathElementt() throws Exception {
    test("org.jgrapht.alg.RankingPathElement", 1, 1, 1, 1, 1, 1);
  }
}
