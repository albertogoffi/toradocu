package org.toradocu.generator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.extractor.Type;

import com.beust.jcommander.JCommander;

public class OracleGeneratorTest {
	
	@Test
	public void oracleGeneratorTest() throws Exception {
		String[] args = new String[] {"--target-class", "example.util.Arrays",
									  "--oracle-generation", "true"};
		new JCommander(Toradocu.CONFIGURATION, args);
		
		OracleGenerator oracleGenerator = new OracleGenerator();
		Parameter parameter1 = new Parameter(new Type("java.lang.Integer[]"), "array", 0);
		Parameter parameter2 = new Parameter(new Type("java.lang.Integer"), "element", 1);
		Parameter parameter3 = new Parameter(new Type("java.lang.String[]"), "names", 2);
		ThrowsTag tag = new ThrowsTag("java.lang.NullPointerException", "if array or element is null");
		tag.setCondition("args[0]==null || args[1]==null");
		DocumentedMethod method = new DocumentedMethod.Builder("example.util.Arrays.count", new Type("java.lang.Integer"), 
										parameter1, parameter2, parameter3)
								  		.tag(tag).build();
			
		List<DocumentedMethod> methods = new ArrayList<>();
		methods.add(method);
		
		oracleGenerator.createAspects(methods);
		
		//TODO: check created aspects
		
//		FileUtils.deleteDirectory(new File(Toradocu.CONFIGURATION.getAspectsOutputDir()));
	}
}
