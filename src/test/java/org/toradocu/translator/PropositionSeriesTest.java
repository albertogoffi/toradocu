package org.toradocu.translator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class PropositionSeriesTest {

  @Test
  public void toStringTest() {
    final IndexedWord subject1 = new IndexedWord();
    subject1.setWord("subject1");
    final IndexedWord predicate1 = new IndexedWord();
    predicate1.setWord("predicate1");
    final IndexedWord subject2 = new IndexedWord();
    subject2.setWord("subject2");
    final IndexedWord predicate2 = new IndexedWord();
    predicate2.setWord("predicate2");
    final IndexedWord subject3 = new IndexedWord();
    subject3.setWord("subject3");
    final IndexedWord predicate3 = new IndexedWord();
    predicate3.setWord("predicate3");

    List<IndexedWord> subject1Words = new ArrayList<>();
    subject1Words.add(subject1);
    List<IndexedWord> subject2Words = new ArrayList<>();
    subject2Words.add(subject2);
    List<IndexedWord> subject3Words = new ArrayList<>();
    subject3Words.add(subject3);

    PropositionSeries propositions = new PropositionSeries();
    propositions.add(new Proposition(new Subject(subject1Words), "predicate1"));
    propositions.add(Conjunction.OR, new Proposition(new Subject(subject2Words), "predicate2"));
    propositions.add(Conjunction.AND, new Proposition(new Subject(subject3Words), "predicate3"));

    assertThat(propositions.numberOfPropositions(), is(3));
    assertThat(
        propositions.toString(),
        is("(subject1, predicate1) || (subject2, predicate2) && (subject3, predicate3)"));
  }

  @Test
  public void testSingleProposition() {
    PropositionSeries propositions = getPropositions("if expectedKeys is negative");

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(propositions.getPropositions().get(0).toString(), is("(expectedKeys, is negative)"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testSinglePropositionWithNegation() {
    PropositionSeries propositions = getPropositions("if expectedKeys is not negative");

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(
        propositions.getPropositions().get(0).toString(), is("(expectedKeys, not(is negative))"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testSinglePropositionPassive() {
    PropositionSeries propositions = getPropositions("if expectedKeys was not set");

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(
        propositions.getPropositions().get(0).toString(), is("(expectedKeys, not(was set))"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testSinglePropositionCompoundName() {
    PropositionSeries propositions = getPropositions("if the JNDI name is null");

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(propositions.getPropositions().get(0).toString(), is("(JNDI name, is null)"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testExplicitDisjunction() {
    PropositionSeries propositions =
        getPropositions("if expectedKeys or expectedValuesPerKey is negative");

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(expectedKeys, is negative)"));
    assertThat(
        propositions.getPropositions().get(1).toString(),
        is("(expectedValuesPerKey, is negative)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.OR));
  }

  @Test
  public void testExplicitDisjunctionWithOneSubject() throws Exception {
    PropositionSeries propositions = getPropositions("if expectedKeys is null or is negative");

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(expectedKeys, is null)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(expectedKeys, is negative)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.OR));
  }

  @Test
  public void testDisjunctionBetweenComplements() throws Exception {
    PropositionSeries propositions = getPropositions("name is empty or null");

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(name, is empty)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(name, is null)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.OR));
  }

  @Test
  public void testDisjunctionBetweenVerbs() throws Exception {
    PropositionSeries propositions = getPropositions("name is or contains null");

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(name, is null)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(name, contains null)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.OR));
  }

  @Test
  public void testImplicitDisjunction() {
    PropositionSeries propositions = getPropositions("Joe eats apples, Bill eats oranges");

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(Joe, eats apples)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(Bill, eats oranges)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(
        propositions.getConjunctions().get(0),
        is(Conjunction.OR)); // If not specified we assume OR as conjunction
  }

  @Test
  public void testExplicitConjunction() {
    PropositionSeries propositions =
        getPropositions("if expectedKeys and expectedValuesPerKey are negative");

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(
        propositions.getPropositions().get(0).toString(), is("(expectedKeys, are negative)"));
    assertThat(
        propositions.getPropositions().get(1).toString(),
        is("(expectedValuesPerKey, are negative)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.AND));
  }

  @Test
  public void testMultipleConjunction() {
    PropositionSeries propositions =
        getPropositions("bar is negative, and foo is null, and baz is empty");

    assertThat(propositions.numberOfPropositions(), is(3));
    assertThat(propositions.getPropositions().get(0).toString(), is("(bar, is negative)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(foo, is null)"));
    assertThat(propositions.getPropositions().get(2).toString(), is("(baz, is empty)"));

    assertThat(propositions.getConjunctions().size(), is(2));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.AND));
    assertThat(propositions.getConjunctions().get(1), is(Conjunction.AND));
  }

  @Test
  public void testMultiplePropositions() {
    PropositionSeries propositions =
        getPropositions(
            "the outputCollection is null and both inputCollection and transformer are not null.");

    assertThat(propositions.numberOfPropositions(), is(3));
    assertThat(propositions.getPropositions().get(0).toString(), is("(outputCollection, is null)"));
    assertThat(
        propositions.getPropositions().get(1).toString(), is("(inputCollection, not(are null))"));
    assertThat(
        propositions.getPropositions().get(2).toString(), is("(transformer, not(are null))"));

    assertThat(propositions.getConjunctions().size(), is(2));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.AND));
    assertThat(propositions.getConjunctions().get(1), is(Conjunction.AND));
  }

  private PropositionSeries getPropositions(String sentence) {
    List<SemanticGraph> semanticGraphs = StanfordParser.getSemanticGraphs(sentence);
    assertThat(semanticGraphs.size(), is(1));
    SentenceParser parser = new SentenceParser(semanticGraphs.get(0));
    return parser.getPropositionSeries();
  }
}
