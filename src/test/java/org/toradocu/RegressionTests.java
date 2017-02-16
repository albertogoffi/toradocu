package org.toradocu;

import org.junit.Test;

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
          "false"
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
}
