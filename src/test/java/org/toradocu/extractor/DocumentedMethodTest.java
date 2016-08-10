package org.toradocu.extractor;

import static org.junit.Assert.*;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;
import org.toradocu.extractor.DocumentedMethod.Builder;

import com.google.gson.Gson;

public class DocumentedMethodTest {
	
	@Test
	public void testToString() {
	    DocumentedMethod method = new DocumentedMethod.Builder("Foo.compute").build();
        assertThat(method.toString(), is("Foo.compute()"));
	  
		method = new DocumentedMethod.Builder("Foo.compute", new Parameter("java.lang.String[]", "array")).build();
		assertThat(method.toString(), is("Foo.compute(java.lang.String[] array)"));
		
	    method = new DocumentedMethod.Builder("Foo.compute", new Parameter("int", "x"), new Parameter("int", "y")).build();
        assertThat(method.toString(), is("Foo.compute(int x,int y)"));
	}
	
	@Test
	public void testEquals() {
		Builder methodBuilder = new DocumentedMethod.Builder("Foo.compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method1 = methodBuilder.build();

		methodBuilder = new DocumentedMethod.Builder("Foo.compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method2 = methodBuilder.build();
		
		assertTrue(method1.equals(method2));
		
		methodBuilder = new DocumentedMethod.Builder("Foo.foo", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method3 = methodBuilder.build();

		assertFalse(method1.equals(method3));
	}

	@Test
	public void testMultipleTags() {
		Builder methodBuilder = new DocumentedMethod.Builder("Foo.compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method = methodBuilder.build();
		
		List<ThrowsTag> tags = method.throwsTags();
		assertThat(tags.size(), is(1));
		assertThat(tags.get(0), is(new ThrowsTag("java.lang.NullPointerException", "if the array is empty")));
		
		methodBuilder = new DocumentedMethod.Builder("Foo.compute", new Parameter("java.lang.String[]", "array"));
        methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is null"));
        methodBuilder.tag(new ThrowsTag("java.lang.IllegalArgumentException", "if the array is empty"));
        method = methodBuilder.build();
        
        tags = method.throwsTags();
        assertThat(tags.size(), is(2));
        assertThat(tags.get(0), is(new ThrowsTag("java.lang.NullPointerException", "if the array is null")));
        assertThat(tags.get(1), is(new ThrowsTag("java.lang.IllegalArgumentException", "if the array is empty")));
	}

	@Test
	public void testJSon() {
		Builder methodBuilder = new DocumentedMethod.Builder("Foo.compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		DocumentedMethod method1 = methodBuilder.build();

		String json = new Gson().toJson(method1);
		DocumentedMethod method2 = new Gson().fromJson(json, DocumentedMethod.class);
		assertEquals(method1, method2);
	}

}
