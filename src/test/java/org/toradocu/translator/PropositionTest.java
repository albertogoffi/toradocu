package org.toradocu.translator;

import static org.junit.Assert.*;

import edu.stanford.nlp.ling.IndexedWord;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class PropositionTest {

  @Test
  public void testEqualsAndHashCode() {
    final IndexedWord subject1 = new IndexedWord();
    subject1.setWord("array");
    final IndexedWord subject2 = new IndexedWord();
    subject2.setWord("array");
    List<IndexedWord> subject1Words = new ArrayList<>();
    subject1Words.add(subject1);
    List<IndexedWord> subject2Words = new ArrayList<>();
    subject2Words.add(subject2);

    Proposition p1 = new Proposition(new Subject(subject1Words, false), "is empty");
    Proposition p2 = new Proposition(new Subject(subject2Words, false), "is empty");
    assertTrue(p1.equals(p2));
    assertEquals(p1.hashCode(), p2.hashCode());
  }
}
