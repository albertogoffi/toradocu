package org.toradocu.generator;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

public class IdentifyCallsToEnrichVisitor extends VoidVisitorAdapter<SupportStructure> {

	/** {@code Logger} for this class. */
	private static final Logger log = LoggerFactory.getLogger(IdentifyCallsToEnrichVisitor.class);

	@Override
	public void visit(MethodCallExpr analyzedMethodExpr, SupportStructure ss) {
		DocumentedExecutable targetCall = ss.getTargetCall();
		String targetCallName = targetCall.getName().substring(targetCall.getName().lastIndexOf('.') + 1);
		String analyzedMethodName = analyzedMethodExpr.getNameAsString();
		if (targetCallName.equals(analyzedMethodName)) {
			try {
				ResolvedMethodDeclaration analyzedMethodDeclaration = analyzedMethodExpr.resolve();
				List<ResolvedType> analyzedMethodParameters = analyzedMethodDeclaration.formalParameterTypes();
				extracted(analyzedMethodExpr, analyzedMethodParameters, ss, targetCall);
			} catch (Exception e) {
				// TODO This is a temporary fix. We should analyze why the SymbolSolver
				// sometimes fails in detecting the constructor/method declaration
				log.warn("SymbolSolver failure. A check will be added but it may need to be manually removed.", e);
				boolean cont = true;
				Node n = analyzedMethodExpr;
				do {
					Optional<Node> optionalParent = n.getParentNode();
					if (!optionalParent.isPresent()) {
						throw new RuntimeException("Method creation expression found without expression statement");
					} else {
						n = optionalParent.get();
						if (n instanceof ExpressionStmt) {
							ExpressionStmt es = (ExpressionStmt) n;
							String comment = "";
							Optional<Comment> optionalComment = es.getComment();
							if (optionalComment.isPresent())
								comment = comment + optionalComment.get().asString();
							es.setLineComment(
									comment + "Check added due to SymbolSolver failure. Check if removal is needed.");
							ss.getTargetCallsList().add(es);
							cont = false;
						}
					}

				} while (cont);
			}
		}
	}

	@Override
	public void visit(ObjectCreationExpr analyzedConstructorExpr, SupportStructure ss) {
		DocumentedExecutable targetCall = ss.getTargetCall();
		String targetCallName = targetCall.getName().substring(targetCall.getName().lastIndexOf('.') + 1);
		String analyzedConstructorName = analyzedConstructorExpr.getType().getNameAsString();
		if (targetCallName.equals(analyzedConstructorName)) {
			try {
				ResolvedConstructorDeclaration analyzedConstructorDeclaration = analyzedConstructorExpr.resolve();
				List<ResolvedType> analyzedConstructorParameters = analyzedConstructorDeclaration
						.formalParameterTypes();
				extracted(analyzedConstructorExpr, analyzedConstructorParameters, ss, targetCall);
			} catch (Exception e) {
				// TODO This is a temporary fix. We should analyze why the SymbolSolver
				// sometimes fails in detecting the constructor/method declaration
				log.warn("SymbolSolver failure. A check will be added but it may need to be manually removed.", e);
				boolean cont = true;
				Node n = analyzedConstructorExpr;
				do {
					Optional<Node> optionalParent = n.getParentNode();
					if (!optionalParent.isPresent()) {
						throw new RuntimeException("Method creation expression found without expression statement");
					} else {
						n = optionalParent.get();
						if (n instanceof ExpressionStmt) {
							ExpressionStmt es = (ExpressionStmt) n;
							String comment = "";
							Optional<Comment> optionalComment = es.getComment();
							if (optionalComment.isPresent())
								comment = comment + optionalComment.get().asString();
							es.setLineComment(
									comment + "Check added due to SymbolSolver failure. Check if removal is needed.");
							ss.getTargetCallsList().add(es);
							cont = false;
						}
					}

				} while (cont);
			}
		}
	}

	private void extracted(Node analyzedExpr, List<ResolvedType> analyzedParameters, SupportStructure ss,
			DocumentedExecutable targetCall) {

		List<DocumentedParameter> argsCall = targetCall.getParameters();

		boolean found = false;
		if (analyzedParameters.size() == argsCall.size()) {
			if (analyzedParameters.size() == 0) {
				found = true;
			} else {
				for (int i = 0; i < analyzedParameters.size(); i++) {
					found = false;
					ResolvedType argMethod = analyzedParameters.get(i);
					DocumentedParameter argWanted = argsCall.get(i);
					String argMethodString = argMethod.describe();
					String argWantedString = argWanted.toString().substring(0,
							argWanted.toString().indexOf(" " + argWanted.getName()));
					// Replace $ with . to allow comparison of inner classes
					argWantedString = argWantedString.replace("$", ".");
					found = argMethodString.equals(argWantedString);
					if (!found)
						break;
				}
			}
			if (found) {
				boolean cont = true;
				Node n = analyzedExpr;
				do {
					Optional<Node> optionalParent = n.getParentNode();
					if (!optionalParent.isPresent()) {
						throw new RuntimeException("Method creation expression found without expression statement");
					} else {
						n = optionalParent.get();
						if (n instanceof ExpressionStmt) {
							ss.getTargetCallsList().add((ExpressionStmt) n);
							cont = false;
						}
					}

				} while (cont);
			}
		}
	}

}
