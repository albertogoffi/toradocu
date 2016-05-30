package org.toradocu.translator;

public enum Conjunction { 
	OR, AND;
	
	@Override
	public String toString() {
		switch (this) {
		case AND:
			return "&&";
		case OR:
			return "||";
		default:
			return "";
		}
	}
};