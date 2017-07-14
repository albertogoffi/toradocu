package org.toradocu.translator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.toradocu.Toradocu;
import org.toradocu.output.util.JsonOutput;
import org.toradocu.util.GsonInstance;

/**
 * The AClass Javadoc conditions involve every case the Matcher can encounter. The output of the
 * translators must be equal to the goal file.
 */
public class MatcherAndTranslatorsTest {
  private static final Path resourcesPath = Paths.get("src", "test", "resources");
  private static final Path expectedOutput =
      Paths.get("src/test/resources/expected-output/example.AClass_goal.json");
  private static final Path actualOutput = Paths.get("example.AClass_out.json");

  private static Path sourcePath;

  @BeforeClass
  public static void setUp() throws Exception {
    sourcePath = Paths.get(resourcesPath.toString(), "example", "AClass.java");

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    int compilerExitCode = compiler.run(null, null, null, sourcePath.toString());
    assertThat(compilerExitCode, is(0));
  }

  @Test
  public void name() throws Exception {
    String targetPath = sourcePath.getParent().getParent().toString();

    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "example.AClass",
          "--condition-translator-output",
          actualOutput.toString(),
          "--class-dir",
          targetPath,
          "--source-dir",
          targetPath
        };
    List<String> argsList = new ArrayList<>(Arrays.asList(toradocuArgs));
    Toradocu.main(argsList.toArray(new String[0]));
    assertTrue(Files.exists(actualOutput));

    Type listType = new TypeToken<List<JsonOutput>>() {}.getType();
    try (BufferedReader actualOutputReader = Files.newBufferedReader(actualOutput);
        BufferedReader expectedOutputReader = Files.newBufferedReader(expectedOutput); ) {
      List<JsonOutput> actualSpecs = GsonInstance.gson().fromJson(actualOutputReader, listType);
      List<JsonOutput> expectedSpecs = GsonInstance.gson().fromJson(expectedOutputReader, listType);
      assertThat(actualSpecs, is(expectedSpecs));
    }

    Files.delete(actualOutput);
  }
}
