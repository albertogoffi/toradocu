package org.toradocu.translator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.JavadocExceptionComment;
import org.toradocu.extractor.Method;
import org.toradocu.extractor.ThrowsTag;

import com.sun.javadoc.ExecutableMemberDoc;

public class ConditionTranslator {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConditionTranslator.class);
	
	public static List<TranslatedExceptionComment> translate(List<Method> methodsToProcess) {
		List<TranslatedExceptionComment> translatedComments = new ArrayList<>();

		// For each method that has comments to translate
		for (Method method : methodsToProcess) {
			// For each comment to translate
			for (ThrowsTag tag : method.tags()) {
				String tagComment = tag.getComment();
				
				StringBuilder logMessage = new StringBuilder("=== " + method.getSignature() + " ===");
				logMessage.append("\n").append("Identifying propositions from: \"" + tagComment + "\"");
				LOG.trace(logMessage.toString());
				
				// We identify propositions in the comment (as a -potentially disconnected- graph)
				Set<String> javaConditions = new HashSet<>();
				List<PropositionList> extractedPropositions = PropositionExtractor.getPropositions(tagComment);		
				
				// We translate subject and predicate into Java code elements
				for (PropositionList propositions : extractedPropositions) {
					translatePropositions(propositions, method);
				}
				
				// We remove from the proposition graph all the proposition for which the translation has failed
//				pruneUntraslatedPropositions(propositionGraph);
				
				// We remove from the proposition graph all the proposition we know are wrong
//				pruneWrongTranslations(propositionGraph);
				
				// We build the Java conditions taking into account also conjunctions
//				Set<Proposition> visitedPropositions = new HashSet<>();
//				for (ConjunctionEdge<Proposition> edge : propositionGraph.edgeSet()) { // TODO Improve this. We do not support multiple conjunctions
//					javaConditions.add(edge.getSource().getTranslation().get() + edge.getConjunction() + edge.getTarget().getTranslation().get());
//					visitedPropositions.add(edge.getSource());
//					visitedPropositions.add(edge.getTarget());
//				}
//				for (Proposition p : propositionGraph.vertexSet()) { // This loop adds propositions not linked with others by a conjunction
//					if (!visitedPropositions.contains(p)) {
//						javaConditions.add(p.getTranslation().get());
//					}
//				}
//				translatedComments.add(new TranslatedExceptionComment(commentToTranslate, javaConditions));
			}
		}
		return translatedComments;
	}

//	private static void pruneWrongTranslations(Graph<Proposition, ConjunctionEdge<Proposition>> propositionGraph) {
//		HashSet<Proposition> propositions = new HashSet<>(propositionGraph.vertexSet());
//		for (Proposition p : propositions) {
//			if (p.getTranslation().isPresent() && p.getTranslation().get().contains("target==null")) {
//				propositionGraph.removeVertex(p);
//			}
//		}
//	}
//
//	private static void pruneUntraslatedPropositions(Graph<Proposition, ConjunctionEdge<Proposition>> propositionGraph) {
//		HashSet<Proposition> propositions = new HashSet<>(propositionGraph.vertexSet());
//		for (Proposition p : propositions) {
//			if (!p.getTranslation().isPresent()) {
//				propositionGraph.removeVertex(p);
//			}
//		}
//	}

	private static void translatePropositions(PropositionList propositionList, Method method) {
	
		for (Proposition p : propositionList.getNodes()) {
			String translation = "";
			
			List<CodeElement> subjectMatches;
			try {
				subjectMatches = Matcher.subjectMatch(p.getSubject(), method);
				if (subjectMatches.isEmpty()) {
					LOG.debug("Failed subject translation for: " + p);
					return;
				}
				
			} catch (ClassNotFoundException e) {
				LOG.error("Unable to load class. Check the classpath");
				return;
			}
			
//			for (CodeElement<?> subjectMatch : subjectMatches) { // A subject can match multiple elements (e.g., "either value...")
//				String translatedPredicate = Matcher.predicateMatch(p.getRelation(), subjectMatch);
//				if (translatedPredicate == null) {
//					LOG.fine("Failed predicate translation for: " + p);
//					continue;
//				}
//					
//				String translatedSubject = subjectMatch.getStringRepresentation();
//				String t = translatedSubject + translatedPredicate;
//				
//				if (t.contains("target==null")) {
//					LOG.fine("Ignored translation: " + t);
//					continue;
//				}
//				LOG.fine("Translated as: " + t);
//				if (translation.isEmpty()) {
//					translation = translatedSubject + translatedPredicate;
//				} else {
//					translation = translation + getConjunction(p.getSubject()) + translatedSubject + translatedPredicate;
//				}
//			}
//			
//			if (!translation.isEmpty()) {
//				p.setTranslation(translation);
//			}
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
