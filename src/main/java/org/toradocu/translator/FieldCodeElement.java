package org.toradocu.translator;

import java.lang.reflect.Field;

/**
 * This class represents a field code element for use in translation. It holds String identifiers
 * for the field and a Java expression representation of the field to build Java conditions.
 */
public class FieldCodeElement extends CodeElement<Field> {

  /** The class/object in which this field is contained. */
  private final String receiver;

  /**
   * Constructs and initializes a {@code FieldCodeElement} that identifies the given field.
   *
   * @param receiver the class/object in which this field is contained
   * @param field the field that this code element identifies
   */
  FieldCodeElement(String receiver, Field field) {
    super(field);
    this.receiver = receiver;

    // Add name identifier.
    addIdentifier(field.getName());
  }

  @Override
  protected String buildJavaExpression() {
    return receiver + "." + getJavaCodeElement().getName();
  }
}
