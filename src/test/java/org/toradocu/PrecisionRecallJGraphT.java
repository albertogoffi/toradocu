package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;
import org.toradocu.testlib.TestCaseStats;

import java.util.Map;

public class PrecisionRecallJGraphT extends AbstractPrecisionRecallTestSuite {

  private static final String JGRAPHT_SRC = "src/test/resources/JGrapht";
  private static final String JGRAPHT_EXPECTED_DIR = "src/test/resources/JGrapht-0.9.2/";

  @Test
  public void abstractGraphTest() throws Exception {
    test("org.jgrapht.graph.AbstractGraph");
  }

  @Test
  public void abstractPathElementListTest() throws Exception {
    test("org.jgrapht.alg.AbstractPathElementList");
  }

  @Test
  public void directedSimpleCyclesTest() throws Exception {
    test("org.jgrapht.alg.cycle.DirectedSimpleCycles");
  }

  @Test
  public void emptyGraphGeneratorTest() throws Exception {
    test("org.jgrapht.generate.EmptyGraphGenerator");
  }

  @Test
  public void graphTest() throws Exception {
    test("org.jgrapht.Graph");
  }

  @Test
  public void graphsTest() throws Exception {
    test("org.jgrapht.Graphs");
  }

  @Test
  public void kShortestPathsTest() throws Exception {
    test("org.jgrapht.alg.KShortestPaths");
  }

  @Test
  public void linearGraphGeneratorTest() throws Exception {
    test("org.jgrapht.generate.LinearGraphGenerator");
  }

  private Map<String, TestCaseStats> test(String targetClass) {
    Map<String, TestCaseStats> stats =
        PrecisionRecallTest.test(targetClass, JGRAPHT_SRC, JGRAPHT_EXPECTED_DIR);
    testSuiteStats.addTest(stats);
    return stats;
  }
}
