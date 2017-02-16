package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;

public class PrecisionRecallJGraphT extends AbstractPrecisionRecallTestSuite {

  private static final String JGRAPHT_SRC = "src/test/resources/src/jgrapht-core-0.9.2-sources/";
  private static final String JGRAPHT_BIN = "src/test/resources/bin/jgrapht-core-0.9.2.jar";
  private static final String JGRAPHT_GOAL_DIR =
      "src/test/resources/goal-output/jgrapht-core-0.9.2/";

  public PrecisionRecallJGraphT() {
    super(JGRAPHT_SRC, JGRAPHT_BIN, JGRAPHT_GOAL_DIR);
  }

  @Test
  public void testAbstractGraph() throws Exception {
    test("org.jgrapht.graph.AbstractGraph", 1, 0.5, 1, 1, 1, 1);
  }

  @Test
  public void testGraph() throws Exception {
    test("org.jgrapht.Graph", 0.75, 0.333, 1, 1, 1, 1);
  }

  @Test
  public void testGraphs() throws Exception {
    test("org.jgrapht.Graphs", 1, 0, 1, 1, 1, 1);
  }

  @Test
  public void testLinearGraphGenerator() throws Exception {
    test("org.jgrapht.generate.LinearGraphGenerator", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testEmptyGraphGenerator() throws Exception {
    test("org.jgrapht.generate.EmptyGraphGenerator", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testKShortestPaths() throws Exception {
    test("org.jgrapht.alg.KShortestPaths", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testDirectedSimpleCycles() throws Exception {
    test("org.jgrapht.alg.cycle.DirectedSimpleCycles", 1, 1, 1, 1, 1, 1);
  }

  @Test
  public void testAbstractPathElementList() throws Exception {
    test("org.jgrapht.alg.AbstractPathElementList", 1, 0.666, 1, 1, 1, 1);
  }
}
