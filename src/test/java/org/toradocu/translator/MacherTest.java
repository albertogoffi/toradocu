package org.toradocu.translator;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.toradocu.extractor.Method;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ThrowsTag;

public class MacherTest {

	@Test
	public void test() throws Throwable {
		Parameter p1 = new Parameter("Employee", "employee");
		Parameter p2 = new Parameter("Double", "salary");
		Method methodUnderTest = new Method.Builder(ClassUnderTest.class.getName() + ".setSalary", p1, p2)
				.tag(new ThrowsTag("NullPointerException", "if employee or salary are null"))
				.build();
		
		List<CodeElement> matchList = Matcher.subjectMatch("employee", methodUnderTest);
		assertThat(matchList.size(), is(1));
		
		CodeElement codeElement = matchList.get(0);
		assertThat(codeElement.getClass(), is(ParameterCodeElement.class));
		assertThat(codeElement.getStringRepresentation(), is("args[0]"));
	}
	
	public class ClassUnderTest {
		public void setSalary(Object Employee, Double salary) {}
		
		@Override
		public String toString() {
			return "ClassUnderTest";
		}
	}
}
