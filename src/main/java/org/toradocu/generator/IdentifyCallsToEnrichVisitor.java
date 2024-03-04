package org.toradocu.generator;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
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
		if (!analyzedMethodExpr.toString().contains("_lta")) {
			String analyzedMethodName = analyzedMethodExpr.getNameAsString();
			DocumentedExecutable targetCall = ss.getTargetCall();
			String targetCallName = targetCall.getName().substring(targetCall.getName().lastIndexOf('.') + 1);

			if (targetCallName.equals(analyzedMethodName)) {
				try {
					ResolvedMethodDeclaration analyzedMethodDeclaration = analyzedMethodExpr.resolve();
					List<ResolvedType> analyzedMethodParameters = analyzedMethodDeclaration.formalParameterTypes();
					analyzeArgsMatch(analyzedMethodExpr, analyzedMethodParameters, ss, targetCall);
				} catch (Exception e) {
					// The following is a fix in case the JavaParser SymbolSolver fails in resolving
					// a declaration
					log.warn("SymbolSolver failure.");
					NodeList<Expression> analyzedMethodArgs = analyzedMethodExpr.getArguments();
					analyzeArgsMatchFallback(analyzedMethodExpr, analyzedMethodArgs, ss, targetCall);
				}
			}
		}
	}

	@Override
	public void visit(ObjectCreationExpr analyzedConstructorExpr, SupportStructure ss) {
		String analyzedConstructorName = analyzedConstructorExpr.getType().getNameAsString();
		DocumentedExecutable targetCall = ss.getTargetCall();
		String targetCallName = targetCall.getName().substring(targetCall.getName().lastIndexOf('.') + 1);

		if (targetCallName.equals(analyzedConstructorName)) {
			try {
				ResolvedConstructorDeclaration analyzedConstructorDeclaration = analyzedConstructorExpr.resolve();
				List<ResolvedType> analyzedConstructorParameters = analyzedConstructorDeclaration
						.formalParameterTypes();
				analyzeArgsMatch(analyzedConstructorExpr, analyzedConstructorParameters, ss, targetCall);
			} catch (Exception e) {
				// The following is a fix in case the JavaParser SymbolSolver fails in resolving
				// a declaration
				log.warn("SymbolSolver failure.");
				NodeList<Expression> analyzedMethodArgs = analyzedConstructorExpr.getArguments();
				analyzeArgsMatchFallback(analyzedConstructorExpr, analyzedMethodArgs, ss, targetCall);
			}
		}
	}

	private void analyzeArgsMatch(Node analyzedExpr, List<ResolvedType> analyzedParameters, SupportStructure ss,
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

	private void analyzeArgsMatchFallback(Node analyzedExpr, NodeList<Expression> analyzedParameters,
			SupportStructure ss, DocumentedExecutable targetCall) {
		List<DocumentedParameter> argsCall = targetCall.getParameters();
		boolean found = false;
		if (analyzedParameters.size() == argsCall.size()) {
			if (analyzedParameters.size() == 0) {
				found = true;
			} else {
				for (int i = 0; i < analyzedParameters.size(); i++) {
					found = false;
					DocumentedParameter argWanted = argsCall.get(i);
					Expression argMethod = analyzedParameters.get(i);
					String argWantedType = argWanted.getType().getCanonicalName();
					try {
						String argMethodType = argMethod.calculateResolvedType().erasure().describe();
						if (argMethodType.contains("<")) {
							String firstPart = argMethodType.substring(0, argMethodType.indexOf("<"));
							String secondPart = argMethodType.substring(argMethodType.lastIndexOf(">") + 1);
							argMethodType = firstPart + secondPart;
						}
						found = argWantedType.equals(argMethodType);
						if (!found) {
							log.warn(
									"Match excluded by alternative match algorithm. Since this is not perfect (for example in the case of implicit casting) a potential valid check may have been excluded.");
							log.warn("Arguments not matching: wanted: " + argWantedType + "; found: " + argMethodType);
							break;
						}
					} catch (Exception e) {
						log.error("Failure during resolvedType calculation", e);
					}
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
