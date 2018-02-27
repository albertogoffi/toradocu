package org.toradocu.extractor;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.toradocu.conf.ClassDirsConverter;
import org.toradocu.conf.Configuration;
import org.toradocu.testlib.ToradocuJavaCompiler;
import org.toradocu.util.Reflection;

/**
 * Tests {@code JavadocExtractor} on the example class example.AClass in src/test/resources/example.
 */
public class JavadocExtractorOnEnumTest {

  private static final String EXAMPLE_SRC = "src/test/resources";
  private static final String TARGET_CLASS = "example.AnEnum";
  private static DocumentedType documentedType;
  private static List<DocumentedExecutable> members;

  @BeforeClass
  public static void setUp() throws IOException, ClassNotFoundException {
    compileSources();
    documentedType = runJavadocExtractor();
    members = documentedType.getDocumentedExecutables();
  }

  @Test
  public void classUnderAnalysis() throws ClassNotFoundException {
    final Class<?> targetClass = Reflection.getClass(TARGET_CLASS);
    final String docTypeClassName = documentedType.getDocumentedClass().getName();
    final String targetClassName = targetClass.getName();
    assertThat(docTypeClassName, is(equalTo(targetClassName)));
  }

  @Test
  public void numberOfExecutableMembers() {
    assertThat(members.size(), is(1));
  }

  @Test
  public void methodIsEven() {
    DocumentedExecutable member = members.get(0);
    assertThat(member.isConstructor(), is(false));

    final List<DocumentedParameter> parameters = member.getParameters();
    assertThat(parameters.size(), is(1));
    final DocumentedParameter parameter = parameters.get(0);
    assertThat(parameter.getName(), is("num"));
    assertThat(parameter.getType(), is(Integer.class));
    assertThat(parameter.isNullable(), is(nullValue()));

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags.size(), is(1));
    final ParamTag paramTag = paramTags.get(0);
    assertThat(paramTag.getParameter(), is(equalTo(parameter)));
    assertThat(paramTag.getComment().getText(), is("must not be null"));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag.getComment().getText(), is("true if num is even, false otherwise"));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags, is(empty()));

    assertThat(member.getName(), is("isEven"));
    assertThat(member.getSignature(), is("isEven(java.lang.Integer num)"));
    assertThat(member.toString(), is("public boolean example.AnEnum.isEven(java.lang.Integer)"));
    assertThat(member.getDeclaringClass().getName(), is("example.AnEnum"));
  }

  private static DocumentedType runJavadocExtractor()
      throws ClassNotFoundException, FileNotFoundException, MalformedURLException {
    final URL url = Paths.get(EXAMPLE_SRC).toUri().toURL();
    Configuration.INSTANCE.classDirs = Collections.singletonList(url);
    final JavadocExtractor javadocExtractor = new JavadocExtractor();
    return javadocExtractor.extract(TARGET_CLASS, EXAMPLE_SRC);
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
    boolean compilationOK = ToradocuJavaCompiler.run(sourceFiles);
    if (!compilationOK) {
      fail("Error(s) during compilation of test source files.");
    }
    Configuration.INSTANCE.classDirs = new ClassDirsConverter().convert(examplePath);
  }
}
