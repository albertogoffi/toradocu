package org.toradocu;

import org.junit.Test;

/**
 * Collects tests that expose bugs. These tests should be moved to the precision/recall test suite.
 */
public class RegressionTests {

  @Test
  public void polynomialFunctionLagrangeFormTest() {
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm",
          "--class-dir",
          "src/test/resources/bin/commons-math3-3.6.1.jar",
          "--source-dir",
          "src/test/resources/src/commons-math3-3.6.1-src/src/main/java",
          "--oracle-generation",
          "false",
          "--debug"
        };
    Toradocu.main(toradocuArgs);
  }

  @Test
  public void fastCosineTransformerTest() {
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "org.apache.commons.math3.transform.FastCosineTransformer",
          "--class-dir",
          "src/test/resources/bin/commons-math3-3.6.1.jar",
          "--source-dir",
          "src/test/resources/src/commons-math3-3.6.1-src/src/main/java",
          "--oracle-generation",
          "false"
        };
    Toradocu.main(toradocuArgs);
  }

  @Test
  public void fastHadamardTransformerTest() {
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "org.apache.commons.math3.transform.FastHadamardTransformer",
          "--class-dir",
          "src/test/resources/bin/commons-math3-3.6.1.jar",
          "--source-dir",
          "src/test/resources/src/commons-math3-3.6.1-src/src/main/java",
          "--oracle-generation",
          "false"
        };
    Toradocu.main(toradocuArgs);
  }

  @Test
  public void BCEUtilTest() {
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "plume.BCELUtil",
          "--class-dir",
          "src/test/resources/bin/plume-lib-1.1.0.jar",
          "--source-dir",
          "src/test/resources/src/plume-lib-1.1.0/java/src",
          "--oracle-generation",
          "false"
        };
    Toradocu.main(toradocuArgs);
  }

  // Issue #81. Still to fix.
  public void freeColTest() {
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "net.sf.freecol.FreeCol",
          "--class-dir",
          "src/test/resources/bin/freecol-0.11.6.jar",
          "--source-dir",
          "src/test/resources/src/freecol-0.11.6/src",
          "--oracle-generation",
          "false"
        };
    Toradocu.main(toradocuArgs);
  }
}
