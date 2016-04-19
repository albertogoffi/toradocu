package org.toradocu.nlp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.toradocu.nlp.ConjunctionEdge.Conjunction;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraph.OutputFormat;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class SentenceParser {

	private List<HasWord> sentence;
	private String sentenceAsString;
	private List<SemanticGraphEdge> subjectRelations, copulaRelations, complementRelations, conjunctionRelations;
	private SemanticGraph semanticGraph;
	private static final Logger LOG = Logger.getLogger(SentenceParser.class.getName());

	public SentenceParser(List<HasWord> sentence) {
		this.sentence = sentence;
		this.sentenceAsString = sentence.stream().map(Object::toString).collect(Collectors.joining(" "));
	}
	
	public Graph<Proposition,ConjunctionEdge<Proposition>> getPropositionGraph() throws NotSupportedException {
		Map<IndexedWord, Proposition> propositionMap = new HashMap<>();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Graph<Proposition, ConjunctionEdge<Proposition>> propositionGraph = new SimpleGraph(ConjunctionEdge.class);	
		
		init(); // Initialization phase where we extract information from the sentence
		
		// For each subject in the sentence we try to create a proposition
		for (SemanticGraphEdge subjectRelation : subjectRelations) {
			IndexedWord subjectAsIndexWord = subjectRelation.getDependent();
			String subject = getSubject(subjectAsIndexWord);
			String relation = getPredicate(subjectRelation.getGovernor());
			Proposition proposition = new Proposition(subject, relation);
			propositionMap.put(subjectAsIndexWord, proposition);
		}
		
		// For each conjunction in the sentence we create a edge connecting different nodes representing propositions
		for (SemanticGraphEdge conjunctionRelation : conjunctionRelations) {
			Proposition p1 = propositionMap.get(conjunctionRelation.getGovernor());
			Proposition p2 = propositionMap.get(conjunctionRelation.getDependent());
			if (p1 != null && p2 != null) {
				propositionGraph.addVertex(p1);
				propositionGraph.addVertex(p2);
				propositionGraph.addEdge(p1, p2, new ConjunctionEdge<Proposition>(p1, p2, getConjunction(conjunctionRelation)));
				propositionMap.remove(conjunctionRelation.getGovernor());
				propositionMap.remove(conjunctionRelation.getDependent());
			}
		}
		
		// Add to graph propositions not part of a conjunction relation
		for (IndexedWord key : propositionMap.keySet()) {
			Proposition p = propositionMap.get(key);
			propositionGraph.addVertex(p);
		}
		
		return propositionGraph;
	}

	private Conjunction getConjunction(SemanticGraphEdge conjunctionRelation) {
		String conjunctionRelationSpecific = conjunctionRelation.getRelation().getSpecific();
		Conjunction operator;
		switch (conjunctionRelationSpecific) {
		case "or":
			operator = Conjunction.OR;
			break;
		case "and":
			operator = Conjunction.AND;
			break;
		default:
			operator = null;
		}
		return operator;
	}
	
	private String getSubject(IndexedWord subject) {
		List<String> STOPWORDS = Arrays.asList("a", "an", "the");
		
		// When the subject is a compound name (e.g., JNDI name) we have to complete the subject
		Optional<SemanticGraphEdge> compoundEdge = getRelations("compound", "det", "advmod").stream()
				.filter(e -> e.getGovernor().equals(subject))
				.filter(e -> !STOPWORDS.contains(e.getDependent().word()))
				.findFirst();
		if (compoundEdge.isPresent()) {
			return compoundEdge.get().getDependent().word() + " " + subject.word();
		} else {
			return subject.word();
		}
	}

	private String getPredicate(IndexedWord governor) throws NotSupportedException {
		// Copula handling. In the case of copula, gov is also the governor of the copula relation
		String predicate = getPredicateForCopula(governor);
		
		// Passive form handling
		if (predicate == null) {
			predicate = getPredicateForPassiveForm(governor);
		}
		
		// Non-copula handling
		if (predicate == null) {
			predicate = getPredicateForNonCopula(governor);
		}
		
		// Conjunction handling (with or without copula)
		if (predicate == null) {
			predicate = getPredicateForConjunction(governor);
		}
		
		if (predicate != null) {
			return predicate;
		} else {
			throw new NotSupportedException("Unable to identify a predicate (" + governor.word() + ")", sentenceAsString);
		}
	}
	
	private String getPredicateForConjunction(IndexedWord relationGovernor) {
//		Optional<SemanticGraphEdge> clausalSubjectRelationEdge = getRelations("csubj").stream().filter(e -> e.getDependent().equals(relationGovernor)).findFirst();
//		if (clausalSubjectRelationEdge.isPresent()) {
//			return relationGovernor.word() + " " + clausalSubjectRelationEdge.get().getGovernor().word();
//		}
			
		// Follow a possible conjunction 
		// Case 1: conjunction between verbs (e.g., set is OR contains null)
		Optional<SemanticGraphEdge> conjunctionEdge = conjunctionRelations.stream().filter(e -> e.getGovernor().equals(relationGovernor)).findFirst();
		if (conjunctionEdge.isPresent()) {
			Optional<SemanticGraphEdge> complementEdge = complementRelations.stream().filter(e -> e.getGovernor().equals(conjunctionEdge.get().getDependent())).findFirst();
			if (complementEdge.isPresent()) {
				return relationGovernor.word() + " " + complementEdge.get().getDependent().word();
			}
		}
		// Case 2: conjunction between complements when there is a copula (e.g., name is empty or null)
		Optional<SemanticGraphEdge> conjunctionEdge2 = conjunctionRelations.stream().filter(e -> e.getDependent().equals(relationGovernor)).findFirst();
		if (conjunctionEdge2.isPresent()) {
			Optional<SemanticGraphEdge> copulaEdge = copulaRelations.stream().filter(e -> e.getGovernor().equals(conjunctionEdge2.get().getGovernor())).findFirst();
			if (copulaEdge.isPresent()) {
				return copulaEdge.get().getDependent().word() + " " + conjunctionEdge2.get().getDependent().word();
			}
		}
		
		return null;
	}
	
	private String getPredicateForCopula(IndexedWord copulaRelationGovernor) {
		String predicate = null;
		
		Optional<SemanticGraphEdge> copulaEdge = copulaRelations.stream().filter(e -> e.getGovernor().equals(copulaRelationGovernor)).findFirst();
		if (copulaEdge.isPresent()) {
			String complement = copulaRelationGovernor.word();
			String verb = copulaEdge.get().getDependent().word();
			predicate = verb + " " + complement;
			
			// Check if there is an "element of compound number" to consider, it then becomes part of the predicate
			Optional<SemanticGraphEdge> edge = getRelations("number").stream().filter(e -> e.getGovernor().equals(copulaRelationGovernor)).findFirst();
			if (edge.isPresent()) {
				predicate = verb + " " + edge.get().getDependent().word()  + " " + complement;
			}
			
			// Check if there is a "negation modifier" to consider, it then becomes part of the predicate
			edge = getRelations("neg").stream().filter(e -> e.getGovernor().equals(copulaRelationGovernor)).findFirst();
			if (edge.isPresent()) {
				predicate = verb + " " + edge.get().getDependent().word()  + " " + complement;
			}
		}
		return predicate;
	}
	
	private String getPredicateForNonCopula(IndexedWord nonCopulaRelationGovernor) { 
		// Finding the complement
		Optional<SemanticGraphEdge> complementEdge = complementRelations.stream().filter(e -> e.getGovernor().equals(nonCopulaRelationGovernor)).findFirst();
		if (complementEdge.isPresent()) {
			return nonCopulaRelationGovernor.word() + " " + complementEdge.get().getDependent().word();
		}
		// TODO Finding the subject 
		// (e.g., map does not permit null keys. In this case nonCopulaRelationGovernor is permit)
		return null;
	}
	
	private String getPredicateForPassiveForm(IndexedWord passiveNominalSubjectGovernor) {
		String predicate = null;

		//TODO This implementation is very specific (for a case like x <= 0), rewrite to make it more general and reliable!
		Optional<SemanticGraphEdge> causalComplement = getRelations("ccomp").stream().filter(e -> e.getGovernor().equals(passiveNominalSubjectGovernor)).findFirst();
		if (causalComplement.isPresent()) {
			IndexedWord dep = causalComplement.get().getDependent(); // ex. x <= 0, dep is 0
			predicate = dep.word();
			
			Optional<SemanticGraphEdge> dependent = getRelations("dep").stream().filter(e -> e.getGovernor().equals(dep)).findFirst(); // ex. x <= 0, dependent is =
			if (dependent.isPresent()) {
				predicate = dependent.get().getDependent().word() + " " + predicate;
			}
			predicate = "is " + passiveNominalSubjectGovernor.word() + predicate;
		}
		
		// This should be general
		if (predicate == null) {
			Optional<SemanticGraphEdge> passiveAuxiliary = getRelations("auxpass").stream().filter(e -> e.getGovernor().equals(passiveNominalSubjectGovernor)).findFirst();
			if (passiveAuxiliary.isPresent()) {
				predicate = passiveAuxiliary.get().getDependent().word() + " " + passiveNominalSubjectGovernor.word();
			}
		}
		
		return predicate;
	}

	/**
	 * Collect information from the sentence (subjects, complements, copulas, ...)
	 * 
	 * @throws NotSupportedException if unable to identify subjects in the sentence
	 */
	private void init() throws NotSupportedException {
		this.semanticGraph = StanfordParser.getSemanticGraph(sentence);
		LOG.fine(semanticGraph.toString(OutputFormat.READABLE));
		subjectRelations = getRelations("nsubj", "nsubjpass");
		if (subjectRelations.isEmpty()) {
			throw new NotSupportedException("Unable to identify subjects.", sentenceAsString);
		}
		copulaRelations = getRelations("cop");
		complementRelations = getRelations("acomp", "xcomp");
		conjunctionRelations = getRelations("conj");
	}
	
	/**
	 * @param relationShortName
	 * @return all the grammatical relations in the sentence that match the specified relation (short) name
	 */
	private List<SemanticGraphEdge> getRelations(String... relationShortName) {
		List<String> relations = Arrays.asList(relationShortName);
		Stream<SemanticGraphEdge> stream = StreamSupport.stream(semanticGraph.edgeIterable().spliterator(), false);
		return stream.filter(e -> relations.contains(e.getRelation().getShortName())).collect(Collectors.toList());
	}
}
