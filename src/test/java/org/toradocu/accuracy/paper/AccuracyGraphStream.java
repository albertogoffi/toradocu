package org.toradocu.accuracy.paper;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class AccuracyGraphStream extends AbstractPrecisionRecallTestSuite {

  private static final String GRAPHSTREAM_SRC = "src/test/resources/src/gs-core-1.3-sources";
  private static final String GRAPHSTREAM_BIN = "src/test/resources/bin/gs-core-1.3.jar";
  private static final String GRAPHSTREAM_GOAL_DIR = "src/test/resources/goal-output/gs-core-1.3/";

  public AccuracyGraphStream() {
    super(GRAPHSTREAM_SRC, GRAPHSTREAM_BIN, GRAPHSTREAM_GOAL_DIR);
  }

  @Test
  public void testGraphMetrics() throws Exception {
    test("org.graphstream.ui.swingViewer.util.GraphMetrics", 1, 1, 1, 1, 0.666, 1);
  }

  @Test
  public void testFixedArrayList() throws Exception {
    test("org.graphstream.util.set.FixedArrayList", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testGraphicElement() throws Exception {
    test("org.graphstream.ui.graphicGraph.GraphicElement", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testEnvironment() throws Exception {
    test("org.graphstream.util.Environment", 1, 1, 1, 1, 1, 0);
  }

  @Test
  public void testPath() throws Exception {
    test("org.graphstream.graph.Path", 1, 1, 1, 1, 1, 0);
  }

  @Test
  public void testSingleGraph() throws Exception {
    test("org.graphstream.graph.implementations.SingleGraph", 1, 1, 0, 1, 1, 1);
  }

  @Test
  public void testMultiGraph() throws Exception {
    test("org.graphstream.graph.implementations.MultiGraph", 1, 1, 1, 1, 1, 1);
  }
}
