package org.toradocu.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraph.OutputFormat;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

/**
 * {@code SentenceParser} parses a {@code PropositionSeries} from a sentence (i.e. a {@code List<HasWord>}),
 * using NLP to identify subjects, relations, conjunctions, and so on from the sentence.
 */
public class SentenceParser {

	private SemanticGraph semanticGraph;
	private List<SemanticGraphEdge> subjectRelations, copulaRelations, complementRelations, conjunctionRelations;
	private static final Logger LOG = LoggerFactory.getLogger(SentenceParser.class);

	/**
	 * Constructs a {@code SentenceParser} object that will parse a {@code PropositionSeries}
	 * from the given {@code SemanticGraph}.
	 * 
	 * @param semanticGraph the {@code SemanticGraph} that will be used to create the {@code PropositionSeries}
	 */
	public SentenceParser(SemanticGraph semanticGraph) {
		this.semanticGraph = semanticGraph;
	}
	
	/**
	 * Parses and returns a {@code PropositionSeries} for the sentence that this parser is initialized to parse.
	 * 
	 * @return a {@code PropositionSeries} for the sentence that this parser is initialized to parse
	 */
	public PropositionSeries getPropositionSeries() {
		Map<List<IndexedWord>, Proposition> propositionMap = new HashMap<>();
		PropositionSeries propositionSeries = new PropositionSeries();
		
		initializeRelations();
		
		// For each subject in the sentence we try to create a proposition
		for (SemanticGraphEdge subjectRelation : subjectRelations) {
			IndexedWord subjectAsIndexedWord = subjectRelation.getDependent();
			List<IndexedWord> subjectWords = getSubject(subjectAsIndexedWord);
			String subject = subjectWords.stream().map(word -> word.word()).collect(Collectors.joining(" "));
			
			List<IndexedWord> predicateWords = getPredicate(subjectRelation.getGovernor());
			String relation = predicateWords.stream().map(word -> word.word()).collect(Collectors.joining(" "));
			Proposition proposition = new Proposition(subject, relation);
			
			ArrayList<IndexedWord> propositionWords = new ArrayList<>(subjectWords);
			propositionWords.addAll(predicateWords);
			propositionMap.put(propositionWords, proposition);
		}
		
		// For each conjunction in the sentence we create a edge connecting different nodes representing propositions
		for (SemanticGraphEdge conjunctionRelation : conjunctionRelations) {
			IndexedWord conjGovernor = conjunctionRelation.getGovernor();
			IndexedWord conjDependent = conjunctionRelation.getDependent();
			Proposition p1 = null, p2 = null;
			
			for (Entry<List<IndexedWord>, Proposition> entry : propositionMap.entrySet()) {
				if (entry.getKey().contains(conjGovernor)) {
					p1 = entry.getValue();
				}
				if (entry.getKey().contains(conjDependent)) {
					p2 = entry.getValue();
				}
			}
			
			if (p1 != null && p2 != null) {
				if (propositionSeries.isEmpty()) {
					propositionSeries.add(p1);
				}
				propositionSeries.add(getConjunction(conjunctionRelation), p2);
			}
		}
		
		// Add to graph propositions not part of a conjunction relation
		for (Proposition p : propositionMap.values()) {
			if (!propositionSeries.contains(p)) {
				if (propositionSeries.isEmpty()) {
					propositionSeries.add(p);
				} else {
					propositionSeries.add(Conjunction.OR, p); // We assume OR as conjunction when is not specified
				}
			}
		}
		
		return propositionSeries;
	}
	
	/**
	 * Initializes the relations fields using the semantic graph.
	 */
	private void initializeRelations() {
		subjectRelations = getRelationsFromGraph("nsubj", "nsubjpass");
		if (subjectRelations.isEmpty()) {
			LOG.warn("Unable to identify subjects in \"{}\".", semanticGraph.toRecoveredSentenceString());
		}
		copulaRelations = getRelationsFromGraph("cop");
		complementRelations = getRelationsFromGraph("acomp", "xcomp", "dobj");
		conjunctionRelations = getRelationsFromGraph("conj");
	}
	
	/**
	 * Retrieves relations with the given names from the semantic graph.
	 * 
	 * @param relationShortNames the abbreviated names of the relations to retrieve
	 * @return all the grammatical relations in the sentence (as graph edges) that match the specified relation names
	 */
	private List<SemanticGraphEdge> getRelationsFromGraph(String... relationShortNames) {
		List<String> relations = Arrays.asList(relationShortNames);
		Stream<SemanticGraphEdge> stream = StreamSupport.stream(semanticGraph.edgeIterable().spliterator(), false);
		return stream.filter(e -> relations.contains(e.getRelation().getShortName())).collect(Collectors.toList());
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
	
	private List<IndexedWord> getSubject(IndexedWord subject) {
		List<IndexedWord> words = new ArrayList<>();
		List<String> STOPWORDS = Arrays.asList("a", "an", "the");
		
		// When the subject is a compound name (e.g., JNDI name) we have to complete the subject
		Optional<SemanticGraphEdge> compoundEdge = getRelationsFromGraph("compound", "det", "advmod").stream()
				.filter(e -> e.getGovernor().equals(subject))
				.filter(e -> !STOPWORDS.contains(e.getDependent().word()))
				.findFirst();
		if (compoundEdge.isPresent()) {
			words.add(compoundEdge.get().getDependent());
		}
		
		words.add(subject);
		return words;
	}

	private List<IndexedWord> getPredicate(IndexedWord governor) {
		// Copula handling. In the case of copula, gov is also the governor of the copula relation
		List<IndexedWord> predicate = getPredicateForCopula(governor);
		
		// Passive form handling
		if (predicate.isEmpty()) {
			predicate = getPredicateForPassiveForm(governor);
		}
		
		// Non-copula handling
		if (predicate.isEmpty()) {
			predicate = getPredicateForNonCopula(governor);
		}
		
		// Conjunction handling (with or without copula)
		if (predicate.isEmpty()) {
			predicate = getPredicateForConjunction(governor);
		}
		
		if (predicate.isEmpty()) {
			LOG.warn("Unable to identify a predicate (governor = " + governor.word() + ")", semanticGraph.toRecoveredSentenceString());
		}
		
		return predicate;
	}
	
	private List<IndexedWord> getPredicateForConjunction(IndexedWord relationGovernor) {
//		Optional<SemanticGraphEdge> clausalSubjectRelationEdge = getRelations("csubj").stream().filter(e -> e.getDependent().equals(relationGovernor)).findFirst();
//		if (clausalSubjectRelationEdge.isPresent()) {
//			return relationGovernor.word() + " " + clausalSubjectRelationEdge.get().getGovernor().word();
//		}
		
		List<IndexedWord> predicate = new ArrayList<>();
			
		// Follow a possible conjunction 
		// Case 1: conjunction between verbs (e.g., set is OR contains null)
		Optional<SemanticGraphEdge> conjunctionEdge = conjunctionRelations.stream().filter(e -> e.getGovernor().equals(relationGovernor)).findFirst();
		if (conjunctionEdge.isPresent()) {
			Optional<SemanticGraphEdge> complementEdge = complementRelations.stream().filter(e -> e.getGovernor().equals(conjunctionEdge.get().getDependent())).findFirst();
			if (complementEdge.isPresent()) {
				predicate.add(relationGovernor);
				predicate.add(complementEdge.get().getDependent());
				return predicate;
			}
		}
		// Case 2: conjunction between complements when there is a copula (e.g., name is empty or null)
		Optional<SemanticGraphEdge> conjunctionEdge2 = conjunctionRelations.stream().filter(e -> e.getDependent().equals(relationGovernor)).findFirst();
		if (conjunctionEdge2.isPresent()) {
			Optional<SemanticGraphEdge> copulaEdge = copulaRelations.stream().filter(e -> e.getGovernor().equals(conjunctionEdge2.get().getGovernor())).findFirst();
			if (copulaEdge.isPresent()) {
				predicate.add(copulaEdge.get().getDependent());
				predicate.add(conjunctionEdge2.get().getDependent());
				return predicate;
			}
		}
		
		return predicate;
	}
	
	private List<IndexedWord> getPredicateForCopula(IndexedWord copulaRelationGovernor) {
		// copulaRelationGovernor is a predicate
		
		List<IndexedWord> predicate = new ArrayList<>();
		
		Optional<SemanticGraphEdge> copulaEdge = copulaRelations.stream().filter(e -> e.getGovernor().equals(copulaRelationGovernor)).findFirst();
		if (copulaEdge.isPresent()) {	
			IndexedWord verb = copulaEdge.get().getDependent();
			predicate.add(verb);
			
			// Check if there is an "element of compound number" to consider, it then becomes part of the predicate
			Optional<SemanticGraphEdge> edge = getRelationsFromGraph("number").stream().filter(e -> e.getGovernor().equals(copulaRelationGovernor)).findFirst();
			if (edge.isPresent()) {
				predicate.add(edge.get().getDependent());
			}
			
			// Check if there is a "negation modifier" to consider, it then becomes part of the predicate
			edge = getRelationsFromGraph("neg").stream().filter(e -> e.getGovernor().equals(copulaRelationGovernor)).findFirst();
			if (edge.isPresent()) {
				predicate.add(edge.get().getDependent());
			}
			
			predicate.add(copulaRelationGovernor); 
		}
		return predicate;
	}
	
	private List<IndexedWord> getPredicateForNonCopula(IndexedWord nonCopulaRelationGovernor) { 
		List<IndexedWord> predicate = new ArrayList<>();
		
		// Finding the complement
		Optional<SemanticGraphEdge> complementEdge = complementRelations.stream().filter(e -> e.getGovernor().equals(nonCopulaRelationGovernor)).findFirst();
		if (complementEdge.isPresent()) {
			predicate.add(nonCopulaRelationGovernor);
			predicate.add(complementEdge.get().getDependent());
		}
		// TODO Finding the subject 
		// (e.g., map does not permit null keys. In this case nonCopulaRelationGovernor is permit)
		
		return predicate;
	}
	
	private List<IndexedWord> getPredicateForPassiveForm(IndexedWord passiveNominalSubjectGovernor) {
		List<IndexedWord> predicate = new ArrayList<>();

//		//TODO This implementation is very specific (for a case like x <= 0), rewrite to make it more general and reliable!
//		Optional<SemanticGraphEdge> causalComplement = getRelations("ccomp").stream().filter(e -> e.getGovernor().equals(passiveNominalSubjectGovernor)).findFirst();
//		if (causalComplement.isPresent()) {
//			IndexedWord dep = causalComplement.get().getDependent(); // ex. x <= 0, dep is 0
//			predicate.add(dep);
//			
//			Optional<SemanticGraphEdge> dependent = getRelations("dep").stream().filter(e -> e.getGovernor().equals(dep)).findFirst(); // ex. x <= 0, dependent is =
//			if (dependent.isPresent()) {
//				predicate = dependent.get().getDependent().word() + " " + predicate;
//			}
//			predicate = "is " + passiveNominalSubjectGovernor.word() + predicate;
//			predicate.add(new IndexedWord(new L))
//		}
	
		// This should be general
		Optional<SemanticGraphEdge> passiveAuxiliaryOpt = getRelationsFromGraph("auxpass").stream().filter(e -> e.getGovernor().equals(passiveNominalSubjectGovernor)).findFirst();
		if (passiveAuxiliaryOpt.isPresent()) {
			SemanticGraphEdge passiveAux = passiveAuxiliaryOpt.get();
			predicate.add(passiveAux.getDependent());
			
			// Check if there is a "negation modifier" to consider, then it becomes part of the predicate
			Optional<SemanticGraphEdge> edge = getRelationsFromGraph("neg").stream().filter(e -> e.getGovernor().equals(passiveAux.getGovernor())).findFirst();
			if (edge.isPresent()) { predicate.add(edge.get().getDependent()); }
			
			predicate.add(passiveNominalSubjectGovernor);
		}
		
		return predicate;
	}

}
