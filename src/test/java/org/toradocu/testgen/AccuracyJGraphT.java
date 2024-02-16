package org.toradocu.testgen;

import org.junit.Test;
import org.toradocu.testlib.TestgenTest;

public class AccuracyJGraphT extends TestgenTest {
  private static final String JGRAPHT_SRC = "src/test/resources/src/jgrapht-core-0.9.2-sources/";
  private static final String JGRAPHT_BIN = "src/test/resources/bin/jgrapht-core-0.9.2.jar";
  private static final String JGRAPHT_GOAL_DIR =
      "src/test/resources/goal-output/jgrapht-core-0.9.2/";

  public AccuracyJGraphT() {
    super(JGRAPHT_SRC, JGRAPHT_BIN, JGRAPHT_GOAL_DIR);
  }

  @Test
  public void testCompleteGraphGenerator() throws Exception {
    test("org.jgrapht.generate.CompleteGraphGenerator");
  }

  @Test
  public void testGraphDelegator() throws Exception {
    test("org.jgrapht.graph.GraphDelegator");
  }

  @Test
  public void testGraphs() throws Exception {
    test("org.jgrapht.Graphs");
  }

  @Test
  public void testKShortestPaths() throws Exception {
    test("org.jgrapht.alg.KShortestPaths");
  }

  @Test
  public void testLinearGraphGenerator() throws Exception {
    test("org.jgrapht.generate.LinearGraphGenerator");
  }

  @Test
  public void testAbstractGraph() throws Exception {
    test("org.jgrapht.graph.AbstractGraph");
  }

  @Test
  public void testGraph() throws Exception {
    test("org.jgrapht.Graph");
  }

  @Test
  public void testEmptyGraphGenerator() throws Exception {
    test("org.jgrapht.generate.EmptyGraphGenerator");
  }

  @Test
  public void testAbstractPathElementList() throws Exception {
    test("org.jgrapht.alg.AbstractPathElementList");
  }

  @Test
  public void testPatonCycleBase() throws Exception {
    test("org.jgrapht.alg.cycle.PatonCycleBase");
  }
}
