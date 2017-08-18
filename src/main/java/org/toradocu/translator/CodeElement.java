package org.toradocu.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.toradocu.util.Distance;

/**
 * This class wraps a Java code element (e.g., {@code java.lang.reflect.Parameter}, {@code
 * java.lang.reflect.Field}, etc.) for use in translation. This class contains String identifiers
 * (sequences of words that can be used in Javadoc comments to refer to this Java code element) for
 * the code element and a Java expression representation of the code element to build Java
 * conditions. A typical constructor invocation looks like this:
 *
 * <p>{@code new CodeElement(parameter, "names", "array", "array names")}.
 *
 * @param <T> the type of code element that this class holds data on
 */
public abstract class CodeElement<T> implements Comparable<CodeElement<?>> {

  /** Strings that can be used to refer to this code element in Javadoc comments. */
  private Set<String> identifiers;
  /** String used to build Java conditions. */
  private String javaExpression;
  // TODO Add a check on the type T so that a CodeElement can be only created with a supported type.
  /**
   * The Java code element this object wraps. T can only be a Java code element like {@code
   * java.lang.reflect.Parameter}. We don't know how to restrict this type since some classes in the
   * Java reflection APIs like {@code java.lang.reflect.Parameter} inherit directly from {@code
   * java.lang.Object}.
   */
  private T javaCodeElement;

  /**
   * Constructs a new {@code CodeElement} that represents the given element with the given
   * identifiers.
   *
   * @param javaCodeElement the element that this code element contains data on
   * @param identifiers string identifiers for the code element
   */
  protected CodeElement(T javaCodeElement, String... identifiers) {
    this(javaCodeElement);
    this.identifiers.addAll(Arrays.asList(identifiers));
  }

  /**
   * Constructs a new {@code CodeElement} that represents the given element.
   *
   * @param javaCodeElement the element that this code element contains data on
   */
  protected CodeElement(T javaCodeElement) {
    this.javaCodeElement = javaCodeElement;
    identifiers = new HashSet<>();
  }

  /**
   * Adds a string identifier for the code element that this object represents.
   *
   * @param identifier a string that identifies this code element
   */
  void addIdentifier(String identifier) {
    identifiers.add(identifier);
  }

  /**
   * Returns a list of strings that identify this code element.
   *
   * @return a list of strings that identify this code element
   */
  public Set<String> getIdentifiers() {
    return identifiers;
  }

  /**
   * Returns the Java expression representation of this code element for use in building Java
   * conditions.
   *
   * @return the Java expression representation of this code element
   */
  String getJavaExpression() {
    if (javaExpression == null) {
      javaExpression = buildJavaExpression();
    }
    return javaExpression;
  }

  /**
   * Builds and returns the Java expression representation of this code element. Clients should call
   * {@link #getJavaExpression} instead.
   *
   * @return the Java expression representation of this code element after building it
   */
  protected abstract String buildJavaExpression();

  /**
   * Returns the edit distance between this code element and the given string. The returned distance
   * is the minimum distance calculated for all the identifiers of this code element.
   * Integer.MAX_VALUE is returned if this code element has no identifiers.
   *
   * @param s the string to get the edit distance from
   * @return the minimum edit distance between the given string and the identifiers of this code
   *     element, or Integer.MAX_VALUE if this code element has no identifiers
   */
  int getEditDistanceFrom(String s) {
    return identifiers
        .stream()
        .map(identifier -> Distance.editDistance(identifier, s))
        .min(Comparator.naturalOrder())
        .orElse(Integer.MAX_VALUE);
  }

  /**
   * Returns the wrapped code element that this object holds data on.
   *
   * @return the wrapped code element that this object holds data on
   */
  public T getJavaCodeElement() {
    return javaCodeElement;
  }

  /**
   * Returns true if this {@code CodeElement} and the specified object are equal.
   *
   * @param obj the object to compare against this object
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CodeElement)) {
      return false;
    }

    CodeElement<?> that = (CodeElement<?>) obj;
    return this.getIdentifiers().equals(that.getIdentifiers())
        && this.getJavaCodeElement().equals(that.getJavaCodeElement())
        && this.getJavaExpression().equals(that.getJavaExpression());
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(identifiers, getJavaExpression(), javaCodeElement);
  }

  /**
   * Returns a string representation of this {@code CodeElement}. The returned string is formatted
   * as "EXPRESSION: [IDENTIFIER_1, IDENTIFIER_2, ...]", where EXPRESSION is the Java expression
   * representation of this code element and IDENTIFIER_i is an identifier for this code element.
   *
   * @return a string representation of this {@code CodeElement}
   */
  @Override
  public String toString() {
    return getJavaExpression() + ": " + getIdentifiers();
  }

  /**
   * Checks whether the given predicate translation is compatible with this code element. For
   * example, if this code element refers to a parameter of type boolean, the predicate translation
   * "==0" would be incompatible, and this method would return false.
   *
   * <p>The default implementation of this method do nothing and returns true. To effectively
   * perform a check, subclasses must override this method.
   *
   * @param declaringClass declaring class of the {@code DocumentedExecutable} to check
   *     compatibility in case of comparisons with receiver object
   * @param predicateTranslation the translation of the predicate whose compatibility with this code
   *     element has to be checked
   * @return true if the predicate translation is compatible, false otherwise
   */
  boolean isCompatibleWith(Class<?> declaringClass, String predicateTranslation) {
    return true;
  }

  @Override
  public int compareTo(CodeElement<?> o) {
    // Is this the best design? (We consider subclasses in this superclass.)

    // A lower index (position) in the priority list means a lower priority.
    List<Class> priorityList = new ArrayList<>();
    priorityList.add(GeneralCodeElement.class);
    priorityList.add(ContainerElementsCodeElement.class);
    priorityList.add(MethodCodeElement.class);
    priorityList.add(StaticMethodCodeElement.class);
    priorityList.add(FieldCodeElement.class);
    priorityList.add(ClassCodeElement.class);
    priorityList.add(ParameterCodeElement.class);

    return priorityList.indexOf(this.getClass()) - priorityList.indexOf(o.getClass());
  }
}
