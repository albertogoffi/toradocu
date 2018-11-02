package org.toradocu.translator;

/**
 * This class represents a dummy Java code element for use in translation. This code element does
 * not wrap a real Java code element, but rather it allows to represent an arbitrary piece of code
 * like ".length==0".
 *
 * <p>This class holds the String identifiers and Java expression representation specified by the
 * client.
 */
public class GeneralCodeElement extends CodeElement<String> {

  /** Null dereference check regarding the dummy object, might be null */
  private String nullDereferenceCheck;

  /**
   * Constructs and initializes a code element with the given identifiers and the given Java
   * expression representation.
   *
   * @param representation the Java expression representation of this code element
   * @param nullDereferenceCheck the expression to check null dereference
   * @param identifiers Strings that identify this code element
   */
  GeneralCodeElement(String representation, String nullDereferenceCheck, String... identifiers) {
    super(representation);
    this.nullDereferenceCheck = nullDereferenceCheck;
    for (String identifier : identifiers) {
      addIdentifier(identifier);
    }
  }

  @Override
  protected String buildJavaExpression() {
    return getJavaCodeElement();
  }

  /**
   * Getter for the null dereference check
   *
   * @return the null dereference check
   */
  String getNullDereferenceCheck() {
    return nullDereferenceCheck;
  }
}
