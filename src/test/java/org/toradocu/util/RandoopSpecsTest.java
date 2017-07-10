package org.toradocu.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.ParamTag;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ReturnTag;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.extractor.Type;
import randoop.condition.specification.Guard;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.Property;
import randoop.condition.specification.ThrowsSpecification;

public class RandoopSpecsTest {

  @Test
  public void paramSpecTest() throws Exception {
    DocumentedExecutable m = createMethod();
    ParamTag paramTag = new ArrayList<>(m.paramTags()).get(0);
    PreSpecification spec = RandoopSpecs.translate(paramTag, m);
    assertThat(spec.getDescription(), is("must not be null"));
    Guard guard = spec.getGuard();
    assertThat(guard.getConditionText(), is("(x==null)==false"));
    assertThat(guard.getDescription(), is("must not be null"));
  }

  @Test
  public void returnSpecsTest() throws Exception {
    DocumentedExecutable m = createMethod();
    ReturnTag tag = m.returnTag();
    List<PostSpecification> specs = RandoopSpecs.translate(tag, m);
    assertThat(specs.size(), is(2));

    PostSpecification spec1 = specs.get(0);
    assertThat(spec1.getDescription(), is("return true iff x is positive"));
    Guard guard1 = spec1.getGuard();
    assertThat(guard1.getDescription(), is(""));
    assertThat(guard1.getConditionText(), is("x>0"));
    Property prop1 = spec1.getProperty();
    assertThat(prop1.getDescription(), is("true iff x is positive"));
    assertThat(prop1.getConditionText(), is("result==true"));

    PostSpecification spec2 = specs.get(1);
    assertThat(spec2.getDescription(), is("return true iff x is positive"));
    Guard guard2 = spec2.getGuard();
    assertEquals(guard1, guard2);
    assertThat(guard2.getDescription(), is(""));
    assertThat(guard2.getConditionText(), is("x>0"));
    Property prop2 = spec2.getProperty();
    assertThat(prop2.getDescription(), is("true iff x is positive"));
    assertThat(prop2.getConditionText(), is("result==false"));
  }

  @Test
  public void throwsSpecTest() throws Exception {
    DocumentedExecutable m = createMethod();

    ThrowsTag tag =
        new ThrowsTag(
            new Type("java.lang.IllegalStateException"), "if the connection is already open");
    tag.setCondition("receiver.isOpen()");

    ThrowsSpecification spec = RandoopSpecs.translate(tag, m);
    assertThat(spec.getExceptionTypeName(), is("java.lang.IllegalStateException"));
    assertThat(
        spec.getDescription(),
        is("throws IllegalStateException if the connection is already open"));
    Guard guard = spec.getGuard();
    assertThat(guard.getConditionText(), is("receiver.isOpen()"));
    assertThat(guard.getDescription(), is("if the connection is already open"));
  }

  private static DocumentedExecutable createMethod() {
    Type contClass = new Type("org.aClass");
    Type bool = new Type("java.lang.Boolean");

    Parameter par = new Parameter(new Type("int"), "x");
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(par);

    ParamTag tag = new ParamTag(par, "must not be null");
    tag.setCondition("(x==null)==false");
    List<ParamTag> paramTags = new ArrayList<>();
    paramTags.add(tag);

    ThrowsTag throwsTag =
        new ThrowsTag(
            new Type("java.lang.IllegalStateException"), "if the connection is already open");
    throwsTag.setCondition("receiver.isOpen()");
    List<ThrowsTag> throwsTags = new ArrayList<>();
    throwsTags.add(throwsTag);

    ReturnTag returnTag = new ReturnTag("true iff x is positive");
    returnTag.setCondition("x>0 ? result==true : result==false");

    return new DocumentedExecutable(
        contClass, "method", bool, parameters, paramTags, false, throwsTags, returnTag);
  }
}
