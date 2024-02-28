package org.toradocu.generator;

import java.util.List;

import org.toradocu.extractor.DocumentedExecutable;

import com.github.javaparser.ast.stmt.ExpressionStmt;

public class SupportStructure {
	private DocumentedExecutable targetCall;
	private List<ExpressionStmt> targetCallsList;

	public SupportStructure(DocumentedExecutable targetCall, List<ExpressionStmt> targetCallsList) {
		super();
		this.targetCall = targetCall;
		this.targetCallsList = targetCallsList;
	}

	public DocumentedExecutable getTargetCall() {
		return targetCall;
	}

	public void setTargetCall(DocumentedExecutable targetCall) {
		this.targetCall = targetCall;
	}

	public List<ExpressionStmt> getTargetCallsList() {
		return targetCallsList;
	}

	public void setTargetCallsList(List<ExpressionStmt> targetCallsList) {
		this.targetCallsList = targetCallsList;
	}

}
