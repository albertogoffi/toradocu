package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallGraphStream extends AbstractPrecisionRecallTestSuite {

  private static final String GRAPHSTREAM_SRC = "src/test/resources/src/gs-core-1.3-sources";
  private static final String GRAPHSTREAM_BIN = "src/test/resources/bin/gs-core-1.3.jar";
  private static final String GRAPHSTREAM_GOAL_DIR = "src/test/resources/goal-output/gs-core-1.3/";

  public PrecisionRecallGraphStream() {
    super(GRAPHSTREAM_SRC, GRAPHSTREAM_BIN, GRAPHSTREAM_GOAL_DIR);
  }

  @Test
  public void testSingleGraph() throws Exception {
    test("org.graphstream.graph.implementations.SingleGraph", 0, 0, 0.333, 0.333, 1, 0);
  }

  @Test
  public void testMultiGraph() throws Exception {
    test("org.graphstream.graph.implementations.MultiGraph", 0, 0, 0.333, 0.333, 1, 0);
  }
}
