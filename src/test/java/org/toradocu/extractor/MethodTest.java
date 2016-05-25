package org.toradocu.extractor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.toradocu.extractor.Method.Builder;

import com.google.gson.Gson;

public class MethodTest {

	@Test
	public void testEquals() {
		Builder methodBuilder = new Method.Builder("compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		Method method1 = methodBuilder.build();
		
		methodBuilder = new Method.Builder("compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		Method method2 = methodBuilder.build();

		assertTrue(method1.equals(method2));
	}
	
	@Test
	public void testJSon() {
		Builder methodBuilder = new Method.Builder("compute", new Parameter("java.lang.String[]", "array"));
		methodBuilder.tag(new ThrowsTag("java.lang.NullPointerException", "if the array is empty"));
		Method method1 = methodBuilder.build();

		String json = new Gson().toJson(method1);
		Method method2 = new Gson().fromJson(json, Method.class);
		assertEquals(method1, method2);
	}

}
