package org.toradocu.extractor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.toradocu.util.Checks;

/** This class represents a method parameter. */
public final class Parameter {

  static final List<String> notNullAnnotations = Arrays.asList("NotNull", "NonNull", "Nonnull");
  static final List<String> nullableAnnotations = Collections.singletonList("Nullable");

  /** The type of the parameter. */
  private final Class<?> type;
  /** The name of the parameter. */
  private final String name;
  /** True if this parameter is nullable, false if nonnull, and null if unspecified. */
  private final Boolean nullable;

  /**
   * Constructs a parameter with the given type and name.
   *
   * @param type the type of the parameter including its dimension
   * @param name the name of the parameter
   * @param nullable true if the parameter is nullable, false if nonnull and null if unspecified
   * @throws NullPointerException if type or name is null
   */
  public Parameter(Class<?> type, String name, Boolean nullable) {
    Checks.nonNullParameter(type, "type");
    Checks.nonNullParameter(name, "name");
    this.type = type;
    this.name = name;
    this.nullable = nullable;
  }

  /**
   * Constructs a parameter with the given type and name.
   *
   * @param type the type of the parameter including its dimension
   * @param name the name of the parameter
   */
  public Parameter(Class<?> type, String name) {
    this(type, name, null);
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
    return type;
  }

  /**
   * Returns {@code true} if the parameter is nullable, {@code false} if it is nonnull, or {@code
   * null} if its nullability is unspecified.
   *
   * @return {@code true} if the parameter is nullable, {@code false} if it is nonnull, or {@code
   *     null} if its nullability is unspecified
   */
  Boolean isNullable() {
    return nullable;
  }

  /**
   * Returns true if this {@code Parameter} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Parameter)) {
      return false;
    }

    Parameter that = (Parameter) obj;
    return type.equals(that.type)
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
    return Objects.hash(type, name, nullable);
  }

  /**
   * Returns a string representation of this parameter. The returned string is in the format "TYPE
   * NAME" where TYPE is the fully qualified parameter type and NAME is the name of the parameter.
   *
   * @return a string representation of this parameter
   */
  @Override
  public String toString() {
    return type.getName() + " " + name;
  }
}
