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
public class JavadocExtractorTest {

  private static final String EXAMPLE_SRC = "src/test/resources";
  private static final String TARGET_CLASS = "example.AClass";
  private static DocumentedType documentedType;
  private static List<DocumentedExecutable> members;
  private static Class<?> stringClass;
  private static Class<?> classClass;
  private static Class<?> collectionClass;

  @BeforeClass
  public static void setUp() throws IOException, ClassNotFoundException {
    stringClass = Class.forName("java.lang.String");
    classClass = Class.forName("java.lang.Class");
    collectionClass = Class.forName("java.util.Collection");
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
    assertThat(members.size(), is(18));
  }

  @Test
  public void constructorAClass1() throws ClassNotFoundException {
    DocumentedExecutable member = members.get(0);
    assertThat(member.isConstructor(), is(true));

    final List<DocumentedParameter> parameters = member.getParameters();
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
    DocumentedExecutable member = members.get(1);
    assertThat(member.isConstructor(), is(true));

    final List<DocumentedParameter> parameters = member.getParameters();
    assertThat(parameters.size(), is(1));
    DocumentedParameter parameter = parameters.get(0);
    assertThat(parameter.getName(), is("x"));
    assertThat(parameter.getType(), is(stringClass));
    assertThat(parameter.isNullable(), is(nullValue()));

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags.size(), is(1));
    final ParamTag paramTag = paramTags.get(0);
    assertThat(paramTag.getParameter(), is(equalTo(parameter)));
    assertThat(paramTag.getComment().getText(), is("must not be null nor empty"));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag, is(nullValue()));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags.size(), is(2));
  }

  @Test
  public void methodFoo() throws ClassNotFoundException {
    DocumentedExecutable member = members.get(2);
    assertThat(member.isConstructor(), is(false));

    final List<DocumentedParameter> parameters = member.getParameters();
    assertThat(parameters.size(), is(1));
    final DocumentedParameter parameter = parameters.get(0);
    assertThat(parameter.getName(), is("array"));
    assertThat(parameter.getType(), is(Object[].class));
    assertThat(parameter.isNullable(), is(false));

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags.size(), is(1));
    final ParamTag paramTag = paramTags.get(0);
    assertThat(paramTag.getParameter(), is(equalTo(parameter)));
    assertThat(paramTag.getComment().getText(), is("an array of objects, must not be null"));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag.getComment().getText(), is("0 always"));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags, is(empty()));

    assertThat(member.getName(), is("foo"));
    assertThat(member.getSignature(), is("foo(java.lang.Object[] array)"));
    assertThat(member.toString(), is("public double example.AClass.foo(java.lang.Object[])"));
    assertThat(member.getDeclaringClass().getName(), is("example.AClass"));
  }

  @Test
  public void methodCallAFriend() throws ClassNotFoundException {
    DocumentedExecutable member = members.get(4);
    assertThat(member.isConstructor(), is(false));

    final List<DocumentedParameter> parameters = member.getParameters();
    assertThat(parameters.size(), is(2));
    final DocumentedParameter parameter = parameters.get(0);
    assertThat(parameter.getName(), is("name"));
    assertThat(parameter.getType(), is(stringClass));
    assertThat(parameter.isNullable(), is(nullValue()));

    final DocumentedParameter parameter2 = parameters.get(1);
    assertThat(parameter2.getName(), is("type"));
    assertThat(parameter2.getType(), is(classClass));
    assertThat(parameter2.isNullable(), is(nullValue()));

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags.size(), is(2));
    final ParamTag paramTag = paramTags.get(0);
    assertThat(paramTag.getParameter(), is(equalTo(parameter)));
    assertThat(paramTag.getComment().getText(), is("a String"));

    final ParamTag paramTag2 = paramTags.get(1);
    assertThat(paramTag2.getParameter(), is(equalTo(parameter2)));
    assertThat(paramTag2.getComment().getText(), is("a Class"));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag, is(nullValue()));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags, is(empty()));
  }

  @Test
  public void methodFromArrayToCollection() throws ClassNotFoundException {
    DocumentedExecutable member = members.get(5);
    assertThat(member.isConstructor(), is(false));

    final List<DocumentedParameter> parameters = member.getParameters();
    assertThat(parameters.size(), is(2));
    final DocumentedParameter parameter = parameters.get(0);
    assertThat(parameter.getName(), is("a"));
    assertThat(parameter.getType(), is(Object[].class));
    assertThat(parameter.isNullable(), is(false));

    final DocumentedParameter parameter2 = parameters.get(1);
    assertThat(parameter2.getName(), is("c"));
    assertThat(parameter2.getType(), is(collectionClass));
    assertThat(parameter2.isNullable(), is(false));

    final List<ParamTag> paramTags = member.paramTags();
    assertThat(paramTags.size(), is(2));
    final ParamTag paramTag = paramTags.get(0);
    assertThat(paramTag.getParameter(), is(equalTo(parameter)));
    assertThat(paramTag.getComment().getText(), is("an array"));

    final ParamTag paramTag2 = paramTags.get(1);
    assertThat(paramTag2.getParameter(), is(equalTo(parameter2)));
    assertThat(paramTag2.getComment().getText(), is("a Collection"));

    final ReturnTag returnTag = member.returnTag();
    assertThat(returnTag, is(nullValue()));

    final List<ThrowsTag> throwsTags = member.throwsTags();
    assertThat(throwsTags, is(empty()));
    assertThat(member.getReturnType().getType().getTypeName(), is("void"));
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
