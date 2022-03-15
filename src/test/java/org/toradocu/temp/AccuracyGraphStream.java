package org.toradocu.temp;

import org.junit.Test;
import org.toradocu.testlib.TestgenTest;

public class AccuracyGraphStream extends TestgenTest {

  private static final String GRAPHSTREAM_SRC = "src/test/resources/src/gs-core-1.3-sources";
  private static final String GRAPHSTREAM_BIN = "src/test/resources/bin/gs-core-1.3.jar";
  private static final String GRAPHSTREAM_GOAL_DIR = "src/test/resources/goal-output/gs-core-1.3/";

  public AccuracyGraphStream() {
    super(GRAPHSTREAM_SRC, GRAPHSTREAM_BIN, GRAPHSTREAM_GOAL_DIR);
  }

  @Test
  public void testGraphMetrics() throws Exception {
    test("org.graphstream.ui.swingViewer.util.GraphMetrics");
  }

  @Test
  public void testFixedArrayList() throws Exception {
    test("org.graphstream.util.set.FixedArrayList");
  }

  @Test
  public void testGraphicElement() throws Exception {
    test("org.graphstream.ui.graphicGraph.GraphicElement");
  }

  @Test
  public void testEnvironment() throws Exception {
    test("org.graphstream.util.Environment");
  }

  @Test
  public void testPath() throws Exception {
    test("org.graphstream.graph.Path");
  }

  @Test
  public void testSingleGraph() throws Exception {
    test("org.graphstream.graph.implementations.SingleGraph");
  }

  @Test
  public void testMultiGraph() throws Exception {
    test("org.graphstream.graph.implementations.MultiGraph");
  }
}
