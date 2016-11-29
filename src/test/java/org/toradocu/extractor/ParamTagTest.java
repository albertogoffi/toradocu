package org.toradocu.extractor;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ParamTagTest {

  @Test
  public void testBasics() {
    ParamTag tag = new ParamTag(new Parameter(new Type("int"), "elements"), "must not be null");
    assertThat(tag.parameter(), is(new Parameter(new Type("int"), "elements")));
    assertThat(tag.parameterComment(), is("must not be null"));
    assertThat(tag.getCondition().isPresent(), is(false));

    tag.setCondition("");
    assertThat(tag.getCondition().isPresent(), is(true));
    assertThat(tag.getCondition().get(), is(emptyString()));

    tag.setCondition("elements != null");
    assertThat(tag.getCondition().isPresent(), is(true));
    assertThat(tag.getCondition().get(), is("elements != null"));
  }

  @Test
  public void testToString() {
    ParamTag tag = new ParamTag(new Parameter(new Type("int"), "elements"), "must not be null");
    assertThat(tag.toString(), is("@param elements" + " " + "must not be null"));

    tag.setCondition("elements != null");
    assertThat(
        tag.toString(),
        is("@param elements" + " " + "must not be null" + " ==> " + "elements != null"));
  }

  @Test
  public void testEquals() {
    ParamTag tag1 = new ParamTag(new Parameter(new Type("int"), "elements"), "must not be null");
    ParamTag tag2 = new ParamTag(new Parameter(new Type("int"), "elements"), "must not be null");
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));
    assertThat(tag1.equals(new Object()), is(false));

    tag1.setCondition("elements != null");
    tag2.setCondition("elements != null");
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));

    tag2.setCondition("elements == null");
    assertThat(tag1.equals(tag2), is(false));

    ParamTag tag3 = new ParamTag(new Parameter(new Type("int"), "elements"), "must not be false");

    assertThat(tag1.equals(tag3), is(false));

    ParamTag tag4 = new ParamTag(new Parameter(new Type("int"), "element"), "must not be null");

    assertThat(tag1.equals(tag4), is(false));
  }
}
