package org.toradocu.translator;

import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleDirectedGraph;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class PropositionExtractor {
	
	private static final Logger LOG = Logger.getLogger(PropositionExtractor.class.getName());
	
	public static Graph<Proposition,ConjunctionEdge<Proposition>> getPropositionGraph(String comment) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		SimpleDirectedGraph<Proposition, ConjunctionEdge<Proposition>> graph = new SimpleDirectedGraph(ConjunctionEdge.class);
				
		// 0. Preprocess text -> where should we do this? Is it still necessary?
		// TODO!
		// 1. We extract each sentence in comment and for each sentence
		for (List<HasWord> sentence : getSentences(comment)) {
			try {
				// 2. We identify propositions
				Graph<Proposition, ConjunctionEdge<Proposition>> newGraph = new SentenceParser(sentence).getPropositionGraph();
				Graphs.addGraph(graph, newGraph);
			} catch (NotSupportedException e) {
				LOG.log(Level.INFO, e.getMessage());
			}
		}
		return graph;
	}
	
	private static Iterable<List<HasWord>> getSentences(String text) {
		return new DocumentPreprocessor(new StringReader(text));
	}
}
