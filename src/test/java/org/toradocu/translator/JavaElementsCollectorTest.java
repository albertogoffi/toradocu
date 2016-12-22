package org.toradocu.translator;

import org.junit.Test;
import org.toradocu.Toradocu;

public class JavaElementsCollectorTest {

  /**
   * Regression test for <a href="https://github.com/albertogoffi/toradocu/issues/51">issue #51</a>.
   */
  @Test
  public void issue51() {
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
