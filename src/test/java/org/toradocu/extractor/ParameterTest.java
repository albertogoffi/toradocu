package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ParameterTest {

	@Test
	public void testNoDimension() {
		Parameter p = new Parameter("NoDim", "foo");
		assertThat(p.getDimension(), is(""));
	}

	@Test
	public void testSingleDimension() {
		Parameter p = new Parameter("Type[]", "foo");
		assertThat(p.getDimension(), is("[]"));
	}
	
	@Test
	public void testMultipleDimension() {
		Parameter p = new Parameter("Type[][][]", "foo");
		assertThat(p.getDimension(), is("[][][]"));
	}
	
	@Test
	public void testSimpleType() {
		Parameter p = new Parameter("org.toradocu.Parameter", "par");
		assertThat(p.getType(), is("org.toradocu.Parameter"));
		assertThat(p.getSimpleType(), is("Parameter"));
	}
}
