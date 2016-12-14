package org.toradocu.extractor;

import java.util.Optional;

interface Tag {

  /**
   * Returns the kind of this tag (e.g., @throws, @param).
   *
   * @return the kind of this tag
   */
  String getKind();

  /**
   * Returns the translated Java boolean condition for this tag as an optional which is empty if
   * translation has not been attempted yet.
   *
   * @return the translated conditions for this tag if translation attempted, else empty optional
   */
  Optional<String> getCondition();

  /**
   * Sets the translated condition for this tag to the given condition.
   *
   * @param condition the translated condition for this tag (as a Java boolean condition)
   * @throws NullPointerException if condition is null
   */
  void setCondition(String condition);

  /**
   * Returns the comment associated with the exception in this tag.
   *
   * @return the comment associated with the exception in this tag
   */
  String getComment();
}
