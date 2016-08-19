package org.toradocu.translator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import edu.stanford.nlp.semgraph.SemanticGraph;

public class SentenceParserTest {

	@Test
	public void testSingleProposition() {
		PropositionSeries list = getPropositions("if expectedKeys is negative");
		
		assertThat(list.numberOfPropositions(), is(1));
		assertThat(list.getPropositions().get(0).toString(), is("(expectedKeys, is negative)"));
		assertTrue(list.getConjunctions().isEmpty());
	}
	
	@Test
	public void testSinglePropositionWithNegation() {
		PropositionSeries list = getPropositions("if expectedKeys is not negative");
		
		assertThat(list.numberOfPropositions(), is(1));
		assertThat(list.getPropositions().get(0).toString(), is("(expectedKeys, is not negative)"));
		assertTrue(list.getConjunctions().isEmpty());
	}
	
	@Test
	public void testSinglePropositionPassive() {
		PropositionSeries list = getPropositions("if expectedKeys was not set");
		
		assertThat(list.numberOfPropositions(), is(1));
		assertThat(list.getPropositions().get(0).toString(), is("(expectedKeys, was not set)"));
		assertTrue(list.getConjunctions().isEmpty());
	}
	
	@Test
	public void testSinglePropositionCompoundName() {
		PropositionSeries list = getPropositions("if the JNDI name is null");
		
		assertThat(list.numberOfPropositions(), is(1));
		assertThat(list.getPropositions().get(0).toString(), is("(JNDI name, is null)"));
		assertTrue(list.getConjunctions().isEmpty());
	}
	
	@Test
	public void testExplicitDisjunction() {
		PropositionSeries list = getPropositions("if expectedKeys or expectedValuesPerKey is negative");
		
		assertThat(list.numberOfPropositions(), is(2));
		assertThat(list.getPropositions().get(0).toString(), is("(expectedKeys, is negative)")); 
		assertThat(list.getPropositions().get(1).toString(), is("(expectedValuesPerKey, is negative)"));
	
		assertThat(list.getConjunctions().size(), is(1));
		assertThat(list.getConjunctions().get(0), is(Conjunction.OR));
	}
	
	@Test
	public void testExplicitDisjunctionWithOneSubject() throws Exception {
		PropositionSeries list = getPropositions("if expectedKeys is null or is negative");
		
		assertThat(list.numberOfPropositions(), is(2));
		assertThat(list.getPropositions().get(0).toString(), is("(expectedKeys, is null)"));
		assertThat(list.getPropositions().get(1).toString(), is("(expectedKeys, is negative)"));
		
		assertThat(list.getConjunctions().size(), is(1));
		assertThat(list.getConjunctions().get(0), is(Conjunction.OR));
	}
	
	@Test
	public void testDisjunctionBetweenComplements() throws Exception {
		PropositionSeries list = getPropositions("name is empty or null");
		
		assertThat(list.numberOfPropositions(), is(2));
		assertThat(list.getPropositions().get(0).toString(), is("(name, is empty)"));
		assertThat(list.getPropositions().get(1).toString(), is("(name, is null)"));
		
		assertThat(list.getConjunctions().size(), is(1));
		assertThat(list.getConjunctions().get(0), is(Conjunction.OR));
	}
	
	@Test
	public void testDisjunctionBetweenVerbs() throws Exception {
		PropositionSeries list = getPropositions("name is or contains null");
		
		assertThat(list.numberOfPropositions(), is(2));
		assertThat(list.getPropositions().get(0).toString(), is("(name, is null)"));
		assertThat(list.getPropositions().get(1).toString(), is("(name, contains null)"));
		
		assertThat(list.getConjunctions().size(), is(1));
		assertThat(list.getConjunctions().get(0), is(Conjunction.OR));
	}

	@Test
	public void testImplicitDisjunction() {
		PropositionSeries list = getPropositions("Joe eats apples, Bill eats oranges");
		
		assertThat(list.numberOfPropositions(), is(2));
		assertThat(list.getPropositions().get(0).toString(), is("(Joe, eats apples)")); 
		assertThat(list.getPropositions().get(1).toString(), is("(Bill, eats oranges)"));
		
		assertThat(list.getConjunctions().size(), is(1));
		assertThat(list.getConjunctions().get(0), is(Conjunction.OR)); // If not specified we assume OR as conjunction
	}	

	@Test
	public void testExplicitConjunction() {
		PropositionSeries list = getPropositions("if expectedKeys and expectedValuesPerKey are negative");
		
		assertThat(list.numberOfPropositions(), is(2));
		assertThat(list.getPropositions().get(0).toString(), is("(expectedKeys, are negative)"));
		assertThat(list.getPropositions().get(1).toString(), is("(expectedValuesPerKey, are negative)"));
		
		assertThat(list.getConjunctions().size(), is(1));
		assertThat(list.getConjunctions().get(0), is(Conjunction.AND));
	}
	
	@Test
	public void testMultipleConjunction() {
		PropositionSeries list = getPropositions("bar is negative, and foo is null, and baz is empty");
		
		assertThat(list.numberOfPropositions(), is(3));
		assertThat(list.getPropositions().get(0).toString(), is("(bar, is negative)"));
		assertThat(list.getPropositions().get(1).toString(), is("(foo, is null)"));
		assertThat(list.getPropositions().get(2).toString(), is("(baz, is empty)"));
		
		assertThat(list.getConjunctions().size(), is(2));
		assertThat(list.getConjunctions().get(0), is(Conjunction.AND));
		assertThat(list.getConjunctions().get(1), is(Conjunction.AND));
	}
	
	private PropositionSeries getPropositions(String sentence) {
		List<SemanticGraph> semanticGraphs = StanfordParser.getSemanticGraphs(sentence);		
		assertThat(semanticGraphs.size(), is(1));
		SentenceParser parser = new SentenceParser(semanticGraphs.get(0));
		return parser.getPropositionSeries();
	}
	
	@Test
	public void scratch() {
		List<SemanticGraph> semanticGraphs = StanfordParser.getSemanticGraphs("any element in array is null.");
		System.out.println(semanticGraphs.get(0).toString());
		
		semanticGraphs = StanfordParser.getSemanticGraphs("the set is empty or null.");
		System.out.println(semanticGraphs.get(0).toString());
		
		semanticGraphs = StanfordParser.getSemanticGraphs("the set is empty.");
		System.out.println(semanticGraphs.get(0).toString());
		
		semanticGraphs = StanfordParser.getSemanticGraphs("the set or the list is empty.");
		System.out.println(semanticGraphs.get(0).toString());
	}
}
