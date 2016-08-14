package org.toradocu.extractor;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class ThrowsTagTest {

	@Test
	public void testBasics() {
		ThrowsTag tag = new ThrowsTag("java.lang.NullPointerException", "if x is null");
		assertThat(tag.getComment(), is("if x is null"));
		assertThat(tag.getException(), is("java.lang.NullPointerException"));
		assertThat(tag.getConditions().isPresent(), is(false));
	}
	
	@Test
	public void testToString() {
		ThrowsTag tag = new ThrowsTag("java.lang.NullPointerException", "if x is null");
		assertThat(tag.toString(), is("@throws java.lang.NullPointerException" + " " + "if x is null"));
	
		tag.setConditions("x == null");
		assertThat(tag.toString(), is("@throws java.lang.NullPointerException" + " " 
				+ "if x is null" + " ==> " + "x == null"));	
	}

	@Test
	public void testEquals() {
		ThrowsTag tag1 = new ThrowsTag("java.lang.NullPointerException", "if x is null");
		ThrowsTag tag2 = new ThrowsTag("java.lang.NullPointerException", "if x is null");
		assertThat(tag1.equals(tag2), is(true));
		assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));
		assertThat(tag1.equals(new Object()), is(false));
		
		tag1.setConditions("x == null");
		tag2.setConditions("x == null");
		assertThat(tag1.equals(tag2), is(true));
		assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));
		
		tag2.setConditions("x == null || y == null");
		assertThat(tag1.equals(tag2), is(false));
		
		ThrowsTag tag3 = new ThrowsTag("java.lang.NullPointerException", "if y is null");
		assertThat(tag1.equals(tag3), is(false));
		
		ThrowsTag tag4 = new ThrowsTag("java.lang.IllegalArgumentException", "if x is null");
		assertThat(tag1.equals(tag4), is(false));
	}
}
