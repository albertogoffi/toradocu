package org.toradocu.translator;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Proposition {
	private String subject, relation, translation;
	private String[] arguments;
	
	public Proposition(String subject, String relation, String... arguments) {
		this.subject = Objects.requireNonNull(subject);
		this.relation = Objects.requireNonNull(relation);
		this.arguments = Objects.requireNonNull(arguments);
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getRelation() {
		return relation;
	}
	
	public String[] getArguments() {
		return arguments;
	}
	
	/**
	 * @return the translation of the proposition
	 */
	public Optional<String> getTranslation() {
		return Optional.ofNullable(translation);
	}
	
	public void setTranslation(String translation) {
		this.translation = translation;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Proposition) {
			Proposition that = (Proposition) obj;
			if (subject.equals(that.getSubject()) &&
				relation.equals(that.getRelation()) &&
				Arrays.equals(arguments, that.getArguments())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(subject, relation, arguments);
	}
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder("(" + subject + ", " + relation);
		if (arguments.length != 0) {
			output.append(", " + Arrays.toString(arguments));
		}
		output.append(")");
		if (getTranslation().isPresent()) {
			output.append(" -> " + getTranslation().orElseGet(() -> ""));
		}		
		return output.toString();
	}
}
