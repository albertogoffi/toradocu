package org.toradocu.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.toradocu.Toradocu;
import org.toradocu.conf.Configuration;
import org.toradocu.util.Distance;

public class EditDistanceTest {

  /**
   * Initializes Toradocu.configuration field that is read during initialization of the class
   * util.Distance (thus preventing a NullPointerException).
   *
   * @throws Exception if something bad happens during the initialization of the field
   *         {@code Toradocu.configuration}
   */
  @BeforeClass
  public static void setUp() throws Exception {
    Configuration toradocuConfiguration = new Configuration();
    Toradocu.class.getDeclaredField("configuration").set(null, toradocuConfiguration);
  }

  @Test
  public void distanceWithWordDeletionCost0Test() throws Exception {
    // With removal word = 0, the distance measure becomes a regular edit distance with the most
    // close word.
    setWordDeletionCost(0);

    int distance = Distance.editDistance("x", "x");
    assertThat(distance, is(0));

    distance = Distance.editDistance("x", "xyz");
    assertThat(distance, is(1));

    distance = Distance.editDistance("x", "specified x");
    assertThat(distance, is(0));

    distance = Distance.editDistance("x", "specified xy");
    assertThat(distance, is(1));

    distance = Distance.editDistance("specified", "specified xy");
    assertThat(distance, is(0));

    distance = Distance.editDistance("map", "the specified myMap");
    assertThat(distance, is(2));
  }

  @Test
  public void distanceWithWordDeletionCost1Test() throws Exception {
    setWordDeletionCost(1);

    int distance = Distance.editDistance("x", "x");
    assertThat(distance, is(0));

    distance = Distance.editDistance("x", "xyz");
    assertThat(distance, is(2));

    distance = Distance.editDistance("x", "specified x");
    assertThat(distance, is(1));

    distance = Distance.editDistance("x", "specified xy");
    assertThat(distance, is(2));

    distance = Distance.editDistance("specified", "specified xy");
    assertThat(distance, is(1));

    distance = Distance.editDistance("x", "the specified xy");
    assertThat(distance, is(3));
  }

  @Test
  public void distanceWithWordDeletionCost2Test() throws Exception {
    setWordDeletionCost(2);

    int distance = Distance.editDistance("x", "x");
    assertThat(distance, is(0));

    distance = Distance.editDistance("x", "xyz");
    assertThat(distance, is(2));

    distance = Distance.editDistance("x", "specified x");
    assertThat(distance, is(2));

    distance = Distance.editDistance("x", "specified xy");
    assertThat(distance, is(3));

    distance = Distance.editDistance("map", "the specified myMap");
    assertThat(distance, is(6));
  }

  private void setWordDeletionCost(int cost) throws Exception {
    Distance.WORD_DELETION_COST = cost;
  }
}
