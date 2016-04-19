package org.toradocu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.toradocu.nlp.CodeElement;
import org.toradocu.nlp.ConjunctionEdge;
import org.toradocu.nlp.Matcher;
import org.toradocu.nlp.Proposition;
import org.toradocu.nlp.PropositionExtractor;

import com.sun.javadoc.ExecutableMemberDoc;

public class ConditionTranslator {
	
	private static final Logger LOG = Logger.getLogger(ConditionTranslator.class.getName());
	
	public static List<TranslatedExceptionComment> translate(Set<JavadocExceptionComment> commentsToTranslate) {
		List<TranslatedExceptionComment> translatedComments = new ArrayList<>();

		// For each comment to translate
		for (JavadocExceptionComment commentToTranslate : commentsToTranslate) {
			String javadocComment = commentToTranslate.getComment();
			ExecutableMemberDoc member = commentToTranslate.getMember();
			
			StringBuilder logMessage = new StringBuilder("=== " + member.qualifiedName() + " ===");
			logMessage.append("\n").append("Identifying propositions from: \"" + javadocComment + "\"");
			LOG.fine(logMessage.toString());
			
			// We identify propositions in the comment (as a -potentially disconnected- graph)
			Set<String> javaConditions = new HashSet<>();
			Graph<Proposition, ConjunctionEdge<Proposition>> propositionGraph = PropositionExtractor.getPropositionGraph(javadocComment);			
			
			// We translate subject and predicate into Java code elements
			translatePropositions(propositionGraph, commentToTranslate);
			
			// We remove from the proposition graph all the proposition for which the translation has failed
			pruneUntraslatedPropositions(propositionGraph);
			
			// We remove from the proposition graph all the proposition we know are wrong
			pruneWrongTranslations(propositionGraph);
			
			// We build the Java conditions taking into account also conjunctions
			Set<Proposition> visitedPropositions = new HashSet<>();
			for (ConjunctionEdge<Proposition> edge : propositionGraph.edgeSet()) { // TODO Improve this. We do not support multiple conjunctions
				javaConditions.add(edge.getSource().getTranslation().get() + edge.getConjunction() + edge.getTarget().getTranslation().get());
				visitedPropositions.add(edge.getSource());
				visitedPropositions.add(edge.getTarget());
			}
			for (Proposition p : propositionGraph.vertexSet()) { // This loop adds propositions not linked with others by a conjunction
				if (!visitedPropositions.contains(p)) {
					javaConditions.add(p.getTranslation().get());
				}
			}
			translatedComments.add(new TranslatedExceptionComment(commentToTranslate, javaConditions));
		}
		return translatedComments;
	}

	private static void pruneWrongTranslations(Graph<Proposition, ConjunctionEdge<Proposition>> propositionGraph) {
		HashSet<Proposition> propositions = new HashSet<>(propositionGraph.vertexSet());
		for (Proposition p : propositions) {
			if (p.getTranslation().isPresent() && p.getTranslation().get().contains("target==null")) {
				propositionGraph.removeVertex(p);
			}
		}
	}

	private static void pruneUntraslatedPropositions(Graph<Proposition, ConjunctionEdge<Proposition>> propositionGraph) {
		HashSet<Proposition> propositions = new HashSet<>(propositionGraph.vertexSet());
		for (Proposition p : propositions) {
			if (!p.getTranslation().isPresent()) {
				propositionGraph.removeVertex(p);
			}
		}
	}

	private static void translatePropositions(Graph<Proposition, ConjunctionEdge<Proposition>> propositionGraph, JavadocExceptionComment commentToTranslate) {
		for (Proposition p : propositionGraph.vertexSet()) {
			LOG.fine("Extracted proposition: " + p);
			String translation = "";
			
			List<CodeElement<?>> subjectMatches = Matcher.subjectMatch(p.getSubject(), commentToTranslate.getOriginalMember());
			if (subjectMatches.isEmpty()) {
				LOG.fine("Failed subject translation for: " + p);
				return;
			}
			
			for (CodeElement<?> subjectMatch : subjectMatches) { // A subject can match multiple elements (e.g., "either value...")
				String translatedPredicate = Matcher.predicateMatch(p.getRelation(), subjectMatch);
				if (translatedPredicate == null) {
					LOG.fine("Failed predicate translation for: " + p);
					continue;
				}
					
				String translatedSubject = subjectMatch.getStringRepresentation();
				String t = translatedSubject + translatedPredicate;
				
				if (t.contains("target==null")) {
					LOG.fine("Ignored translation: " + t);
					continue;
				}
				LOG.fine("Translated as: " + t);
				if (translation.isEmpty()) {
					translation = translatedSubject + translatedPredicate;
				} else {
					translation = translation + getConjunction(p.getSubject()) + translatedSubject + translatedPredicate;
				}
			}
			
			if (!translation.isEmpty()) {
				p.setTranslation(translation);
			}
		}
	}

	private static String getConjunction(String subject) {
		if (subject.startsWith("either")) {
			return "||";
		} else if (subject.startsWith("both")) {
			return "&&";
		} else {
			return "";
		}
	}
}
