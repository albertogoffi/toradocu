package org.toradocu.translator;

/**
 * This class represents a general code element for use in translation. It holds the String
 * identifiers and Java expression representation specified by the client.
 */
public class GeneralCodeElement extends CodeElement<String> {

  /**
   * Constructs and initializes a code element with the given identifiers and the given Java
   * expression representation.
   *
   * @param representation the Java expression representation of this code element
   * @param identifiers Strings that identify this code element
   */
  public GeneralCodeElement(String representation, String... identifiers) {
    super(representation);
    for (String identifier : identifiers) {
      addIdentifier(identifier);
    }
  }

  @Override
  protected String buildJavaExpression() {
    return getJavaCodeElement();
  }
}
