package org.toradocu.extractor;

//import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.toradocu.extractor.DocumentedMethod.Builder;

import com.google.gson.Gson;

public class DocumentedMethodTest {
	
	@Test
	public void testBasics() {
		DocumentedMethod method = new DocumentedMethod.Builder("void", "Foo.bar").build();
		assertThat(method.getSimpleName(), is("bar"));
		assertThat(method.getSignature(), is("Foo.bar()"));
		assertThat(method.getContainingClass(), is("Foo"));
		assertThat(method.getReturnType(), is("void"));
		assertThat(method.isAConstructor(), is(false));
		assertThat(method.getParameters(), is(emptyCollectionOf(Parameter.class)));
		
		method = new DocumentedMethod.Builder("", "example.Foo.Foo").build();
		assertThat(method.getSimpleName(), is("Foo"));
		assertThat(method.getSignature(), is("example.Foo.Foo()"));
		assertThat(method.getContainingClass(), is("example.Foo"));
		assertThat(method.getReturnType(), is(""));
		assertThat(method.isAConstructor(), is(true));
		assertThat(method.getParameters(), is(emptyCollectionOf(Parameter.class)));
	}
	
	@Test
	public void testIllegalMethodName() {
		try {
			new DocumentedMethod.Builder("void", "foo");
			fail("IllegalArgumentException expected but not thrown.");
		} catch (IllegalArgumentException e) {}
		
		try {
			new DocumentedMethod.Builder("void", ".Foo.bar");
			fail("IllegalArgumentException expected but not thrown.");
		} catch (IllegalArgumentException e) {}
		
		try {
			new DocumentedMethod.Builder("void", "Foo.bar.");
			fail("IllegalArgumentException expected but not thrown.");
		} catch (IllegalArgumentException e) {}
	}
	
	@Test
	public void testMultipleTags() {
		Builder methodBuilder = new DocumentedMethod.Builder("void", "Foo.compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method = methodBuilder.build();
		
		List<ThrowsTag> tags = method.throwsTags();
		assertThat(tags.size(), is(1));
		assertThat(tags.get(0), is(new ThrowsTag("java.lang.NullPointerException", "if the array is empty")));
		
		methodBuilder = new DocumentedMethod.Builder("void", "Foo.compute", new Parameter("java.lang.String[]", "array"));
        methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is null"));
        methodBuilder.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if the array is empty"));
        method = methodBuilder.build();
        
        tags = method.throwsTags();
        assertThat(tags.size(), is(2));
        assertThat(tags.get(0), is(new ThrowsTag("java.lang.NullPointerException", "if the array is null")));
        assertThat(tags.get(1), is(new ThrowsTag("java.lang.IllegalArgumentException", "if the array is empty")));
	}
	
	@Test
	public void testToString() {
	    DocumentedMethod method = new DocumentedMethod.Builder("void", "Foo.compute").build();
        assertThat(method.toString(), is("void Foo.compute()"));
	  
		method = new DocumentedMethod.Builder("void", "Foo.compute", new Parameter("java.lang.String[]", "array")).build();
		assertThat(method.toString(), is("void Foo.compute(java.lang.String[] array)"));
		
	    method = new DocumentedMethod.Builder("void", "Foo.compute", new Parameter("int", "x"), new Parameter("int", "y")).build();
        assertThat(method.toString(), is("void Foo.compute(int x,int y)"));
        
        method = new DocumentedMethod.Builder("void", "Foo.Foo").build();
        assertThat(method.toString(), is("Foo.Foo()"));
	}
	
	@Test
	public void testEquals() {
		Builder methodBuilder = new DocumentedMethod.Builder("void", "Foo.compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method1 = methodBuilder.build();
		
		assertThat(method1.equals(method1), is(true));
		assertThat(method1.equals(new Object()), is(false));

		methodBuilder = new DocumentedMethod.Builder("void", "Foo.compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method2 = methodBuilder.build();
		
		assertThat(method1.equals(method2), is(true));
		assertThat(method1.hashCode(), is(equalTo(method2.hashCode())));
		
		methodBuilder = new DocumentedMethod.Builder("void", "Foo.foo", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method3 = methodBuilder.build();

		assertThat(method1.equals(method3), is(false));
		assertThat(method1.hashCode(), is(not(equalTo(method3.hashCode()))));
	}

	@Test
	public void testJSon() {
		Builder methodBuilder = new DocumentedMethod.Builder("void", "Foo.compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method1 = methodBuilder.build();

		String json = new Gson().toJson(method1);
		DocumentedMethod method2 = new Gson().fromJson(json, DocumentedMethod.class);
		assertThat(method1, is(equalTo(method2)));
	}

}
