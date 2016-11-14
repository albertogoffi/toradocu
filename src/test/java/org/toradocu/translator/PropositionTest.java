package org.toradocu.translator;

import static org.junit.Assert.*;

import org.junit.Test;

public class PropositionTest {

  @Test
  public void testEqualsAndHashCode() {
    Proposition p1 = new Proposition("array", "is empty");
    Proposition p2 = new Proposition("array", "is empty");
    assertTrue(p1.equals(p2));
    assertEquals(p1.hashCode(), p2.hashCode());
  }
}
