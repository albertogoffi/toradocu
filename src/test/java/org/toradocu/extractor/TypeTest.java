package org.toradocu.extractor;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class TypeTest {
	
	@Test
	public void testBasics() throws Exception {
		Type typeInDefaultPackage = new Type("Foo[]");
		assertThat(typeInDefaultPackage.getSimpleName(), is("Foo[]"));
		assertThat(typeInDefaultPackage.getQualifiedName(), is("Foo[]"));
		assertThat(typeInDefaultPackage.isArray(), is(true));
		assertThat(typeInDefaultPackage.toString(), is("Foo[]"));
		assertThat(typeInDefaultPackage.equals(typeInDefaultPackage), is(true));
		assertThat(typeInDefaultPackage.equals(new Object()), is(false));
		
		Type standardType = new Type("example.Foo");
		assertThat(standardType.getSimpleName(), is("Foo"));
		assertThat(standardType.getQualifiedName(), is("example.Foo"));
		assertThat(standardType.isArray(), is(false));
		assertThat(standardType.toString(), is("example.Foo"));
		assertThat(standardType.equals(typeInDefaultPackage), is(false));
	}
	
	@Test
	public void testConstructor() throws Exception {
		try {
			new Type(null);
			fail("Expected NullPointerException not thrown");
		} catch (NullPointerException e) {}
		
		try {
			new Type(".example.Foo");
			fail("Expected IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {}
		
		try {
			new Type("example.Foo.");
			fail("Expected IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {}
	}
}
