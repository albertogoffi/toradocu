package org.toradocu.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a series of propositions and conjunctions in a sentence, as in
 * "PROPOSITION_A CONJUNCTION_AB PROPOSITION_B CONJUNCTION_BC PROPOSITION_C ...". It provides
 * methods to retrieve propositions and conjunctions between propositions.
 */
public class PropositionSeries {

  private final List<Proposition> propositions;
  private final List<Conjunction> conjunctions;

  /**
   * Initializes an empty {@code PropositionSeries}.
   */
  public PropositionSeries() {
    propositions = new ArrayList<>();
    conjunctions = new ArrayList<>();
  }

  /**
   * Adds an initial proposition to an empty {@code PropositionSeries}. If this series is not empty,
   * an {@code IllegalStateException} is thrown.
   *
   * @param proposition the initial proposition to add to the series
   * @throws IllegalStateException if the series not empty
   */
  public void add(Proposition proposition) {
    if (!propositions.isEmpty()) {
      throw new IllegalStateException("Series is not empty. Use add(Proposition, Conjunction).");
    }
    propositions.add(proposition);
  }

  /**
   * Adds a conjunction and proposition to the series. The conjunction links the formerly last
   * proposition in the series with this proposition. An {@code IllegalStateException} is thrown if
   * there are no propositions in the series when this method is called (since there is then nothing
   * to link the conjunction with).
   *
   * @param conjunction the conjunction between the formerly last proposition and the given
   * proposition
   * @param proposition the proposition to add to the end of the series
   * @throws IllegalStateException if there is not already at least one proposition in the series
   */
  public void add(Conjunction conjunction, Proposition proposition) {
    if (propositions.isEmpty()) {
      throw new IllegalStateException("List is empty. Use add(Proposition)");
    }
    conjunctions.add(conjunction);
    propositions.add(proposition);
  }

  /**
   * Returns true if the series contains the given proposition.
   *
   * @param proposition the proposition that is checked
   * @return true if the series contains the given proposition
   */
  public boolean contains(Proposition proposition) {
    return propositions.contains(proposition);
  }

  /**
   * Returns true if the series is empty (no propositions or conjunctions).
   *
   * @return true if there are no propositions or conjunctions in the series
   */
  public boolean isEmpty() {
    return propositions.isEmpty();
  }

  /**
   * Returns the number of propositions in the series.
   *
   * @return the number of propositions in the series
   */
  public int numberOfPropositions() {
    return propositions.size();
  }

  /**
   * Returns an unmodifiable list view of the propositions in this series.
   *
   * @return an unmodifiable list view of the propositions in this series
   */
  public List<Proposition> getPropositions() {
    return Collections.unmodifiableList(propositions);
  }

  /**
   * Returns an unmodifiable list view of the conjunctions in this series.
   *
   * @return an unmodifiable list view of the conjunctions in this series
   */
  public List<Conjunction> getConjunctions() {
    return Collections.unmodifiableList(conjunctions);
  }

  /**
   * Returns the translation of this series of propositions to a Java expression.
   *
   * @return the translation of this series of propositions to a Java expression
   */
  public String getTranslation() {
    StringBuilder output = new StringBuilder();
    // Only output translations for those propositions that actually have a translation.
    int i = 0;
    while (i < numberOfPropositions() && !propositions.get(i).getTranslation().isPresent()) {
      i++;
    }
    if (i < numberOfPropositions()) {
      output.append(propositions.get(i).getTranslation().get());
      for (int j = i + 1; j < numberOfPropositions(); j++) {
        if (propositions.get(j).getTranslation().isPresent()) {
          output.append(conjunctions.get(j - 1));
          output.append(propositions.get(j).getTranslation().get());
        }
      }
    }
    return output.toString();
  }

  /**
   * Returns a string representation of this series. The returned string is formatted as
   * "PROPOSITION_A CONJUNCTION_AB PROPOSITION_B CONJUNCTION_BC PROPOSITION_C ..." where
   * CONJUNCTION_ij is the conjunction linking propositions PROPOSITION_i and PROPOSITION_j.
   *
   * @return a string representation of this series
   */
  @Override
  public String toString() {
    StringBuilder output = new StringBuilder();
    if (!isEmpty()) {
      output.append(propositions.get(0));
      for (int i = 1; i < numberOfPropositions(); i++) {
        output.append(conjunctions.get(i - 1));
        output.append(propositions.get(i));
      }
    }
    return output.toString();
  }
}
