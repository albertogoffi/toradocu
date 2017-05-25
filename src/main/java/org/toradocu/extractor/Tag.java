package org.toradocu.extractor;

/** Represents a Javadoc tag. Supported tags are registered in {@code Tag.Kind}. */
public interface Tag {

  enum Kind {
    THROWS, // @throws and @exception
    PARAM, // @param
    RETURN; // @return

    @Override
    public String toString() {
      final String label = name();
      switch (label) {
        case "THROWS":
          return "@throws";
        case "PARAM":
          return "@param";
        case "RETURN":
          return "@return";
        default:
          throw new IllegalStateException("The value " + label + " has no string representation.");
      }
    }
  }

  /**
   * Returns the kind of this tag (e.g., @throws, @param).
   *
   * @return the kind of this tag
   */
  Kind getKind();

  /**
   * Returns the translated Java boolean condition for this tag. Empty if the translation has not
   * been attempted yet or no translation has been generated.
   *
   * @return the translated conditions for this tag. Empty if the translation has not been attempted
   *     yet or no translation has been generated.
   */
  String getCondition();

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
  Comment getComment();

  /**
   * Sets the comment for this tag.
   *
   * @param comment the comment for this tag
   * @throws NullPointerException if comment is null
   */
  void setComment(Comment comment);
}
