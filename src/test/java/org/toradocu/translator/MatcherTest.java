package org.toradocu.translator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.extractor.Type;

public class MatcherTest {

	@Test
	public void test() throws Throwable {
		Parameter p1 = new Parameter(new Type("Employee"), "employee", 0);
		Parameter p2 = new Parameter(new Type("Double"), "salary", 1);
		DocumentedMethod methodUnderTest = new DocumentedMethod.Builder(new Type(ClassUnderTest.class.getName()), "setSalary", null, p1, p2)
				.tag(new ThrowsTag(new Type("NullPointerException"), "if employee or salary are null"))
				.build();
		
		Set<CodeElement<?>> matchList = Matcher.subjectMatch("employee", methodUnderTest);
		assertThat(matchList.size(), is(1));
		
		CodeElement<?> codeElement = matchList.iterator().next();
		assertThat(codeElement.getClass(), is(ParameterCodeElement.class));
		assertThat(codeElement.getJavaExpression(), is("args[0]"));
	}
	
	public class ClassUnderTest {
		public void setSalary(Object Employee, Double salary) {}
		
		@Override
		public String toString() {
			return "ClassUnderTest";
		}
	}
}
