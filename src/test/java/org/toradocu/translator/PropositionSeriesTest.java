package org.toradocu.translator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import edu.stanford.nlp.ling.IndexedWord;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.DocumentedExecutable;

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

    PropositionSeries propositions = new PropositionSeries(null);
    propositions.add(new Proposition(new Subject(subject1Words, false), "predicate1"));
    propositions.add(
        Conjunction.OR, new Proposition(new Subject(subject2Words, false), "predicate2"));
    propositions.add(
        Conjunction.AND, new Proposition(new Subject(subject3Words, false), "predicate3"));

    assertThat(propositions.numberOfPropositions(), is(3));
    assertThat(
        propositions.toString(),
        is("(subject1, predicate1) || (subject2, predicate2) && (subject3, predicate3)"));
  }

  @Test
  public void testSingleProposition() {
    PropositionSeries propositions =
        getPropositions(new Comment("if expectedKeys is negative"), null);

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(propositions.getPropositions().get(0).toString(), is("(expectedKeys, is negative)"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testSinglePropositionWithNegation() {
    PropositionSeries propositions =
        getPropositions(new Comment("if expectedKeys is not negative"), null);

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(
        propositions.getPropositions().get(0).toString(), is("(expectedKeys, not(is negative))"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testSinglePropositionPassive() {
    PropositionSeries propositions =
        getPropositions(new Comment("if expectedKeys was not set"), null);

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(
        propositions.getPropositions().get(0).toString(), is("(expectedKeys, not(was set))"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testSinglePropositionCompoundName() {
    PropositionSeries propositions = getPropositions(new Comment("if the JNDI name is null"), null);

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(propositions.getPropositions().get(0).toString(), is("(JNDI name, is null)"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testExplicitDisjunction() {
    PropositionSeries propositions =
        getPropositions(new Comment("if expectedKeys or expectedValuesPerKey is negative"), null);
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
    PropositionSeries propositions =
        getPropositions(new Comment("if expectedKeys is null or is negative"), null);

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(expectedKeys, is null)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(expectedKeys, is negative)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.OR));
  }

  @Test
  public void testDisjunctionBetweenComplements() throws Exception {
    PropositionSeries propositions = getPropositions(new Comment("name is empty or null"), null);

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(name, is empty)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(name, is null)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.OR));
  }

  @Test
  public void testDisjunctionBetweenVerbs() throws Exception {
    PropositionSeries propositions = getPropositions(new Comment("name is or contains null"), null);

    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(name, is null)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(name, contains null)"));

    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.OR));
  }

  @Test
  public void testImplicitDisjunction() {
    PropositionSeries propositions =
        getPropositions(new Comment("Joe eats apples, Bill eats oranges"), null);

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
        getPropositions(new Comment("if expectedKeys and expectedValuesPerKey are negative"), null);

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
        getPropositions(new Comment("bar is negative, and foo is null, and baz is empty"), null);

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
            new Comment(
                "the outputCollection is null and both inputCollection and transformer are not null."),
            null);

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

  @Test
  public void testIssue90() {
    // https://github.com/albertogoffi/toradocu/issues/90
    PropositionSeries propositions =
        getPropositions(new Comment("Any of the specified vertices is null."), null);

    assertThat(propositions.numberOfPropositions(), is(1));
    assertThat(
        propositions.getPropositions().get(0).toString(), is("(Any specified vertices, is null)"));
    assertTrue(propositions.getConjunctions().isEmpty());
  }

  @Test
  public void testIssue97() {
    // https://github.com/albertogoffi/toradocu/issues/97
    PropositionSeries propositions =
        getPropositions(new Comment("shape is <= 0 or scale is <= 0."), null);
    assertThat(propositions.numberOfPropositions(), is(2));
    assertThat(propositions.getPropositions().get(0).toString(), is("(shape, is <= 0)"));
    assertThat(propositions.getPropositions().get(1).toString(), is("(scale, is <= 0)"));
    assertThat(propositions.getConjunctions().size(), is(1));
    assertThat(propositions.getConjunctions().get(0), is(Conjunction.OR));
  }

  private PropositionSeries getPropositions(Comment sentence, DocumentedExecutable member) {

    List<PropositionSeries> propositions = Parser.parse(sentence, member);
    // assertThat(semanticGraphs.size(), is(1));
    // SentenceParser parser = new SentenceParser(semanticGraphs.get(0));
    // return parser.getPropositionSeries();

    return propositions.get(0);
  }
}
