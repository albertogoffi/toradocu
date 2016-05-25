package org.toradocu.extractor;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import com.google.gson.Gson;

public class MethodTest {

	@Test
	public void test() {
		Gson gson = new Gson();
		
		ThrowsTag throwsTag = new ThrowsTag("java.lang.NullPointerException", "if the array is empty");
		Method method = new Method.Builder("compute", new Parameter("java.lang.String[]", "array"))
								  .tag(throwsTag).build();
		
		String json = gson.toJson(method);
		System.out.println(json);
		
		Method methodDeserialized = gson.fromJson(json, Method.class);
		assertThat(methodDeserialized, is(method));
	}

}
