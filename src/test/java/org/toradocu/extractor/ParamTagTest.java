package org.toradocu.extractor;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import org.toradocu.util.Reflection;

public class ParamTagTest {

  private final Class<?> intClass;

  public ParamTagTest() throws ClassNotFoundException {
    intClass = Reflection.getClass("int");
  }

  @Test
  public void testBasics() {
    ParamTag tag =
        new ParamTag(new Parameter(intClass, "elements"), new Comment("must not be null"));
    assertThat(tag.getParameter(), is(new Parameter(intClass, "elements")));
    assertThat(tag.getComment(), is("must not be null"));
    assertThat(tag.getCondition(), is(emptyString()));

    tag.setCondition("elements != null");
    assertThat(tag.getCondition(), is("elements != null"));
  }

  @Test
  public void testToString() {
    ParamTag tag =
        new ParamTag(new Parameter(intClass, "elements"), new Comment("must not be null"));
    assertThat(tag.toString(), is("@param elements must not be null"));

    tag.setCondition("elements != null");
    assertThat(tag.toString(), is("@param elements must not be null ==> elements != null"));
  }

  @Test
  public void testEquals() {
    ParamTag tag1 =
        new ParamTag(new Parameter(intClass, "elements"), new Comment("must not be null"));
    ParamTag tag2 =
        new ParamTag(new Parameter(intClass, "elements"), new Comment("must not be null"));
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));
    assertThat(tag1.equals(new Object()), is(false));

    tag1.setCondition("elements != null");
    tag2.setCondition("elements != null");
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));

    tag2.setCondition("elements == null");
    assertThat(tag1.equals(tag2), is(false));

    ParamTag tag3 =
        new ParamTag(new Parameter(intClass, "elements"), new Comment("must not be false"));
    assertThat(tag1.equals(tag3), is(false));

    ParamTag tag4 =
        new ParamTag(new Parameter(intClass, "element"), new Comment("must not be null"));
    assertThat(tag1.equals(tag4), is(false));
  }

  @Test
  public void testHashCode() {
    ParamTag tag1 =
        new ParamTag(new Parameter(intClass, "elements"), new Comment("must not be null"));
    ParamTag tag2 =
        new ParamTag(new Parameter(intClass, "elements"), new Comment("must not be null"));
    Set<ParamTag> set1 = new LinkedHashSet<>();
    Set<ParamTag> set2 = new LinkedHashSet<>();
    set1.add(tag1);
    set2.add(tag2);

    assertThat(set1, is(equalTo(set2)));
  }
}
