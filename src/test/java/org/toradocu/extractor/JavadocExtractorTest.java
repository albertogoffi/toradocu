package org.toradocu.extractor;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.toradocu.conf.ClassDirsConverter;
import org.toradocu.conf.Configuration;
import org.toradocu.testlib.Compiler;

/**
 * Tests {@code JavadocExtractor} on the example class example.AClass in src/test/resources/example
 */
public class JavadocExtractorTest {

  private static final String EXAMPLE_SRC = "src/test/resources";
  private static List<ExecutableMember> members;

  @BeforeClass
  public static void setUp() throws IOException, ClassNotFoundException {
    compileSources();
    members = runJavadocExtractor();
  }

  @Test
  public void numberOfExecutableMembers() {
    assertThat(members.size(), is(4));
  }

  @Test // Constructor AClass().
  public void constructorAClass1() throws ClassNotFoundException {
    ExecutableMember member = members.get(0);

    final List<Parameter> parameters1 = member.getParameters();
    assertThat(parameters1, is(empty()));

    final List<ParamTag> paramTags1 = member.paramTags();
    assertThat(paramTags1, is(empty()));

    final ReturnTag returnTag1 = member.returnTag();
    assertThat(returnTag1, is(nullValue()));

    final List<ThrowsTag> throwsTags1 = member.throwsTags();
    assertThat(throwsTags1.size(), is(1));
    final ThrowsTag throwsTag = throwsTags1.get(0);
    assertThat(
        throwsTag.getException(), is(equalTo(Class.forName("java.lang.NullPointerException"))));
  }

  @Test
  public void constructorAClass2() throws ClassNotFoundException {
    ExecutableMember member = members.get(1);

    final List<Parameter> parameters2 = member.getParameters();
    assertThat(parameters2.size(), is(1));
    final Parameter parameter1 = parameters2.get(0);

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags.size(), is(1));
    final ParamTag paramTag = paramTags.get(0);
    Parameter parameter = paramTag.getParameter();
    assertThat(parameter.getName(), is("x"));
    assertThat(parameter.getType(), is(Class.forName("java.lang.String")));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag, is(nullValue()));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags.size(), is(2));
  }

  // TODO Add more assertions (about other methods in the example test class).

  private static List<ExecutableMember> runJavadocExtractor()
      throws ClassNotFoundException, FileNotFoundException {
    final JavadocExtractor javadocExtractor = new JavadocExtractor();
    return javadocExtractor.extract("example.AClass", EXAMPLE_SRC);
  }

  private static void compileSources() throws IOException {
    final String examplePath = EXAMPLE_SRC + "/example";
    final File sourceDir = new File(examplePath);
    List<String> sourceFiles =
        Files.walk(sourceDir.toPath())
            .filter(p -> p.getFileName().toString().endsWith(".java"))
            .map(Path::toString)
            .collect(toList());
    if (sourceFiles.isEmpty()) {
      fail("No Java files to compile found in " + sourceDir);
    }
    boolean compilationOK = Compiler.run(sourceFiles);
    if (!compilationOK) {
      fail("Error(s) during compilation of test source files.");
    }
    Configuration.INSTANCE.classDirs = new ClassDirsConverter().convert(examplePath);
  }
}
