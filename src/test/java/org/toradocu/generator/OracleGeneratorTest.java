package org.toradocu.generator;

import org.junit.Test;
import org.toradocu.Toradocu;

public class OracleGeneratorTest {

  @Test
  public void oracleGeneratorTest() throws Exception {
    Toradocu.main(
        new String[] {
          "--target-class",
          "com.google.common.collect.ArrayListMultimap",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources"
        });
  }
}
