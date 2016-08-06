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

public class SentenceParser {

	private final List<HasWord> sentence;
	private String sentenceAsString;
	private List<SemanticGraphEdge> subjectRelations, copulaRelations, complementRelations, conjunctionRelations;
	private SemanticGraph semanticGraph;
	private static final Logger LOG = LoggerFactory.getLogger(SentenceParser.class);

	public SentenceParser(List<HasWord> sentence) {
		this.sentence = sentence;
		this.sentenceAsString = sentence.stream().map(Object::toString).collect(Collectors.joining(" "));
	}
	
	/**
	 * Identify propositions in a given sentence. Identified propositions are then returned as a single
	 * <code>PropositionList</code> code.
	 * 
	 * @return a proposition list containing all the identified propositions joint with conjunctions.
	 */
	public PropositionSeries getPropositionSeries() {
		Map<List<IndexedWord>, Proposition> propositionMap = new HashMap<>();
		PropositionSeries propositionList = new PropositionSeries();
		
		init(); // Initialization phase where we extract information from the sentence
		
		// For each subject in the sentence we try to create a proposition
		for (SemanticGraphEdge subjectRelation : subjectRelations) {
			IndexedWord subjectAsIndexWord = subjectRelation.getDependent();
			List<IndexedWord> subjectWords = getSubject(subjectAsIndexWord);
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
				if (propositionList.isEmpty()) {
					propositionList.add(p1);
				}
				propositionList.add(getConjunction(conjunctionRelation), p2);
			}
		}
		
		// Add to graph propositions not part of a conjunction relation
		for (Proposition p : propositionMap.values()) {
			if (!propositionList.contains(p)) {
				if (propositionList.isEmpty()) {
					propositionList.add(p);
				} else {
					propositionList.add(Conjunction.OR, p); // We assume OR as conjunction when is not specified
				}
			}
		}
		
		return propositionList;
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
		Optional<SemanticGraphEdge> compoundEdge = getRelations("compound", "det", "advmod").stream()
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
			LOG.warn("Unable to identify a predicate (governor = " + governor.word() + ")", sentenceAsString);
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
			Optional<SemanticGraphEdge> edge = getRelations("number").stream().filter(e -> e.getGovernor().equals(copulaRelationGovernor)).findFirst();
			if (edge.isPresent()) {
				predicate.add(edge.get().getDependent());
			}
			
			// Check if there is a "negation modifier" to consider, it then becomes part of the predicate
			edge = getRelations("neg").stream().filter(e -> e.getGovernor().equals(copulaRelationGovernor)).findFirst();
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
		Optional<SemanticGraphEdge> passiveAuxiliaryOpt = getRelations("auxpass").stream().filter(e -> e.getGovernor().equals(passiveNominalSubjectGovernor)).findFirst();
		if (passiveAuxiliaryOpt.isPresent()) {
			SemanticGraphEdge passiveAux = passiveAuxiliaryOpt.get();
			predicate.add(passiveAux.getDependent());
			
			// Check if there is a "negation modifier" to consider, then it becomes part of the predicate
			Optional<SemanticGraphEdge> edge = getRelations("neg").stream().filter(e -> e.getGovernor().equals(passiveAux.getGovernor())).findFirst();
			if (edge.isPresent()) { predicate.add(edge.get().getDependent()); }
			
			predicate.add(passiveNominalSubjectGovernor);
		}
		
		return predicate;
	}

	/**
	 * Collect information from the sentence (subjects, complements, copulas, ...)
	 * 
	 * @throws NotSupportedException if unable to identify subjects in the sentence
	 */
	private void init() {
		this.semanticGraph = StanfordParser.getSemanticGraph(sentence);
		LOG.trace(semanticGraph.toString(OutputFormat.READABLE));
		subjectRelations = getRelations("nsubj", "nsubjpass");
		if (subjectRelations.isEmpty()) {
			LOG.warn("Unable to identify subjects.", sentenceAsString);
		}
		copulaRelations = getRelations("cop");
		complementRelations = getRelations("acomp", "xcomp", "dobj");
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
