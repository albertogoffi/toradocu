package org.toradocu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.jgrapht.Graph;
import org.junit.Test;
import org.toradocu.nlp.ConjunctionEdge;
import org.toradocu.nlp.Proposition;
import org.toradocu.nlp.PropositionExtractor;

public class PropositionExtractorTest {
	
	@Test
	public void simplePropositions() throws Exception {
		Graph<Proposition, ConjunctionEdge<Proposition>> graph = PropositionExtractor.getPropositionGraph("the array is empty, the size is zero");
		assertEquals(0, graph.edgeSet().size());
		
		Set<Proposition> propositions = graph.vertexSet();
		assertEquals(2, propositions.size());
		final Iterator<Proposition> iterator = propositions.iterator();
		assertEquals(new Proposition("array", "is empty"), iterator.next());
		assertEquals(new Proposition("size", "is zero"), iterator.next());
	}
	
	@Test
	public void multiplePropositions() throws Exception {
		Graph<Proposition, ConjunctionEdge<Proposition>> graph = PropositionExtractor.getPropositionGraph("The array is empty. The size is zero");
		assertEquals(0, graph.edgeSet().size());

		Set<Proposition> propositions = graph.vertexSet();
		assertEquals(2, propositions.size());
		final Iterator<Proposition> iterator = propositions.iterator();
		assertEquals(new Proposition("The array", "is empty"), iterator.next());
		assertEquals(new Proposition("The size", "is zero"), iterator.next());
	}
}
