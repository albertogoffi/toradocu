package org.toradocu.generator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ThrowsTag;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodChangerVisitor extends VoidVisitorAdapter<Object> {
	
	/** {@code DocumentedMethod} for which generate an aspect */
	private DocumentedMethod method;
	/** {@code Logger} for this class. */
	private static final Logger LOG = LoggerFactory.getLogger(MethodChangerVisitor.class);
	/** Holds Toradocu configuration options. */
	private final Configuration CONF = Configuration.INSTANCE;

	public MethodChangerVisitor(DocumentedMethod method) {
		this.method = method;
	}

	@Override
    public void visit(MethodDeclaration n, Object arg) {
    	if (n.getName().equals("advice")) {
    		String pointcut = "";
    		
    		if (method.isAConstructor()) {
    			pointcut = "execution(" + getPointcutSignature(method) + ")";
    		} else {
    			pointcut = "call(" + getPointcutSignature(method) + ")";
    			pointcut += " && within(" + CONF.getTestClass() + ")";
    		}
    		
    		AnnotationExpr annotation = new SingleMemberAnnotationExpr(new NameExpr("Around"), new StringLiteralExpr(pointcut));
    		List<AnnotationExpr> annotations = n.getAnnotations();
    		annotations.add(annotation);
    		n.setAnnotations(annotations);
    	} else if (n.getName().equals("getExpectedExceptions")) {
    		String addExpectedExceptionPart1 = "{try{expectedExceptions.add(";
        	String addExpectedExceptionPart2 = ");} catch (ClassNotFoundException e) {"
             								+ "System.err.println(\"Class not found!\" + e);}}";
        	
        	for (ThrowsTag tag : method.throwsTags()) {
        		String condition = tag.getConditions().orElse("");
        		if (condition.isEmpty()) {
        			continue;
        		}
        		
				condition = addCasting(condition);
        		
        		IfStmt ifStmt = new IfStmt();
        		Expression conditionExpression;
    			try {
    				conditionExpression = JavaParser.parseExpression(condition);
    				ifStmt.setCondition(conditionExpression);
    				String addExpectedException = addExpectedExceptionPart1 + 
    											  "Class.forName(\"" + tag.getException() + "\")" +
    											  addExpectedExceptionPart2;
    				ifStmt.setThenStmt(JavaParser.parseBlock(addExpectedException));
    		
    				ClassOrInterfaceType exceptionType = new ClassOrInterfaceType("java.lang.NullPointerException");
    				List<com.github.javaparser.ast.type.Type> types = new ArrayList<>();
    				types.add(exceptionType);
    				CatchClause catchClause = new CatchClause(0, null, types, new VariableDeclaratorId("e"), JavaParser.parseBlock("{}"));
    				List<CatchClause> catchClauses = new ArrayList<>();
    				catchClauses.add(catchClause);
    				
    				TryStmt nullCheckTryCatch = new TryStmt();
    				nullCheckTryCatch.setTryBlock(JavaParser.parseBlock("{" + ifStmt.toString() + "}"));
    				nullCheckTryCatch.setCatchs(catchClauses);
    	        	
    	        	ASTHelper.addStmt(n.getBody(), nullCheckTryCatch);
    			} catch (ParseException e) {
    				LOG.error("Parsing error during the apect creation.", e);
    				e.printStackTrace();
        		}
        	}
        	
        	try {
    			ASTHelper.addStmt(n.getBody(), JavaParser.parseStatement("return expectedExceptions;"));
    		} catch (ParseException e) {
    			LOG.error("Parsing error during the apect creation.", e);
    			e.printStackTrace();
    		}
    	}
    }

	private String getPointcutSignature(DocumentedMethod method) {
		String pointcut = "";// doc.modifiers() + " ";
		
		if (!method.isAConstructor()) { // Regular methods
			pointcut += method.getReturnType() + " ";
			pointcut += method.getName() + "(";
		} else { // Constructors
			pointcut += method.getContainingClass() + ".new("; 
		}
		
		for (Parameter parameter : method.getParameters()) {
			pointcut += parameter.getType() + ","; // We add one comma at the end of the parameters list
		}
		if (pointcut.endsWith(",")) {
			pointcut = pointcut.substring(0, pointcut.length() - 1); // Remove the last comma in parameters list
		}
		
		pointcut += ")";
		return pointcut;
	}

	private String addCasting(String condition) {	
		int index = 0;
		for (Parameter parameter : method.getParameters()) {
			String type = parameter.getType();
			condition = condition.replace("args[" + index + "]", "((" + type + ") args[" + index + "])");
			index++;
		}
		
		// Casting of target object in check
		condition = condition.replace("target.", "((" + method.getContainingClass() + ") target).");		
		return condition;
	}
}