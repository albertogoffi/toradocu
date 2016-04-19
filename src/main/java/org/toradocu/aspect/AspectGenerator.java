package org.toradocu.aspect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import org.toradocu.TranslatedExceptionComment;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.sun.javadoc.ExecutableMemberDoc;

public class AspectGenerator {
	
	private String aspectTemplate;
	private static final Logger LOG = Logger.getLogger(AspectGenerator.class.getName());
	
	public AspectGenerator(String template) {
		this.aspectTemplate = template;
	}
	
	public void create(String testClassName, ExecutableMemberDoc doc, boolean parsingComplete, Set<TranslatedExceptionComment> comments, String completePath) {
		// creates an input stream for the file to be parsed
        try (FileInputStream template = new FileInputStream(aspectTemplate);
             FileOutputStream output = new FileOutputStream(new File(completePath + ".java"))) {
        	CompilationUnit cu = JavaParser.parse(template);
            new MethodChangerVisitor(testClassName, doc, !parsingComplete, comments).visit(cu, null);
            String modifiedAspect = cu.toString();
            String aspectName = completePath.substring(completePath.lastIndexOf(File.separator) + 1);
            modifiedAspect = modifiedAspect.replace("public class Aspect_Template", "public class " + aspectName);
            output.write(modifiedAspect.getBytes());
		} catch (IOException|ParseException e) {
			LOG.severe("Error during aspect creation.");
			e.printStackTrace();
		}
	}
}
