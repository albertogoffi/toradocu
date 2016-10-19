package org.toradocu;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.TestCaseStats;

public class PrecisionRecallJGraphT extends AbstractPrecisionRecallTestSuite {

  private static final String JGRAPHT_SRC = "src/test/resources/src/jgrapht-core-0.9.2-sources";
  private static final String JGRAPHT_BIN = "src/test/resources/bin/jgrapht-core-0.9.2.jar";
  private static final String JGRAPHT_EXPECTED_DIR = "src/test/resources/JGraphT/";

  public PrecisionRecallJGraphT() {
    super(JGRAPHT_SRC, JGRAPHT_BIN, JGRAPHT_EXPECTED_DIR);
  }

  @Test
  public void abstractGraphTest() throws Exception {
    test("org.jgrapht.graph.AbstractGraph", 1.0, 0.5);
  }

  @Test
  public void graphTest() throws Exception {
    test("org.jgrapht.Graph", 0.8, 0.4);
  }

  @Test
  public void graphsTest() throws Exception {
    test("org.jgrapht.Graphs", 0.0, 0.0);
  }

  @Test
  public void linearGraphGeneratorTest() throws Exception {
    test("org.jgrapht.generate.LinearGraphGenerator", 1.0, 1.0);
  }

  @Test
  public void emptyGraphGeneratorTest() throws Exception {
    test("org.jgrapht.generate.EmptyGraphGenerator", 1.0, 1.0);
  }

  @Test
  public void kShortestPathsTest() throws Exception {
    test("org.jgrapht.alg.KShortestPaths", 1.0, 1.0);
  }

  @Test
  public void directedSimpleCyclesTest() throws Exception {
    test("org.jgrapht.alg.cycle.DirectedSimpleCycles", 1.0, 1.0);
  }

  @Test
  public void abstractPathElementListTest() throws Exception {
    test("org.jgrapht.alg.AbstractPathElementList", 1.0, 0.666);
  }
}
