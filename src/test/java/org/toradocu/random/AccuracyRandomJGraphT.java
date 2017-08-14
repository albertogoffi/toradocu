package org.toradocu.random;

import org.junit.Test;
import org.toradocu.testlib.AbstractPrecisionRecallTestSuite;
import org.toradocu.testlib.PrecisionRecallTest;

public class AccuracyRandomJGraphT extends AbstractPrecisionRecallTestSuite {

  private static final String SRC = "src/test/resources/JGraphT";
  private static final String EXPECTED = "src/test/resources/JGrapht-0.9.2/";

  @Test
  public void graphsTest() throws Exception {
    test("org.jgrapht.Graphs");
  }

  @Test
  public void kShortestPathsTest() throws Exception {
    test("org.jgrapht.alg.KShortestPaths");
  }

  @Test
  public void directedAcyclicGraphTest() throws Exception {
    test("org.jgrapht.experimental.dag.DirectedAcyclicGraph");
  }

  private void test(String clazz) {
    testSuiteStats.addTest(PrecisionRecallTest.test(clazz, SRC, EXPECTED));
  }
}
