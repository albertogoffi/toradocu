package org.toradocu.generator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.After;
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
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "true"
        });

    String outputDir = Toradocu.configuration.getAspectsOutputDir();
    File actualOutput = Paths.get(outputDir, "Aspect_1.java").toFile();
    File expectedOutput =
        Paths.get(getClass().getClassLoader().getResource("aspects/Aspect_1.java").toURI())
            .toFile();
    assertThat(FileUtils.contentEquals(actualOutput, expectedOutput), is(true));
  }

  @After
  public void deleteToradocuOutputDir() {
    FileUtils.deleteQuietly(new File(Toradocu.configuration.getAspectsOutputDir()));
  }
}
