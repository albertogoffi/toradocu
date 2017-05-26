package org.toradocu.extractor;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ThrowsTagTest {

  private final Class<?> NPE;

  public ThrowsTagTest() throws ClassNotFoundException {
    NPE = loadException("java.lang.NullPointerException");
  }

  @Test
  public void testBasics() {
    ThrowsTag tag = new ThrowsTag(NPE, new Comment("if x is null"));
    assertThat(tag.getComment(), is("if x is null"));
    assertThat(tag.getException(), is(NPE));
    assertThat(tag.getCondition(), is(emptyString()));

    tag.setCondition("(x==null)||(y==null)");
    assertThat(tag.getCondition(), is("(x==null)||(y==null)"));

    assertThat(
        tag.toString(),
        is(
            tag.getKind()
                + " "
                + NPE.getName()
                + " "
                + tag.getComment()
                + " ==> "
                + tag.getCondition()));
  }

  @Test
  public void testEquals() throws ClassNotFoundException {
    ThrowsTag tag1 = new ThrowsTag(NPE, new Comment("if x is null"));
    ThrowsTag tag2 = new ThrowsTag(NPE, new Comment("if x is null"));
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));
    assertThat(tag1.equals(new Object()), is(false));

    tag1.setCondition("x == null");
    tag2.setCondition("x == null");
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));

    tag2.setCondition("x == null || y == null");
    assertThat(tag1.equals(tag2), is(false));

    ThrowsTag tag3 = new ThrowsTag(NPE, new Comment("if y is null"));
    assertThat(tag1.equals(tag3), is(false));

    final Class<?> IAE = loadException("java.lang.IllegalArgumentException");
    ThrowsTag tag4 = new ThrowsTag(IAE, new Comment("if x is null"));
    assertThat(tag1.equals(tag4), is(false));
  }

  private Class<?> loadException(String exception) throws ClassNotFoundException {
    return Class.forName(exception);
  }
}
