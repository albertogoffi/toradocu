package org.toradocu.extractor;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ThrowsTagTest {

	private final Type npe = new Type("java.lang.NullPointerException");
	private final Type iae = new Type("java.lang.IllegalArgumentException");
	
	@Test
	public void testBasics() {
		ThrowsTag tag = new ThrowsTag(npe, "if x is null");
		assertThat(tag.exceptionComment(), is("if x is null"));
		assertThat(tag.exception(), is(npe));
		assertThat(tag.getCondition().isPresent(), is(false));
		
		tag.setCondition("");
		assertThat(tag.getCondition().isPresent(), is(true));
		assertThat(tag.getCondition().get(), is(emptyString()));
		
		tag.setCondition("(x==null)||(y==null)");
		assertThat(tag.getCondition().isPresent(), is(true));
		assertThat(tag.getCondition().get(), is("(x==null)||(y==null)"));
	}
	
	@Test
	public void testToString() {
		ThrowsTag tag = new ThrowsTag(npe, "if x is null");
		assertThat(tag.toString(), is("@throws java.lang.NullPointerException" + " " + "if x is null"));
	
		tag.setCondition("x == null");
		assertThat(tag.toString(), is("@throws java.lang.NullPointerException" + " " 
				+ "if x is null" + " ==> " + "x == null"));	
	}

	@Test
	public void testEquals() {
		ThrowsTag tag1 = new ThrowsTag(npe, "if x is null");
		ThrowsTag tag2 = new ThrowsTag(npe, "if x is null");
		assertThat(tag1.equals(tag2), is(true));
		assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));
		assertThat(tag1.equals(new Object()), is(false));
		
		tag1.setCondition("x == null");
		tag2.setCondition("x == null");
		assertThat(tag1.equals(tag2), is(true));
		assertThat(tag1.hashCode(), is(equalTo(tag2.hashCode())));
		
		tag2.setCondition("x == null || y == null");
		assertThat(tag1.equals(tag2), is(false));
		
		ThrowsTag tag3 = new ThrowsTag(npe, "if y is null");
		assertThat(tag1.equals(tag3), is(false));
		
		ThrowsTag tag4 = new ThrowsTag(iae, "if x is null");
		assertThat(tag1.equals(tag4), is(false));
	}
}
