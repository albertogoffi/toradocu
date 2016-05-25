package org.toradocu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.jgrapht.Graph;
import org.junit.Test;
import org.toradocu.translator.ConjunctionEdge;
import org.toradocu.translator.Proposition;
import org.toradocu.translator.PropositionExtractor;

public class PropositionExtractorTest {
	
	@Test
	public void simplePropositions() throws Exception {
		Graph<Proposition, ConjunctionEdge<Proposition>> graph = PropositionExtractor.getPropositionGraph("the array is empty, the size is zero");
		assertEquals(0, graph.edgeSet().size());
		
		Set<Proposition> propositions = graph.vertexSet();
		assertEquals(2, propositions.size());
		assertTrue(propositions.contains(new Proposition("array", "is empty")));
		assertTrue(propositions.contains(new Proposition("size", "is zero")));
	}
	
	@Test
	public void multiplePropositions() throws Exception {
		Graph<Proposition, ConjunctionEdge<Proposition>> graph = PropositionExtractor.getPropositionGraph("The array is empty. The size is zero");
		assertEquals(0, graph.edgeSet().size());
		
		Set<Proposition> propositions = graph.vertexSet();
		assertEquals(2, propositions.size());
		assertTrue(propositions.contains(new Proposition("The array", "is empty"))); // TODO: check why here "the" is included, while is not the case in simpleProposition test
		assertTrue(propositions.contains(new Proposition("The size", "is zero")));
	}
}
