package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class DocumentedMethodTest {

  private final Type arrayType = new Type("java.lang.String[]");
  private final Type npe = new Type("java.lang.NullPointerException");
  private final Type iae = new Type("java.lang.IllegalArgumentException");
  private final Type containingClass = new Type("example.Foo");

  @Test
  public void testBasics() {
    DocumentedMethod method =
        new DocumentedMethod(containingClass, "bar", Type.VOID, null, false, null);
    assertThat(method.getSignature(), is("bar()"));
    assertThat(method.getName(), is("bar"));
    assertThat(method.getContainingClass(), is(containingClass));
    assertThat(method.getReturnType(), is(Type.VOID));
    assertThat(method.isConstructor(), is(false));
    assertThat(method.getParameters(), is(emptyCollectionOf(Parameter.class)));
    assertThat(method.isVarArgs(), is(false));

    method = new DocumentedMethod(containingClass, "Foo", null, null, false, null);
    assertThat(method.getSignature(), is("Foo()"));
    assertThat(method.getContainingClass(), is(containingClass));
    assertThat(method.getReturnType(), is(nullValue()));
    assertThat(method.isConstructor(), is(true));
    assertThat(method.getParameters(), is(emptyCollectionOf(Parameter.class)));
    assertThat(method.isVarArgs(), is(false));

    List<Parameter> params = new ArrayList<>();
    params.add(new Parameter(new Type("int"), "elements"));
    method = new DocumentedMethod(containingClass, "bat", Type.VOID, params, true, null);
    assertThat(method.isVarArgs(), is(true));
  }

  @Test
  public void testIllegalMethodName() {
    try {
      new DocumentedMethod(new Type("Foo"), "Foo.bar", Type.VOID, null, false, null);
      fail("IllegalArgumentException expected but not thrown.");
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testMultipleTags() {
    List<Parameter> params = new ArrayList<>();
    params.add(new Parameter(new Type("int"), "elements"));
    List<ThrowsTag> tags = new ArrayList<>();
    tags.add(new ThrowsTag(npe, "if the array is empty", null));
    tags.add(new ThrowsTag(npe, "if the array is empty", null));

    DocumentedMethod method =
        new DocumentedMethod(containingClass, "compute", Type.VOID, params, false, tags);
    assertThat(method.throwsTags().size(), is(1));
    assertThat(
        method.throwsTags().iterator().next(),
        is(new ThrowsTag(npe, "if the array is empty", null)));

    tags.clear();
    tags.add(new ThrowsTag(npe, "if the array is null", null));
    tags.add(new ThrowsTag(iae, "if the array is empty", null));
    method = new DocumentedMethod(containingClass, "compute", Type.VOID, params, false, tags);
    assertThat(method.throwsTags().size(), is(2));
    Iterator<ThrowsTag> iterator = method.throwsTags().iterator();
    assertThat(iterator.next(), is(new ThrowsTag(npe, "if the array is null", null)));
    assertThat(iterator.next(), is(new ThrowsTag(iae, "if the array is empty", null)));
  }

  @Test
  public void testToString() {
    DocumentedMethod method =
        new DocumentedMethod(containingClass, "compute", Type.VOID, null, false, null);
    assertThat(method.toString(), is("void example.Foo.compute()"));

    List<Parameter> params = new ArrayList<>();
    params.add(new Parameter(arrayType, "array"));
    method = new DocumentedMethod(containingClass, "compute", Type.VOID, params, false, null);
    assertThat(method.toString(), is("void example.Foo.compute(java.lang.String[] array)"));

    params.clear();
    params.add(new Parameter(new Type("int"), "x"));
    params.add(new Parameter(new Type("int"), "y"));
    method = new DocumentedMethod(containingClass, "compute", Type.VOID, params, false, null);
    assertThat(method.toString(), is("void example.Foo.compute(int x,int y)"));

    method = new DocumentedMethod(containingClass, "Foo", null, null, false, null);
    assertThat(method.toString(), is("example.Foo.Foo()"));
  }

  @Test
  public void testEquals() {
    List<Parameter> params = new ArrayList<>();
    List<ThrowsTag> tags = new ArrayList<>();

    params.add(new Parameter(arrayType, "array"));
    tags.add(new ThrowsTag(npe, "if the array is empty", null));
    DocumentedMethod method1 =
        new DocumentedMethod(containingClass, "compute", Type.VOID, params, false, tags);

    assertThat(method1.equals(method1), is(true));
    assertThat(method1.equals(new Object()), is(false));

    DocumentedMethod method2 =
        new DocumentedMethod(containingClass, "compute", Type.VOID, params, false, tags);
    assertThat(method1.equals(method2), is(true));
    assertThat(method1.hashCode(), is(equalTo(method2.hashCode())));

    DocumentedMethod method3 =
        new DocumentedMethod(containingClass, "foo", Type.VOID, params, false, tags);
    assertThat(method1.equals(method3), is(false));
    assertThat(method1.hashCode(), is(not(equalTo(method3.hashCode()))));
  }

  @Test
  public void testJSon() {
    List<Parameter> params = new ArrayList<>();
    params.add(new Parameter(new Type("int"), "elements"));
    List<ThrowsTag> tags = new ArrayList<>();
    tags.add(new ThrowsTag(npe, "if the array is empty", null));

    DocumentedMethod method1 =
        new DocumentedMethod(containingClass, "compute", Type.VOID, params, false, tags);

    String json = new Gson().toJson(method1);
    DocumentedMethod method2 = new Gson().fromJson(json, DocumentedMethod.class);
    assertThat(method1, is(equalTo(method2)));
  }
}
