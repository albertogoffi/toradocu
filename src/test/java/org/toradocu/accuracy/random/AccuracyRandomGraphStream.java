package org.toradocu.accuracy.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

/** Created by arianna on 07/08/17. */
public class AccuracyRandomGraphStream extends AbstractPrecisionRecallTestSuite {

  private static final String GRAPHSTREAM_SRC = "src/test/resources/src/gs-core-1.3-sources";
  private static final String GRAPHSTREAM_BIN = "src/test/resources/bin/gs-core-1.3.jar";
  private static final String GRAPHSTREAM_GOAL_DIR =
      "src/test/resources/goal-output/random/gs-core-1.3/";

  public AccuracyRandomGraphStream() {
    super(GRAPHSTREAM_SRC, GRAPHSTREAM_BIN, GRAPHSTREAM_GOAL_DIR);
  }

  @Test
  public void testGradientFactory() throws Exception {
    test("org.graphstream.ui.swingViewer.util.GradientFactory", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testNetStreamDecoder() throws Exception {
    test("org.graphstream.stream.netstream.NetStreamDecoder", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testGraphMetrics() throws Exception {
    test("org.graphstream.ui.swingViewer.util.GraphMetrics", 1, 1, 1, 1, 0.666, 1);
  }
}
