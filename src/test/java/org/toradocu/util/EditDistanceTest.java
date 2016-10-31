package org.toradocu.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EditDistanceTest {

  @Test
  public void distanceWithWordDeletionCost0Test() throws Exception {
    final int WORD_DELETION_COST = 0;

    int distance = Distance.editDistance(WORD_DELETION_COST, "x", "x");
    assertThat(distance, is(0));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "xyz");
    assertThat(distance, is(1));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "specified x");
    assertThat(distance, is(0));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "specified xy");
    assertThat(distance, is(1));

    distance = Distance.editDistance(WORD_DELETION_COST, "specified", "specified xy");
    assertThat(distance, is(0));

    distance = Distance.editDistance(WORD_DELETION_COST, "map", "the specified myMap");
    assertThat(distance, is(2));
  }

  @Test
  public void distanceWithWordDeletionCost1Test() throws Exception {
    final int WORD_DELETION_COST = 1;

    int distance = Distance.editDistance(WORD_DELETION_COST, "x", "x");
    assertThat(distance, is(0));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "xyz");
    assertThat(distance, is(2));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "specified x");
    assertThat(distance, is(1));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "specified xy");
    assertThat(distance, is(2));

    distance = Distance.editDistance(WORD_DELETION_COST, "specified", "specified xy");
    assertThat(distance, is(1));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "the specified xy");
    assertThat(distance, is(3));
  }

  @Test
  public void distanceWithWordDeletionCost2Test() throws Exception {
    final int WORD_DELETION_COST = 2;

    int distance = Distance.editDistance(WORD_DELETION_COST, "x", "x");
    assertThat(distance, is(0));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "xyz");
    assertThat(distance, is(2));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "specified x");
    assertThat(distance, is(2));

    distance = Distance.editDistance(WORD_DELETION_COST, "x", "specified xy");
    assertThat(distance, is(3));

    distance = Distance.editDistance(WORD_DELETION_COST, "map", "the specified myMap");
    assertThat(distance, is(6));
  }
}
