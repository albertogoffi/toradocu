package org.toradocu.translator;

/**
 * The {@code Match} class bounds a matching expression computed by Toradocu with its
 * null-dereference check expression. The base expression is the match computed as-is. When an
 * expression involves invocations that may cause null dereference during the final assertion
 * execution,the expression must be preceded by the null dereference check.
 *
 * <p>Example: consider the comment "@param c, an empty collections". The base expression that
 * matches the comment is {@code c.isEmpty} and it needs to be preceded by the check {@code c!=null}
 */
public class Match {
  /** The basic expression of a match. */
  private String baseExpression;
  /** The null dereference check that an expression may need. */
  private String nullDereferenceCheck;

  /**
   * Creates a new Match with the given expression and check.
   *
   * @param baseExpression the base expression of the Match
   * @param nullDereferenceCheck the null dereference check, may be null
   */
  public Match(String baseExpression, String nullDereferenceCheck) {
    this.baseExpression = baseExpression;
    this.nullDereferenceCheck = nullDereferenceCheck;
  }

  /**
   * Getter method for the base expression.
   *
   * @return the base expression of the Match
   */
  String getBaseExpression() {
    return baseExpression;
  }

  /**
   * Setter method for the base expression.
   *
   * @param baseExpression value to give to the base expression
   */
  void setBaseExpression(String baseExpression) {
    this.baseExpression = baseExpression;
  }

  /**
   * Getter method for the null dereference check.
   *
   * @return the null dereference check for the match
   */
  String getNullDereferenceCheck() {
    return nullDereferenceCheck;
  }

  /**
   * Setter method for the null dereference check.
   *
   * @param nullDereferenceCheck value to give to the null dereference check
   */
  void setNullDereferenceCheck(String nullDereferenceCheck) {
    this.nullDereferenceCheck = nullDereferenceCheck;
  }

  /**
   * Use this method when you need to complete the base expression, for example while composing the
   * Match for a method call one parameter at time.
   *
   * @param extension the portion of expression to be added to the base
   */
  void completeExpression(String extension) {
    this.baseExpression += extension;
  }
}
