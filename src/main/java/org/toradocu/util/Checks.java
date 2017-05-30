package org.toradocu.util;

import java.util.Objects;

/**
 * This utility class contains methods to check different properties of given objects. For example,
 * the non-nullity of a method's parameter named foo can be checked with {@code
 * Checks.nonNullParameter(foo, "foo")}. The main advantage of this pattern is that in this way the
 * error messages provided to the users are consistent. This class is intended to be used within the
 * Toradocu codebase.
 */
public final class Checks {

  /**
   * This method checks whether {@code obj} is null. If it is null, a {@code NullPointerException}
   * will be thrown with the message "The parameter {@code parameterName} must not be null". This
   * method has no effect if {@code obj} is not null.
   *
   * @param obj the object to check for its nullity
   * @param parameterName the parameter's name that will be printed in the error message if {@code
   *     obj} is null
   * @throws NullPointerException if {@code obj} is null
   */
  public static void nonNullParameter(Object obj, String parameterName) {
    Objects.requireNonNull(obj, "The parameter " + parameterName + " must not be null");
  }
}
