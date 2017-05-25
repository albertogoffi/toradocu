package org.toradocu.extractor;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
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

// TODO Add more assertions (about other methods in the example test class).

/**
 * Tests {@code JavadocExtractor} on the example class example.AClass in src/test/resources/example
 */
public class JavadocExtractorTest {

  private static final String EXAMPLE_SRC = "src/test/resources";
  private static List<ExecutableMember> members;
  private static Class<?> stringClass;

  @BeforeClass
  public static void setUp() throws IOException, ClassNotFoundException {
    stringClass = Class.forName("java.lang.String");
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
    assertThat(member.isConstructor(), is(true));

    final List<Parameter> parameters = member.getParameters();
    assertThat(parameters, is(empty()));

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags, is(empty()));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag, is(nullValue()));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags.size(), is(1));
    final ThrowsTag throwsTag = throwsTags.get(0);
    assertThat(
        throwsTag.getException(), is(equalTo(Class.forName("java.lang.NullPointerException"))));
  }

  @Test
  public void constructorAClass2() throws ClassNotFoundException {
    ExecutableMember member = members.get(1);
    assertThat(member.isConstructor(), is(true));

    final List<Parameter> parameters = member.getParameters();
    assertThat(parameters.size(), is(1));
    Parameter parameter = parameters.get(0);
    assertThat(parameter.getName(), is("x"));
    assertThat(parameter.getType(), is(stringClass));

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags.size(), is(1));
    final ParamTag paramTag = paramTags.get(0);
    assertThat(paramTag.getParameter(), is(equalTo(parameter)));
    assertThat(paramTag.getComment(), is("must not be null nor empty"));
    assertThat(paramTag.getCondition(), is(emptyString()));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag, is(nullValue()));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags.size(), is(2));
  }

  @Test
  public void methodFoo() throws ClassNotFoundException {
    ExecutableMember member = members.get(2);
    assertThat(member.isConstructor(), is(false));

    final List<Parameter> parameters = member.getParameters();
    assertThat(parameters.size(), is(1));
    final Parameter parameter = parameters.get(0);
    assertThat(parameter.getName(), is("array"));
    assertThat(parameter.getType(), is(Object[].class));

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags.size(), is(1));
    final ParamTag paramTag = paramTags.get(0);
    assertThat(paramTag.getParameter(), is(equalTo(parameter)));
    assertThat(paramTag.getComment(), is("an array of objects, must not be null"));
    assertThat(paramTag.getCondition(), is(emptyString()));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag.getComment(), is("0 always"));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags, is(empty()));
  }

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
