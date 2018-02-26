package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomJGraphT extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/JGraphT";
  private static final String EXPECTED = "src/test/resources/JGrapht-0.9.2/";

  @Test
  public void testCompleteGraphGenerator() {
    test("org.jgrapht.generate.CompleteGraphGenerator");
  }

  @Test
  public void testGraphDelegator() {
    test("org.jgrapht.graph.GraphDelegator");
  }

  @Test
  public void testGraphs() {
    test("org.jgrapht.Graphs");
  }

  @Test
  public void testKShortestPaths() {
    test("org.jgrapht.alg.KShortestPaths");
  }

  @Test
  public void testLinearGraphGenerator() {
    test("org.jgrapht.generate.LinearGraphGenerator");
  }

  @Test
  public void testAbstractGraph() {
    test("org.jgrapht.graph.AbstractGraph");
  }

  @Test
  public void testGraph() {
    test("org.jgrapht.Graph");
  }

  @Test
  public void testEmptyGraphGenerator() {
    test("org.jgrapht.generate.EmptyGraphGenerator");
  }

  @Test
  public void testDirectedSimpleCycles() {
    test("org.jgrapht.alg.cycle.DirectedSimpleCycles");
  }

  @Test
  public void testAbstractPathElementList() {
    test("org.jgrapht.alg.AbstractPathElementList");
  }

  @Test
  public void testPatonCycleBase() {
    test("org.jgrapht.alg.cycle.PatonCycleBase");
  }

  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
