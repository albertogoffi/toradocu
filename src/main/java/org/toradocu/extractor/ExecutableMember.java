package org.toradocu.extractor;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.toradocu.Checks;

/**
 * ExecutableMember represents the Javadoc documentation for a method in a class. It identifies the
 * method itself and key Javadoc information associated with it, such as throws tags, parameters,
 * and return type.
 */
public final class ExecutableMember {

  /**
   * Method signature in reflection format as returned by {@code
   * java.lang.reflect.Executable.toString()}.
   */
  private final String signature;

  private final List<Tag> tags;
  private final String className;

  /** Reflection executable of this ExecutableMember. */
  private final Executable executable;

  private final List<Parameter> parameters;

  // Tags caches.
  private final List<ParamTag> paramTags;
  private final List<ThrowsTag> throwsTags;
  private ReturnTag returnTag;

  public ExecutableMember(String signature) throws ClassNotFoundException {
    this(signature, new ArrayList<>(), new ArrayList<>());
  }

  public ExecutableMember(String signature, List<Tag> tags) throws ClassNotFoundException {
    this(signature, new ArrayList<>(), tags);
  }

  public ExecutableMember(String signature, List<Parameter> parameters, List<Tag> tags)
      throws ClassNotFoundException {
    Checks.nonNullParameter(signature, "signature");
    Checks.nonNullParameter(parameters, "parameters");
    Checks.nonNullParameter(tags, "tags");

    this.signature = signature;
    className = identifyClassName(signature);
    executable = findExecutable(signature);
    this.parameters = parameters;
    this.tags = tags;

    paramTags = new ArrayList<>();
    throwsTags = new ArrayList<>();
    returnTag = null;
    loadTags(tags);
  }

  private void loadTags(List<Tag> tags) {
    for (Tag tag : tags) {
      if (tag instanceof ParamTag) {
        paramTags.add((ParamTag) tag);
      } else if (tag instanceof ReturnTag) {
        if (returnTag == null) {
          returnTag = (ReturnTag) tag;
        } else {
          throw new IllegalArgumentException(
              "Javadoc documentation must contain only one @return tag");
        }
      } else if (tag instanceof ThrowsTag) {
        throwsTags.add((ThrowsTag) tag);
      }
    }
  }

  private String identifyClassName(String signature) {
    final int dotPosition = signature.lastIndexOf(".");
    if (dotPosition == -1) {
      throw new IllegalArgumentException(
          "Invalid signature format. "
              + signature
              + " must contain "
              + "both class and method name");
    }
    return signature.substring(0, dotPosition);
  }

  private Executable findExecutable(String signature) throws ClassNotFoundException {
    final Class<?> clazz = Reflection.getClass(className);

    List<Executable> executables = new ArrayList<>();
    final List<Constructor> constructors = Arrays.stream(clazz.getConstructors()).collect(toList());
    final List<Method> methods = Arrays.stream(clazz.getMethods()).collect(toList());
    executables.addAll(constructors);
    executables.addAll(methods);

    final List<Executable> matchingExecutables =
        executables.stream().filter(e -> e.toString().equals(signature)).collect(toList());
    if (matchingExecutables.isEmpty()) {
      throw new AssertionError(
          "Impossible to load executable " + signature + "." + " Check the provided binaries.");
    }
    if (matchingExecutables.size() > 1) {
      throw new AssertionError(
          "Only one single executable member should match. "
              + "Instead multiple matching members were found: "
              + matchingExecutables);
    }
    return matchingExecutables.get(0);
  }

  /**
   * Returns an unmodifiable view of the throws tags in this method.
   *
   * @return an unmodifiable view of the throws tags in this method
   */
  public List<ThrowsTag> throwsTags() {
    return Collections.unmodifiableList(throwsTags);
  }

  /**
   * Returns an unmodifiable view of the param tags in this method.
   *
   * @return an unmodifiable view of the param tags in this method.
   */
  public List<ParamTag> paramTags() {
    return Collections.unmodifiableList(paramTags);
  }

  /**
   * Returns the return tag in this method.
   *
   * @return the return tag in this method. Null if there is no @return comment
   */
  public ReturnTag returnTag() {
    return returnTag;
  }

  /**
   * Returns true if this method takes a variable number of arguments, false otherwise.
   *
   * @return {@code true} if this method takes a variable number of arguments, {@code false}
   *     otherwise
   */
  public boolean isVarArgs() {
    return executable.isVarArgs();
  }

  /**
   * Returns the simple name of this method.
   *
   * @return the simple name of this method
   */
  public String getName() {
    return executable.getName();
  }

  /**
   * Returns true if this method is a constructor, false otherwise.
   *
   * @return {@code true} if this method is a constructor, {@code false} otherwise
   */
  public boolean isConstructor() {
    return executable instanceof Constructor;
  }

  /**
   * Returns an unmodifiable list view of the parameters in this method.
   *
   * @return an unmodifiable list view of the parameters in this method
   */
  public List<Parameter> getParameters() {
    return Collections.unmodifiableList(parameters);
  }

  /**
   * Returns the signature of this method.
   *
   * @return the signature of this method
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Returns the return type of this method or null if this is a constructor.
   *
   * @return the return type of this method or {@code null} if this is a constructor
   */
  public AnnotatedType getReturnType() {
    return executable.getAnnotatedReturnType();
  }

  /**
   * Returns the fully qualified class name in which this executable member is defined.
   *
   * @return the class in which this executable member is defined
   */
  public String getContainingClass() {
    return className;
  }

  /**
   * Returns true if this {@code ExecutableMember} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ExecutableMember)) return false;

    ExecutableMember that = (ExecutableMember) obj;
    return this.signature.equals(that.signature)
        && this.parameters.equals(that.parameters)
        && this.tags.equals(that.tags);
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(signature, parameters, tags);
  }

  /**
   * Returns the string representation of this executable member as returned by {@code
   * java.lang.reflect.Executable#toString}
   *
   * @return return the string representation of this method
   */
  @Override
  public String toString() {
    return executable.toString();
  }
}
