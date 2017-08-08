package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

/** Created by arianna on 07/08/17. */
public class AccuracyRandomGraphStream extends AbstractPrecisionRecallTestSuite {

  private static final String GRAPHSTREAM_SRC = "src/test/resources/src/gs-core-1.3-sources";
  private static final String GRAPHSTREAM_BIN = "src/test/resources/bin/gs-core-1.3.jar";
  private static final String GRAPHSTREAM_GOAL_DIR = "src/test/resources/goal-output/gs-core-1.3/";

  public AccuracyRandomGraphStream() {
    super(GRAPHSTREAM_SRC, GRAPHSTREAM_BIN, GRAPHSTREAM_GOAL_DIR);
  }

  @Test
  public void testGradientFactory() throws Exception {
    test("org.graphstream.ui.swingViewer.util.GradientFactory", 1, 1, 1, 1, 1, 1);
  }
}
