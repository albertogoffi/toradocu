package org.toradocu.translator;

/** This class represents a grammatical conjunction between propositions: AND or OR. */
public enum Conjunction {
  AND,
  OR;

  /**
   * Returns a string representation of this enum: " &amp;&amp; " for AND, " || " for OR.
   *
   * @return a string representation of this conjunction
   */
  @Override
  public String toString() {
    switch (this) {
      case AND:
        return " && ";
      case OR:
        return " || ";
      default:
        return "";
    }
  }
}
