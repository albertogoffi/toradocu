package org.toradocu.generator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ThrowsTag;

import com.beust.jcommander.JCommander;

public class OracleGeneratorTest {
	
	@Test
	public void oracleGeneratorTest() throws Exception {
		final Configuration CONF = Configuration.INSTANCE;
		String[] args = new String[] {"--targetClass", "example.util.Arrays",
									  "--oracleGeneration", "true"};
		new JCommander(CONF, args);
		
		OracleGenerator oracleGenerator = new OracleGenerator();
		Parameter parameter1 = new Parameter("java.lang.Integer[]", "array");
		Parameter parameter2 = new Parameter("java.lang.Integer", "element");
		ThrowsTag tag = new ThrowsTag("java.lang.NullPointerException", "if array or element is null");
		tag.setConditions("args[0]==null || args[1]==null");
		DocumentedMethod method = new DocumentedMethod.Builder("java.lang.Integer", "example.util.Arrays.count", parameter1, parameter2)
								  		.tag(tag).build();
			
		List<DocumentedMethod> methods = new ArrayList<>();
		methods.add(method);
		
		oracleGenerator.createAspects(methods);
	}
}
