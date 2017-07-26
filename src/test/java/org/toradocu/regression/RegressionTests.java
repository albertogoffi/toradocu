package org.toradocu.regression;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.toradocu.Toradocu;

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
  public void bceUtilTest() {
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

  @Test
  public void issue79() {
    // https://github.com/albertogoffi/toradocu/issues/79
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "com.google.common.base.SmallCharMatcher",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "false",
          "--randoop-specs",
          "randoop-specs/1.txt"
        };
    Toradocu.main(toradocuArgs);

    toradocuArgs =
        new String[] {
          "--target-class",
          "com.google.common.collect.HashBiMap",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "false",
          "--randoop-specs",
          "randoop-specs/2.txt"
        };
    Toradocu.main(toradocuArgs);
  }

  @Test
  public void freeColTest() {
    // Issue #81: https://github.com/albertogoffi/toradocu/issues/81
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "net.sf.freecol.FreeCol",
          "--class-dir",
          "src/test/resources/bin/freecol-0.11.6.jar:src/test/resources/bin/commons-cli-1.3.1.jar",
          "--source-dir",
          "src/test/resources/src/freecol-0.11.6/src",
          "--oracle-generation",
          "false"
        };
    Toradocu.main(toradocuArgs);
  }

  @Test
  public void futuresTest() {
    // Issue #85: https://github.com/albertogoffi/toradocu/issues/85
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "com.google.common.util.concurrent.Futures",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "false"
        };
    Toradocu.main(toradocuArgs);
  }

  @Test
  public void issue98() throws IOException {
    // Issue #98: https://github.com/albertogoffi/toradocu/issues/98
    final String RANDOOP_SPEC = "randoop_specs_issue98.json";
    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "org.apache.commons.collections4.BoundedMap",
          "--class-dir",
          "src/test/resources/bin/commons-collections4-4.1.jar",
          "--source-dir",
          "src/test/resources/src/commons-collections4-4.1-src/src/main/java/",
          "--oracle-generation",
          "false",
          "--randoop-specs",
          RANDOOP_SPEC
        };
    Toradocu.main(toradocuArgs);
    // There are no specifications in the file, and so file should not be written
    assertTrue("file should not be written", Files.notExists(Paths.get(RANDOOP_SPEC)));
  }

  @Test
  public void issue51() {
    // Issue #51: https://github.com/albertogoffi/toradocu/issues/51
    Toradocu.main(
        new String[] {
          "--target-class",
          "com.google.common.collect.Ordering",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "false"
        });
  }
}
