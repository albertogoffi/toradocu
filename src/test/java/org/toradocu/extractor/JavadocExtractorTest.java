package org.toradocu.extractor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.FileNotFoundException;
import java.util.List;
import org.junit.Test;

public class JavadocExtractorTest {

  private static final String EXAMPLE_SRC = "src/test/resources";

  /**
   * Tests {@code JavadocExtractor} on the example class example.AClass in
   * src/test/resources/example
   */
  @Test
  public void exampleAClassTest() throws ClassNotFoundException, FileNotFoundException {
    final JavadocExtractor javadocExtractor = new JavadocExtractor();
    final List<ExecutableMember> members = javadocExtractor.extract("example.AClass", EXAMPLE_SRC);

    assertThat(members.size(), is(8));

    // Constructor AClass().
    checkConstructor1(members.get(0));

    // Constructor AClass(String).
    checkConstructor2(members.get(1));

    // TODO Add more assertions (about other methods in the example test class).
  }

  // Constructor AClass().
  private void checkConstructor1(ExecutableMember member) throws ClassNotFoundException {
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

  // Constructor AClass(String).
  private void checkConstructor2(ExecutableMember member) throws ClassNotFoundException {
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
}
