package org.toradocu.extractor;

//import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.toradocu.extractor.DocumentedMethod.Builder;

import com.google.gson.Gson;

public class DocumentedMethodTest {
	
	private final Type voidType = new Type("void");
	private final Type arrayType = new Type("java.lang.String[]");
	private final Type npe = new Type("java.lang.NullPointerException");
	private final Type iae = new Type("java.lang.IllegalArgumentException");
	private final Type containingClass = new Type("example.Foo");
	
	@Test
	public void testBasics() {
		DocumentedMethod method = new DocumentedMethod.Builder(containingClass, "bar", voidType).build();
		assertThat(method.getSignature(), is("bar()"));
		assertThat(method.getName(), is("bar"));
		assertThat(method.getContainingClass(), is(containingClass));
		assertThat(method.getReturnType(), is(voidType));
		assertThat(method.isConstructor(), is(false));
		assertThat(method.getParameters(), is(emptyCollectionOf(Parameter.class)));
		assertThat(method.isVarArgs(), is(false));
		
		method = new DocumentedMethod.Builder(containingClass, "Foo", null).build();
		assertThat(method.getSignature(), is("Foo()"));
		assertThat(method.getContainingClass(), is(containingClass));
		assertThat(method.getReturnType(), is(nullValue()));
		assertThat(method.isConstructor(), is(true));
		assertThat(method.getParameters(), is(emptyCollectionOf(Parameter.class)));
		assertThat(method.isVarArgs(), is(false));
		
		method = new DocumentedMethod.Builder(containingClass, "bat", voidType, true, new Parameter(new Type("int"), "elements", 0)).build();
		assertThat(method.isVarArgs(), is(true));
	}
	
	@Test
	public void testIllegalMethodName() {
		try {
			new DocumentedMethod.Builder(new Type("Foo"), "Foo.bar", voidType);
			fail("IllegalArgumentException expected but not thrown.");
		} catch (IllegalArgumentException e) {}
	}
	
	@Test
	public void testMultipleTags() {
		Builder methodBuilder = new DocumentedMethod.Builder(containingClass, "compute", voidType, new Parameter(arrayType, "array", 0));
		methodBuilder.tag(new ThrowsTag(npe, "if the array is empty"));
		methodBuilder.tag(new ThrowsTag(npe, "if the array is empty"));
		DocumentedMethod method = methodBuilder.build();
		
		List<ThrowsTag> tags = method.throwsTags();
		assertThat(tags.size(), is(1));
		assertThat(tags.get(0), is(new ThrowsTag(npe, "if the array is empty")));
		
		methodBuilder = new DocumentedMethod.Builder(containingClass, "compute", voidType, new Parameter(arrayType, "array", 0));
        methodBuilder.tag(new ThrowsTag(npe, "if the array is null"));
        methodBuilder.tag(new ThrowsTag(iae, "if the array is empty"));
        method = methodBuilder.build();
        
        tags = method.throwsTags();
        assertThat(tags.size(), is(2));
        assertThat(tags.get(0), is(new ThrowsTag(npe, "if the array is null")));
        assertThat(tags.get(1), is(new ThrowsTag(iae, "if the array is empty")));
	}
	
	@Test
	public void testToString() {
	    DocumentedMethod method = new DocumentedMethod.Builder(containingClass, "compute", voidType).build();
        assertThat(method.toString(), is("void example.Foo.compute()"));
	  
		method = new DocumentedMethod.Builder(containingClass, "compute", voidType, new Parameter(arrayType, "array", 0)).build();
		assertThat(method.toString(), is("void example.Foo.compute(java.lang.String[] array)"));
		
	    method = new DocumentedMethod.Builder(containingClass, "compute", voidType, new Parameter(new Type("int"), "x", 0), new Parameter(new Type("int"), "y", 1)).build();
        assertThat(method.toString(), is("void example.Foo.compute(int x,int y)"));
        
        method = new DocumentedMethod.Builder(containingClass, "Foo", null).build();
        assertThat(method.toString(), is("example.Foo.Foo()"));
	}
	
	@Test
	public void testEquals() {
		Builder methodBuilder = new DocumentedMethod.Builder(containingClass, "compute", voidType, new Parameter(arrayType, "array", 0));
		methodBuilder.tag(new ThrowsTag(npe, "if the array is empty"));
		DocumentedMethod method1 = methodBuilder.build();
		
		assertThat(method1.equals(method1), is(true));
		assertThat(method1.equals(new Object()), is(false));

		methodBuilder = new DocumentedMethod.Builder(containingClass, "compute", voidType, new Parameter(arrayType, "array", 0));
		methodBuilder.tag(new ThrowsTag(npe, "if the array is empty"));
		DocumentedMethod method2 = methodBuilder.build();
		
		assertThat(method1.equals(method2), is(true));
		assertThat(method1.hashCode(), is(equalTo(method2.hashCode())));
		
		methodBuilder = new DocumentedMethod.Builder(containingClass, "foo", voidType, new Parameter(arrayType, "array", 0));
		methodBuilder.tag(new ThrowsTag(npe, "if the array is empty"));
		DocumentedMethod method3 = methodBuilder.build();

		assertThat(method1.equals(method3), is(false));
		assertThat(method1.hashCode(), is(not(equalTo(method3.hashCode()))));
	}

	@Test
	public void testJSon() {
		Builder methodBuilder = new DocumentedMethod.Builder(containingClass, "compute", voidType, new Parameter(arrayType, "array", 0));
		methodBuilder.tag(new ThrowsTag(npe, "if the array is empty"));
		DocumentedMethod method1 = methodBuilder.build();

		String json = new Gson().toJson(method1);
		DocumentedMethod method2 = new Gson().fromJson(json, DocumentedMethod.class);
		assertThat(method1, is(equalTo(method2)));
	}

}
