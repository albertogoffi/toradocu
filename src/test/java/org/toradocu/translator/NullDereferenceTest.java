package org.toradocu.translator;

import static org.hamcrest.CoreMatchers.equalTo;
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

public class NullDereferenceTest {
  private static final Path resourcesPath = Paths.get("src", "test", "resources");
  private static final Path expectedOutput =
      Paths.get(
          "src/test/resources/expected-output/example.nulldereference.ResourceManager_goal.json");
  private static final Path actualOutput =
      Paths.get("example.nulldereference.ResourceManager_out.json");

  private static Path sourcePath;

  @BeforeClass
  public static void setUp() throws Exception {
    sourcePath = Paths.get(resourcesPath.toString(), "example/nulldereference", "Resource.java");

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    int compilerExitCode = compiler.run(null, null, null, sourcePath.toString());
    assertThat(compilerExitCode, is(0));

    sourcePath =
        Paths.get(resourcesPath.toString(), "example/nulldereference", "ResourceManager.java");

    compilerExitCode = compiler.run(null, null, null, sourcePath.toString());
    assertThat(compilerExitCode, is(0));
  }

  @Test
  public void generatedSpecificationTest() throws Exception {
    String targetPath = sourcePath.getParent().getParent().getParent().toString();

    String[] toradocuArgs =
        new String[] {
          "--target-class",
          "example.nulldereference.ResourceManager",
          "--condition-translator-output",
          actualOutput.toString(),
          "--class-dir",
          targetPath,
          "--source-dir",
          targetPath,
          "--expected-output",
          expectedOutput.toString()
        };
    List<String> argsList = new ArrayList<>(Arrays.asList(toradocuArgs));
    Toradocu.main(argsList.toArray(new String[0]));
    assertTrue(Files.exists(actualOutput));

    Type listType = new TypeToken<List<JsonOutput>>() {}.getType();
    try (BufferedReader actualOutputReader = Files.newBufferedReader(actualOutput);
        BufferedReader expectedOutputReader = Files.newBufferedReader(expectedOutput)) {
      List<JsonOutput> actualSpecs = GsonInstance.gson().fromJson(actualOutputReader, listType);
      List<JsonOutput> expectedSpecs = GsonInstance.gson().fromJson(expectedOutputReader, listType);
      assertThat(actualSpecs.size(), is(equalTo(expectedSpecs.size())));

      for (int i = 0; i < actualSpecs.size(); i++) {
        JsonOutput actualSpec = actualSpecs.get(i);
        JsonOutput expectedSpec = expectedSpecs.get(i);
        if (actualSpec.throwsTags != null) {
          for (int j = 0; j < actualSpec.throwsTags.size(); j++) {
            assertThat(
                actualSpec.throwsTags.get(j).getCondition().replaceAll("\\s+", ""),
                is(
                    equalTo(
                        expectedSpec
                            .throwsTags
                            .get(j)
                            .getCondition()
                            .replaceAll("\\s+", "")
                            .trim())));
          }
        }
        if (actualSpec.paramTags != null) {
          for (int j = 0; j < actualSpec.paramTags.size(); j++) {
            assertThat(
                actualSpec.paramTags.get(j).getCondition().replaceAll("\\s+", ""),
                is(equalTo(expectedSpec.paramTags.get(j).getCondition().replaceAll("\\s+", ""))));
          }
        }
        if (actualSpec.returnTag != null)
          assertThat(
              actualSpec.returnTag.getCondition().replaceAll("\\s+", ""),
              is(equalTo(expectedSpec.returnTag.getCondition().replaceAll("\\s+", ""))));
      }
    } finally {
      Files.delete(actualOutput);
    }
  }
}
