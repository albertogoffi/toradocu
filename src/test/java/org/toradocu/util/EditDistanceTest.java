package org.toradocu.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EditDistanceTest {

  @Test
  public void distanceWithWordDeletionCost0Test() throws Exception {
    final int WORD_DELETION_COST = 0;

    int distance = Distance.editDistance("x", "x", WORD_DELETION_COST);
    assertThat(distance, is(0));

    distance = Distance.editDistance("x", "xyz", WORD_DELETION_COST);
    assertThat(distance, is(2));

    distance = Distance.editDistance("x", "specified x", WORD_DELETION_COST);
    assertThat(distance, is(0));

    distance = Distance.editDistance("x", "specified xy", WORD_DELETION_COST);
    assertThat(distance, is(1));

    distance = Distance.editDistance("specified", "specified xy", WORD_DELETION_COST);
    assertThat(distance, is(0));

    distance = Distance.editDistance("map", "the specified myMap", WORD_DELETION_COST);
    assertThat(distance, is(2));
  }

  @Test
  public void distanceWithWordDeletionCost1Test() throws Exception {
    final int WORD_DELETION_COST = 1;

    int distance = Distance.editDistance("x", "x", WORD_DELETION_COST);
    assertThat(distance, is(0));

    distance = Distance.editDistance("x", "xyz", WORD_DELETION_COST);
    assertThat(distance, is(2));

    distance = Distance.editDistance("x", "specified x", WORD_DELETION_COST);
    assertThat(distance, is(1));

    distance = Distance.editDistance("x", "specified xy", WORD_DELETION_COST);
    assertThat(distance, is(2));

    distance = Distance.editDistance("specified", "specified xy", WORD_DELETION_COST);
    assertThat(distance, is(1));

    distance = Distance.editDistance("x", "the specified xy", WORD_DELETION_COST);
    assertThat(distance, is(3));

    distance = Distance.editDistance("n", "relativenth", WORD_DELETION_COST);
    assertThat(distance, is(10));

    distance = Distance.editDistance("relativenth", "n", WORD_DELETION_COST);
    assertThat(distance, is(10));
  }

  @Test
  public void distanceWithWordDeletionCost2Test() throws Exception {
    final int WORD_DELETION_COST = 2;

    int distance = Distance.editDistance("x", "x", WORD_DELETION_COST);
    assertThat(distance, is(0));

    distance = Distance.editDistance("x", "xyz", WORD_DELETION_COST);
    assertThat(distance, is(2));

    distance = Distance.editDistance("x", "specified x", WORD_DELETION_COST);
    assertThat(distance, is(2));

    distance = Distance.editDistance("x", "specified xy", WORD_DELETION_COST);
    assertThat(distance, is(3));

    distance = Distance.editDistance("map", "the specified myMap", WORD_DELETION_COST);
    assertThat(distance, is(6));
  }
}
