package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.util.GsonInstance;

public class JavadocExtractorTest {

  private final String testResources = "src/test/resources";
  private final Type doubleType = new Type("double");
  private final Type objectType = new Type("java.lang.Object");
  private final Type objectArrayType = new Type("java.lang.Object[]");
  private final Type npe = new Type("java.lang.NullPointerException");
  private final Type iae = new Type("java.lang.IllegalArgumentException");
  private final Logger log = LoggerFactory.getLogger(JavadocExtractorTest.class);

  /**
   * Tests {@code JavadocExtractor} on the example class example.AClass in
   * src/test/resources/example
   */
  @Test
  public void exampleAClassTest() {
    List<Parameter> params = new ArrayList<>();
    List<ThrowsTag> throwsTags = new ArrayList<>();
    List<ParamTag> paramTags = new ArrayList<>();
    List<DocumentedMethod> expected = new ArrayList<>();
    Type aClass = new Type("example.AClass");

    // Method: AClass().
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    throwsTags.add(new ThrowsTag(npe, "always"));
    expected.add(new DocumentedMethod(aClass, "AClass", null, null, null, false, throwsTags, null));

    // Method: AClass(String).
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(new Type("java.lang.String"), "x"));
    throwsTags.add(new ThrowsTag(npe, "if x is null"));
    throwsTags.add(new ThrowsTag(new Type("example.exception.AnException"), "if x is empty"));
    paramTags.add(
        new ParamTag(
            new Parameter(new Type("java.lang.String"), "x"), "must not be null nor empty"));
    expected.add(
        new DocumentedMethod(aClass, "AClass", null, params, paramTags, false, throwsTags, null));

    // Method: foo(int[])
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    Parameter par1 = new Parameter(new Type("int[]"), "array", true);
    params.add(par1);
    paramTags.add(new ParamTag(par1, "must not be null"));
    throwsTags.add(new ThrowsTag(npe, "if array is null"));
    expected.add(
        new DocumentedMethod(
            aClass, "foo", doubleType, params, paramTags, false, throwsTags, null));

    // Method: bar(Object, Object).
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(objectType, "x", false));
    params.add(new Parameter(objectType, "y", false));
    throwsTags.add(new ThrowsTag(iae, "if x is null"));
    paramTags.add(new ParamTag(new Parameter(objectType, "x", false), "must not be null"));
    expected.add(
        new DocumentedMethod(
            aClass, "bar", doubleType, params, paramTags, false, throwsTags, null));

    // Method: baz(Object).
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(objectType, "x"));
    throwsTags.add(new ThrowsTag(iae, "if x is null"));
    paramTags.add(new ParamTag(new Parameter(objectType, "x"), "must not be null"));
    expected.add(
        new DocumentedMethod(
            aClass, "baz", doubleType, params, paramTags, false, throwsTags, null));

    // Method: testParam(double, double)
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(doubleType, "x"));
    params.add(new Parameter(doubleType, "y"));
    paramTags.add(
        new ParamTag(new Parameter(doubleType, "x"), "the first number, must be positive"));
    paramTags.add(
        new ParamTag(new Parameter(doubleType, "y"), "the second number, must be " + "positive"));
    expected.add(
        new DocumentedMethod(
            aClass, "testParam", doubleType, params, paramTags, false, throwsTags, null));

    // Method: testParam2(double, double)
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(doubleType, "x"));
    params.add(new Parameter(doubleType, "y"));
    paramTags.add(
        new ParamTag(new Parameter(doubleType, "x"), "the first number, must be positive"));
    paramTags.add(
        new ParamTag(new Parameter(doubleType, "y"), "the second number, must be " + "positive"));
    expected.add(
        new DocumentedMethod(
            aClass, "testParam2", doubleType, params, paramTags, false, throwsTags, null));

    // Method: testParam3(double)
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(doubleType, "x"));
    paramTags.add(new ParamTag(new Parameter(doubleType, "x"), "must be positive"));
    expected.add(
        new DocumentedMethod(
            aClass, "testParam3", doubleType, params, paramTags, false, throwsTags, null));

    test(
        "example.AClass",
        expected,
        testResources + "/example.AClass_extractor_output.txt",
        testResources);
  }

  /**
   * Tests {@code JavadocExtractor} on the example class example.AChild in
   * src/test/resources/example
   */
  @Test
  public void exampleAChildTest() {
    List<Parameter> params = new ArrayList<>();
    List<ThrowsTag> throwsTags = new ArrayList<>();
    List<ParamTag> paramTags = new ArrayList<>();
    List<DocumentedMethod> expected = new ArrayList<>();
    Type aClass = new Type("example.AClass");
    Type aChild = new Type("example.AChild");

    // Method: baz(Object)
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(objectType, "z"));
    throwsTags.add(new ThrowsTag(iae, "if z is null"));
    paramTags.add(new ParamTag(new Parameter(objectType, "z"), "must not be null"));
    expected.add(
        new DocumentedMethod(
            aChild, "baz", doubleType, params, paramTags, false, throwsTags, null));

    // Method: vararg(Object...)
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(objectArrayType, "x"));
    throwsTags.add(new ThrowsTag(iae, "if x is null"));
    paramTags.add(new ParamTag(new Parameter(objectArrayType, "x"), "must not be null"));
    expected.add(
        new DocumentedMethod(
            aChild, "vararg", doubleType, params, paramTags, true, throwsTags, null));

    // Method: testParam(double, double)
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(doubleType, "x"));
    params.add(new Parameter(doubleType, "y"));
    paramTags.add(
        new ParamTag(new Parameter(doubleType, "x"), "the first number, must be positive"));
    paramTags.add(
        new ParamTag(new Parameter(doubleType, "y"), "the second number, must be " + "positive"));
    expected.add(
        new DocumentedMethod(
            aChild, "testParam", doubleType, params, paramTags, false, throwsTags, null));

    // Method: testParam2(double, double)
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(doubleType, "x"));
    params.add(new Parameter(doubleType, "y"));
    paramTags.add(
        new ParamTag(new Parameter(doubleType, "x"), "the first number, must be positive"));
    paramTags.add(
        new ParamTag(new Parameter(doubleType, "y"), "the second number, must be " + "positive"));
    expected.add(
        new DocumentedMethod(
            aChild, "testParam2", doubleType, params, paramTags, false, throwsTags, null));

    // Method: AClass.foo(int[])
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    Parameter par1 = new Parameter(new Type("int[]"), "array", true);
    params.add(par1);
    throwsTags.add(new ThrowsTag(npe, "if array is null"));
    paramTags.add(new ParamTag(par1, "must not be null"));
    expected.add(
        new DocumentedMethod(
            aClass, "foo", doubleType, params, paramTags, false, throwsTags, null));

    // Method: AClass.bar(Object, Object).
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(objectType, "x", false));
    params.add(new Parameter(objectType, "y", false));
    throwsTags.add(new ThrowsTag(iae, "if x is null"));
    paramTags.add(new ParamTag(new Parameter(objectType, "x", false), "must not be null"));
    expected.add(
        new DocumentedMethod(
            aClass, "bar", doubleType, params, paramTags, false, throwsTags, null));

    // Method: AClass.testParam3(double)
    params.clear();
    paramTags.clear();
    throwsTags.clear();
    params.add(new Parameter(doubleType, "x"));
    paramTags.add(new ParamTag(new Parameter(doubleType, "x"), "must be positive"));
    expected.add(
        new DocumentedMethod(
            aClass, "testParam3", doubleType, params, paramTags, false, throwsTags, null));

    test(
        "example.AChild",
        expected,
        testResources + "/example.AChild_extractor_output.txt",
        testResources);
  }

  @Test
  public void paramInheritanceInAbstractClassTest() {
    List<Parameter> params = new ArrayList<>();
    List<ThrowsTag> throwsTags = new ArrayList<>();
    List<ParamTag> paramTags = new ArrayList<>();
    List<DocumentedMethod> expected = new ArrayList<>();
    Type abstractClass = new Type("example.AbstractClass");

    Parameter par1 = new Parameter(new Type("V"), "sourceVertex");
    Parameter par2 = new Parameter(new Type("V"), "targetVertex");

    params.add(par1);
    params.add(par2);

    paramTags.add(new ParamTag(par1, "source vertex of the edge."));
    paramTags.add(new ParamTag(par2, "target vertex of the edge."));

    expected.add(
        new DocumentedMethod(
            abstractClass,
            "containsEdge",
            new Type("boolean"),
            params,
            paramTags,
            false,
            throwsTags,
            null));

    test(
        "example.AbstractClass",
        expected,
        testResources + "/example.AbstractClass_extractor_output.txt",
        testResources);
  }

  private void test(
      String targetClass, List<DocumentedMethod> expected, String actualOutput, String sourcePath) {
    Toradocu.main(
        new String[] {
          "--target-class",
          targetClass,
          "--javadoc-extractor-output",
          actualOutput,
          "--condition-translation",
          "false",
          "--oracle-generation",
          "false",
          "--source-dir",
          sourcePath,
          "--class-dir",
          ""
        });

    java.lang.reflect.Type listType = new TypeToken<List<DocumentedMethod>>() {}.getType();
    Gson gson = GsonInstance.gson();
    Path ouputFilePath = Paths.get(actualOutput);
    try (BufferedReader reader = Files.newBufferedReader(ouputFilePath)) {
      List<DocumentedMethod> actual = gson.fromJson(reader, listType);
      assertThat(actual.size(), is(equalTo(expected.size())));

      for (int i = 0; i < actual.size(); i++) {
        DocumentedMethod actualValue = actual.get(i);
        DocumentedMethod expectedValue = expected.get(i);
        assertThat(actualValue, is(equalTo(expectedValue)));
      }
    } catch (IOException e) {
      fail(e.getMessage());
    }
    try {
      Files.delete(ouputFilePath);
    } catch (IOException e) {
      log.error("Error deleting the file: " + ouputFilePath);
    }
  }
}
