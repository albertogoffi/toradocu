package org.toradocu.extractor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.toradocu.Toradocu;
import org.toradocu.util.Checks;
import org.toradocu.util.Reflection;

/**
 * DocumentedMethod represents the Javadoc documentation for a method in a class. It identifies the
 * method itself and key Javadoc information associated with it, such as throws tags, parameters,
 * and return type.
 */
public final class DocumentedMethod {

  /** Method signature in the format method_name(type1 arg1, type2 arg2, ...). */
  private final String signature;
  /** Simple name of the method. */
  private final String name;
  /** Class in which the method is contained. */
  private final Type containingClass;
  /**
   * Target class passed to Toradocu with the option --target-class. (Information needed for Randoop
   * integration.)
   */
  private final String targetClass;
  /** Flag indicating whether this method takes a variable number of arguments. */
  private final boolean isVarArgs;
  /**
   * Return type of the method. {@code null} if this DocumentedMethod represents a constructor.
   * {@code Type.VOID} if the return type is void.
   */
  private final Type returnType;
  /** Method's parameters. */
  private final List<Parameter> parameters;
  /** Param tags specified in the method's Javadoc introduced in order. */
  private final Set<ParamTag> paramTags;
  /**
   * return tags specified in the method's Javadoc introduced in order. (If more than one, but
   * that's weird)
   */
  private final Set<ReturnTag> returnTags;
  /**
   * Throws tags specified in the method's Javadoc. Also, each throws tag can contain the
   * translation of the comment as Java boolean condition.
   */
  private final Set<ThrowsTag> throwsTags;

  /**
   * Constructs a {@code DocumentedMethod} contained in a given {@code containingClass} with the
   * given {@code name}, {@code returnType}, {@code parameters}, and {@code throwsTags}.
   *
   * @param containingClass class containing the {@code DocumentedMethod}
   * @param name the simple name of the {@code DocumentedMethod}
   * @param returnType the fully qualified return type of the method or {@code null} if the {@code
   *     DocumentedMethod} is a constructor
   * @param parameters the parameters of the {@code DocumentedMethod}
   * @param paramTags the {@code @param tags} of the {@code DocumentedMethod}
   * @param isVarArgs true if the {@code DocumentedMethod} takes a variable number of arguments,
   *     false otherwise
   * @param throwsTags the {@code @throws tags} of the {@code DocumentedMethod}
   * @throws NullPointerException if {@code containingClass} or {@code name} is null
   */
  public DocumentedMethod(
      Type containingClass,
      String name,
      Type returnType,
      List<Parameter> parameters,
      Collection<ParamTag> paramTags,
      boolean isVarArgs,
      Collection<ThrowsTag> throwsTags,
      Collection<ReturnTag> returnTags) {
    Checks.nonNullParameter(containingClass, "containingClass");
    Checks.nonNullParameter(name, "name");

    if (name.contains(".")) {
      throw new IllegalArgumentException(
          "Invalid method name: "
              + name
              + ". Method's name must be a valid Java method name (i.e., method name must not contain '.'");
    }

    this.containingClass = containingClass;
    this.name = name;
    this.returnType = returnType;
    this.parameters = parameters == null ? new ArrayList<>() : new ArrayList<>(parameters);
    this.paramTags = paramTags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(paramTags);
    this.isVarArgs = isVarArgs;
    this.throwsTags = throwsTags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(throwsTags);
    this.returnTags = returnTags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(returnTags);

    // Create the method signature using the method name and parameters.
    StringBuilder signatureBuilder = new StringBuilder(name + "(");
    for (Parameter param : this.parameters) {
      signatureBuilder.append(param);
      signatureBuilder.append(",");
    }
    // Remove last comma when needed.
    if (signatureBuilder.charAt(signatureBuilder.length() - 1) == ',') {
      signatureBuilder.deleteCharAt(signatureBuilder.length() - 1);
    }
    signatureBuilder.append(")");
    signature = signatureBuilder.toString();

    // Set the target class if command line options have been parsed.
    targetClass = Toradocu.configuration != null ? Toradocu.configuration.getTargetClass() : null;
  }

  /**
   * Returns an unmodifiable view of the throws tags in this method.
   *
   * @return an unmodifiable view of the throws tags in this method
   */
  public Set<ThrowsTag> throwsTags() {
    return Collections.unmodifiableSet(throwsTags);
  }

  /**
   * Returns an unmodifiable view of the param tags in this method.
   *
   * @return an unmodifiable view of the param tags in this method.
   */
  public Set<ParamTag> paramTags() {
    return Collections.unmodifiableSet(paramTags);
  }

  /**
   * Returns an unmodifiable view of the return tags in this method.
   *
   * @return an unmodifiable view of the return tags in this method
   */
  public Set<ReturnTag> returnTags() {
    return Collections.unmodifiableSet(returnTags);
  }

  /**
   * Returns true if this method takes a variable number of arguments, false otherwise.
   *
   * @return {@code true} if this method takes a variable number of arguments, {@code false}
   *     otherwise
   */
  public boolean isVarArgs() {
    return isVarArgs;
  }

  /**
   * Returns the simple name of this method.
   *
   * @return the simple name of this method
   */
  public String getName() {
    return name;
  }

  /**
   * Returns true if this method is a constructor, false otherwise.
   *
   * @return {@code true} if this method is a constructor, {@code false} otherwise
   */
  public boolean isConstructor() {
    return returnType == null;
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
  public Type getReturnType() {
    return returnType;
  }

  /**
   * Returns the class in which this method is contained.
   *
   * @return the class in which this method is contained
   */
  public Type getContainingClass() {
    return containingClass;
  }

  /**
   * Returns true if this {@code DocumentedMethod} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof DocumentedMethod)) return false;

    DocumentedMethod that = (DocumentedMethod) obj;
    if (this.containingClass.equals(that.containingClass)
        && this.name.equals(that.name)
        && Objects.equals(this.returnType, that.returnType)
        && this.parameters.equals(that.parameters)
        && this.isVarArgs == that.isVarArgs
        && this.throwsTags.equals(that.throwsTags)
        && this.paramTags.equals(that.paramTags)) {
      return true;
    }
    return false;
  }

  /**
   * Returns the {@code java.lang.reflect.Executable} corresponding to this DocumentedMethod.
   *
   * @return the {@code java.lang.reflect.Executable} corresponding to this DocumentedMethod.
   *     Returns null if no corresponding Executable is found.
   */
  public Executable getExecutable() {
    Class<?> containingClass = Reflection.getClass(getContainingClass().getQualifiedName());

    // Load the DocumentedMethod as a reflection Method or Constructor.
    if (isConstructor()) {
      for (Constructor<?> constructor : containingClass.getDeclaredConstructors()) {
        List<Class<?>> params =
            Arrays.stream(constructor.getParameterTypes()).collect(Collectors.toList());

        // The first two parameters of enum constructors are synthetic and must be removed to
        // reflect the source code.
        if (containingClass.isEnum()) {
          params.remove(0);
          params.remove(0);
        }

        if (Reflection.checkTypes(
            getParameters().toArray(new Parameter[0]), params.toArray(new Class<?>[0]))) {
          return constructor;
        }
      }
    } else {
      for (Method method : containingClass.getDeclaredMethods()) {
        if (method.getName().equals(getName())
            && Reflection.checkTypes(
                getParameters().toArray(new Parameter[0]), method.getParameterTypes())) {
          return method;
        }
      }
    }
    return null;
  }

  /**
   * Returns the target class as specified with the command line option --target-class.
   *
   * @return the target class as specified with the command line option --target-class. Null if the
   *     command line options have not been parsed.
   */
  public String getTargetClass() {
    return targetClass;
  }

  /**
   * Returns the hash code of this object.
   *
   * @return the hash code of this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(containingClass, name, returnType, parameters, paramTags, throwsTags);
  }

  /**
   * Returns return type (when present), fully qualified class, and signature of this method in the
   * format "&lt;RETURN TYPE&gt; &lt;CLASS TYPE.METHOD SIGNATURE&gt;"
   *
   * @return return the string representation of this method
   */
  @Override
  public String toString() {
    StringBuilder methodAsString = new StringBuilder();
    if (returnType != null) {
      methodAsString.append(returnType + " ");
    }
    methodAsString.append(containingClass + Type.SEPARATOR + signature);
    return methodAsString.toString();
  }
}
