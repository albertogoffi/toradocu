package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.toradocu.util.Reflection;

public class ParameterTest {

  private final Class<?> string;

  public ParameterTest() throws ClassNotFoundException {
    string = Reflection.getClass("java.lang.String");
  }

  @Test
  public void testBasics() {
    Parameter p = new Parameter(string, "par");
    assertThat(p.getType(), is(string));
    assertThat(p.getName(), is("par"));
    assertThat(p.isNullable(), is(nullValue()));

    Parameter intPar = new Parameter(string, "par", false);
    assertThat(intPar.isNullable(), is(false));

    intPar = new Parameter(string, "par", true);
    assertThat(intPar.isNullable(), is(true));
  }

  @Test
  public void testToString() {
    String name = "par";
    Parameter p = new Parameter(string, name);
    assertThat(p.toString(), is(string.getName() + " " + name));
  }

  @Test
  public void testEquals() {
    Parameter p1 = new Parameter(string, "par");
    assertThat(p1.equals(p1), is(true));
    assertThat(p1.hashCode(), is(p1.hashCode()));

    Parameter p1Copy = new Parameter(string, "par");
    assertThat(p1.equals(p1Copy), is(true));
    assertThat(p1.hashCode(), is(equalTo(p1Copy.hashCode())));

    Parameter differentPar = new Parameter(string, "foo");
    assertThat(p1.equals(differentPar), is(false));

    Object anObject = new Object();
    assertThat(p1.equals(anObject), is(false));
    assertThat(p1.hashCode(), is(not(anObject.hashCode())));
  }
}
