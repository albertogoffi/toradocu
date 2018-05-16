package org.toradocu.extractor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.toradocu.util.Checks;

/** This class represents a method parameter. */
public final class DocumentedParameter {

  static final List<String> notNullAnnotations = Arrays.asList("NotNull", "NonNull", "Nonnull");
  static final List<String> nullableAnnotations = Collections.singletonList("Nullable");

  /** The parameter this class wraps. */
  private final java.lang.reflect.Parameter parameter;
  /** The name of the parameter. */
  private final String name;
  /** True if this parameter is nullable, false if nonnull, and null if unspecified. */
  private final Boolean nullable;

  /**
   * Constructs a parameter with the given type and name.
   *
   * @param parameter the parameter
   * @param name the name of the parameter
   * @param nullable true if the parameter is nullable, false if nonnull and null if unspecified
   * @throws NullPointerException if type or name is null
   */
  public DocumentedParameter(java.lang.reflect.Parameter parameter, String name, Boolean nullable) {
    Checks.nonNullParameter(parameter, "parameter");
    Checks.nonNullParameter(name, "name");
    this.parameter = parameter;
    this.name = name;
    this.nullable = nullable;
  }

  /**
   * Constructs a parameter with the given type and name.
   *
   * @param parameter the type of the parameter including its dimension
   * @param name the name of the parameter
   */
  public DocumentedParameter(java.lang.reflect.Parameter parameter, String name) {
    this(parameter, name, null);
  }

  /**
   * Returns the name of the parameter.
   *
   * @return the name of the parameter
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of the parameter.
   *
   * @return the type of the parameter
   */
  public Class<?> getType() {
    return parameter.getType();
  }

  /**
   * Returns the reflection parameter this parameter wraps.
   *
   * @return the reflection parameter this parameter wraps
   */
  public java.lang.reflect.Parameter asReflectionParameter() {
    return parameter;
  }

  /**
   * Returns {@code true} if the parameter is nullable, {@code false} if it is nonnull, or {@code
   * null} if its nullability is unspecified.
   *
   * @return {@code true} if the parameter is nullable, {@code false} if it is nonnull, or {@code
   *     null} if its nullability is unspecified
   */
  public Boolean isNullable() {
    return nullable;
  }

  /**
   * Returns true if this {@code DocumentedParameter} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DocumentedParameter)) {
      return false;
    }

    DocumentedParameter that = (DocumentedParameter) obj;
    return parameter.equals(that.parameter)
        && name.equals(that.name)
        && Objects.equals(nullable, that.nullable);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(parameter, name, nullable);
  }

  /**
   * Returns a string representation of this parameter. The returned string is in the format "TYPE
   * NAME" where TYPE is the fully qualified parameter type and NAME is the name of the parameter.
   *
   * @return a string representation of this parameter
   */
  @Override
  public String toString() {
    String paramString = parameter.toString();
    // parameter is of type reflect.Parameter, thus toString() will return a bytecode-format String.
    // We convert it into plain Java format.
    return paramString.substring(0, paramString.lastIndexOf(" ")) + " " + name;
  }
}
