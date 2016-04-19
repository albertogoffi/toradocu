package org.toradocu.aspect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.toradocu.TranslatedExceptionComment;

import com.github.javaparser.ASTHelper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;

public class MethodChangerVisitor extends VoidVisitorAdapter<Object> {
	
	private static final Logger LOG = Logger.getLogger(MethodChangerVisitor.class.getName());
    private Set<TranslatedExceptionComment> comments;
    private ExecutableMemberDoc doc;
	private String testClassName;
	private boolean incomplete;

	public MethodChangerVisitor(String testClassName, ExecutableMemberDoc doc, boolean incomplete, Set<TranslatedExceptionComment> comments) {
		this.incomplete = incomplete;
    	this.testClassName = testClassName;
		this.comments = comments;
    	this.doc = doc;
	}

	@Override
    public void visit(MethodDeclaration n, Object arg) {
    	if (n.getName().equals("advice")) {
    		String pointcut = "";
    		ClassDoc containingClass = doc.containingClass();

    		// Avoid to add within declaration if we target a constructor defined in an abstract class.
    		// This is because such constructors are not called directly in the test suite.
    		if (doc instanceof MethodDoc || (doc instanceof ConstructorDoc && !containingClass.isAbstract())) { 
    			pointcut = "call(" + getPointcutSignature(doc) + ")";
    			if (!testClassName.isEmpty()) {
    				pointcut += " && within(" + testClassName + ")";
    			}
    		} else {
    			pointcut = "execution(" + getPointcutSignature(doc) + ")";
    		}
    		
    		AnnotationExpr annotation = new SingleMemberAnnotationExpr(new NameExpr("Around"), new StringLiteralExpr(pointcut));
    		List<AnnotationExpr> annotations = n.getAnnotations();
    		annotations.add(annotation);
    		n.setAnnotations(annotations);
    		
	    	if (!incomplete) { // Here we assume that the first statement in AspectTemplate.advice is "boolean expectedExceptionsComplete = false;"
    			ExpressionStmt stmt = (ExpressionStmt) n.getBody().getStmts().get(0);
	    		VariableDeclarationExpr exp = (VariableDeclarationExpr) stmt.getExpression();
	    		BooleanLiteralExpr init = (BooleanLiteralExpr) exp.getVars().get(0).getInit();
	    		init.setValue(true);
    		}
    	} else if (n.getName().equals("getExpectedExceptions")) {
    		String addExpectedExceptionPart1 = "{try{expectedExceptions.add(";
        	String addExpectedExceptionPart2 = ");} catch (ClassNotFoundException e) {"
             								+ "System.err.println(\"Class not found!\" + e);}}";
        	
        	for (TranslatedExceptionComment translatedComment : comments) {
        		for (String condition : translatedComment.getConditions()) {
	        		String check = condition;
					check = addCasting(check);
	        		
	        		IfStmt ifStmt = new IfStmt();
	        		Expression conditionExpression;
	    			try {
	    				conditionExpression = JavaParser.parseExpression(check);
	    				ifStmt.setCondition(conditionExpression);
	    				String addExpectedException = addExpectedExceptionPart1 + 
	    											  "Class.forName(\"" + translatedComment.getException() + "\")" +
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
	    				LOG.severe("Parsing error during the apect creation.");
	    				e.printStackTrace();
	    			}	
        		}
        	}
        	
        	try {
    			ASTHelper.addStmt(n.getBody(), JavaParser.parseStatement("return expectedExceptions;"));
    		} catch (ParseException e) {
    			LOG.severe("Parsing error during the apect creation.");
    			e.printStackTrace();
    		}
    	}
    }

	private String getPointcutSignature(ExecutableMemberDoc doc) {
		String pointcut = doc.modifiers() + " ";
		
		if (doc instanceof MethodDoc) { // Standard methods
			pointcut += getRawQualifiedTypeName(((MethodDoc) doc).returnType()) + " ";
			pointcut += doc.qualifiedName() + "(";
		} else { // Constructors
			pointcut += doc.containingClass().qualifiedName() + ".new("; 
		}
		
		for (Parameter parameter : doc.parameters()) {
			String type = getRawQualifiedTypeName(parameter.type());
			pointcut += type + ","; // We add one comma at the end of the parameters list
		}
		if (pointcut.endsWith(",")) {
			pointcut = pointcut.substring(0, pointcut.length() - 1); // Remove the last comma in parameters list
		}
		
		if (doc.isVarArgs()) {
			if (pointcut.endsWith("[]")) { // Remove "[]" part of the qualified name 
 				pointcut = pointcut.substring(0, pointcut.length() - 2);
			}
			pointcut += "...";
		}
		
		pointcut += ")";
		return pointcut;
	}
	
	private String getRawQualifiedTypeName(Type type) {
		String rawTypeName = null;
		if (type.asTypeVariable() != null) {
			rawTypeName = Object.class.getCanonicalName(); // If the parameter IS a TypeVariable
		} else {
			rawTypeName = type.qualifiedTypeName();
			if (rawTypeName.contains("<")) { // Handling of parameterized type such as Map<? extends K, ? extends V>
				rawTypeName = rawTypeName.substring(0, rawTypeName.indexOf("<"));
			}
			String dimension = type.dimension();
			if (!dimension.isEmpty()) {
				rawTypeName += dimension;
			}
		}
		return rawTypeName;
	}

	private String addCasting(String check) {
		// Casting of parameters in the check
		Parameter[] parameterTypes = doc.parameters();
		for (int i = 0; i < parameterTypes.length; i++) {
			Type type = parameterTypes[i].type();
			if (type.asTypeVariable() != null || type.asParameterizedType() != null) {
				continue; // Avoid the casting of a TypeVariable or of a ParametrizedType
			}
			String typeString = parameterTypes[i].type().qualifiedTypeName();
			if (type.dimension().contains("[]")) {
				typeString += type.dimension(); // Add dimensions for parameters that are arrays
			}
			check = check.replace("args[" + i + "]", "((" + typeString + ")args[" + i + "])");
		}
		
		// Casting of target object in check
		check = check.replace("target.", "((" + doc.containingClass().qualifiedName() + ")target).");
		
		return check;
	}
}