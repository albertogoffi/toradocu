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
        Parameter p = new Parameter(new Type("org.toradocu.Parameter"), "par");
        assertThat(p.getType().getQualifiedName(), is("org.toradocu.Parameter"));
        assertThat(p.getType().getName(), is("Parameter"));
        assertThat(p.getName(), is("par"));
        assertThat(p.getNullability(), is(nullValue()));

        Parameter intPar = new Parameter(new Type("int"), "par", false);
        assertThat(intPar.getType().getName(), is("int"));
        assertThat(intPar.getType().getQualifiedName(), is("int"));
        assertThat(intPar.getNullability(), is(false));
        
        intPar = new Parameter(new Type("int"), "par", true);
        assertThat(intPar.getNullability(), is(true));
    }
    
    @Test
    public void testToString() {
    	Type type = new Type("org.toradocu.Parameter");
    	String name = "par";
    	Parameter p = new Parameter(type, name);
    	assertThat(p.toString(), is(type + " " + name));
    }
    
    @Test
    public void testNullability() {
    	Type type = new Type("org.toradocu.Parameter");
        Parameter p = new Parameter(type, "par");
        assertThat(p.getNullability(), is(nullValue()));
        
        p = new Parameter(type, "par", false);
        assertThat(p.getNullability(), is(false));
        
        p = new Parameter(type, "par", true);
        assertThat(p.getNullability(), is(true));
    }
    
	@Test
	public void testEquals() {
		Type type = new Type("org.toradocu.Parameter");
        Parameter p1 = new Parameter(type, "par");
        assertThat(p1.equals(p1), is(true));
        assertThat(p1.hashCode(), is(p1.hashCode()));
        
        Parameter p1Copy = new Parameter(type, "par");
        assertThat(p1.equals(p1Copy), is(true));
        assertThat(p1.hashCode(), is(equalTo(p1Copy.hashCode())));
        
        Parameter differentPar = new Parameter(type, "foo");
        assertThat(p1.equals(differentPar), is(false));
        
        Object anObject = new Object();
        assertThat(p1.equals(anObject), is(false));
        assertThat(p1.hashCode(), is(not(anObject.hashCode())));
    } 

}
