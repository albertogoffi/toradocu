package org.toradocu.translator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.BeforeClass;
import org.junit.Test;
import org.toradocu.Toradocu;
import org.toradocu.conf.Configuration;
import org.toradocu.util.Distance;

public class EditDistanceTest {

  private static Field wordDeletionCost;

  @BeforeClass
  public static void setUp() throws Exception {
    Configuration toradocuConfiguration = new Configuration();
    Toradocu.class.getDeclaredField("configuration").set(null, toradocuConfiguration);

    wordDeletionCost = Distance.class.getDeclaredField("WORD_DELETION_COST");
    wordDeletionCost.setAccessible(true);

    // Remove final from Distance.WORD_DELETION_COST modifiers
    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(wordDeletionCost, wordDeletionCost.getModifiers() & ~Modifier.FINAL);
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
    wordDeletionCost.set(null, cost);
  }
}
