package org.toradocu.extractor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ParameterTest {

    @Test
    public void testBasics() {
        Parameter p = new Parameter("org.toradocu.Parameter", "par");
        assertThat(p.getType(), is("org.toradocu.Parameter"));
        assertThat(p.getSimpleType(), is("Parameter"));
        assertThat(p.getName(), is("par"));
        assertThat(p.getDimension(), is(""));
        assertThat(p.getNullability(), is(nullValue()));

        Parameter intPar = new Parameter("int", "par");
        assertThat(intPar.getType(), is("int"));
        assertThat(intPar.getSimpleType(), is("int"));
    }
    
    @Test
    public void testToString() {
    	String type = "org.toradocu.Parameter";
    	String name = "par";
    	Parameter p = new Parameter(type, name);
    	assertThat(p.toString(), is(type + " " + name));
    }
    
    @Test
    public void testNullability() {
        Parameter p = new Parameter("org.toradocu.Parameter", "par");
        assertThat(p.getNullability(), is(nullValue()));
        
        p = new Parameter("org.toradocu.Parameter", "par", false);
        assertThat(p.getNullability(), is(false));
        
        p = new Parameter("org.toradocu.Parameter", "par", true);
        assertThat(p.getNullability(), is(true));
    }
    
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
	public void testEquals() {
        Parameter p1 = new Parameter("org.toradocu.Parameter", "par");
        assertThat(p1.equals(p1), is(true));
        assertThat(p1.hashCode(), is(p1.hashCode()));
        
        Parameter p1Copy = new Parameter("org.toradocu.Parameter", "par");
        assertThat(p1.equals(p1Copy), is(true));
        assertThat(p1.hashCode(), is(equalTo(p1Copy.hashCode())));
        
        Parameter differentPar = new Parameter("org.toradocu.Parameter", "foo");
        assertThat(p1.equals(differentPar), is(false));
        
        Object anObject = new Object();
        assertThat(p1.equals(anObject), is(false));
        assertThat(p1.hashCode(), is(not(anObject.hashCode())));
    } 

}
