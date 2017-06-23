package org.toradocu.extractor;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.toradocu.translator.spec.ExcPostcondition;
import org.toradocu.translator.spec.Guard;

public class ThrowsTagTest {

  private final Class<?> NPE;

  public ThrowsTagTest() throws ClassNotFoundException {
    NPE = loadException("java.lang.NullPointerException");
  }

  @Test
  public void testBasics() {
    ThrowsTag tag = new ThrowsTag(NPE, new Comment("if x is null"));
    assertThat(tag.getComment().getText(), is("if x is null"));
    assertThat(tag.getException(), is(NPE));
    assertThat(tag.getSpecification(), is(nullValue()));

    Guard guard = new Guard("(x==null)||(y==null)");
    ExcPostcondition excPostcondition = new ExcPostcondition(guard, tag.getException().getName());
    tag.setSpecification(excPostcondition);
    assertThat(tag.getSpecification(), is(excPostcondition));

    assertThat(
        tag.toString(),
        is(
            tag.getKind()
                + " "
                + NPE.getName()
                + " "
                + tag.getComment().getText()
                + " ==> "
                + tag.getSpecification().toString()));
  }

  @Test
  public void testEquals() throws ClassNotFoundException {
    ThrowsTag tag1 = new ThrowsTag(NPE, new Comment("if x is null"));
    ThrowsTag tag2 = new ThrowsTag(NPE, new Comment("if x is null"));
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));
    assertThat(tag1.equals(new Object()), is(false));

    Guard guard1 = new Guard("(x==null)");
    ExcPostcondition excPostcondition1 = new ExcPostcondition(guard1, NPE.getName());
    tag1.setSpecification(excPostcondition1);
    tag2.setSpecification(excPostcondition1);
    assertThat(tag1.equals(tag2), is(true));
    assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));

    Guard guard2 = new Guard("(x==null || y==null)");
    ExcPostcondition excPostcondition2 = new ExcPostcondition(guard2, NPE.getName());
    tag2.setSpecification(excPostcondition2);
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
