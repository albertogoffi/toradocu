package org.toradocu.extractor;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import org.toradocu.util.Checks;

/**
 * DocumentedExecutable represents the Javadoc documentation for a method in a class. It identifies
 * the method itself and key Javadoc information associated with it, such as throws, param, and
 * return tags.
 */
public final class DocumentedExecutable {

  /** Reflection executable of this DocumentedExecutable. */
  private final Executable executable;
  /** Parameter list. */
  private final List<DocumentedParameter> parameters;

  /** Javadoc @param, @return, and @throws tags of this executable member. */
  private BlockTags tags;

  /** Represents the @param, @return, and @throws tags of an executable member. */
  public static class BlockTags {
    /** Javadoc @param tags of this executable member. */
    private final List<ParamTag> paramTags;
    /** Javadoc @return tag of this executable member. */
    private final ReturnTag returnTag;
    /** Javadoc @throws and @exception tags of this executable member. */
    private final List<ThrowsTag> throwsTags;

    /**
     * Create a representation of the block tags of an executable member.
     *
     * @param paramTags Javadoc @param tags of this executable member
     * @param returnTag Javadoc @return tag of this executable member
     * @param throwsTags @throws and @exception tags of this executable member
     */
    BlockTags(List<ParamTag> paramTags, ReturnTag returnTag, List<ThrowsTag> throwsTags) {
      this.paramTags = paramTags;
      this.returnTag = returnTag;
      this.throwsTags = throwsTags;
    }

    /**
     * Returns an unmodifiable view of the param tags in this method.
     *
     * @return an unmodifiable view of the param tags in this method
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
     * Returns an unmodifiable view of the throws tags in this method.
     *
     * @return an unmodifiable view of the throws tags in this method
     */
    public List<ThrowsTag> throwsTags() {
      return Collections.unmodifiableList(throwsTags);
    }

    /**
     * Returns true if this {@code BlockTags} and the specified object are equal.
     *
     * @param obj the object to test for equality
     * @return true if this object and {@code obj} are equal
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof BlockTags)) {
        return false;
      }

      BlockTags that = (BlockTags) obj;
      return this.paramTags.equals(that.paramTags)
          && this.returnTag.equals(that.returnTag)
          && this.throwsTags.equals(that.throwsTags);
    }

    /**
     * Returns the hash code of this object.
     *
     * @return the hash code of this object
     */
    @Override
    public int hashCode() {
      return Objects.hash(paramTags, returnTag, throwsTags);
    }
  }

  /**
   * Creates a new {@code DocumentedExecutable} wrapping the given executable, with the specified
   * parameters and Javadoc comments introduced by block tags.
   *
   * @param executable the executable this DocumentedExecutable wraps, must not be null
   * @param parameters the parameters of this DocumentedExecutable, must not be null
   * @param blockTags the Javadoc comments introduced by block tags (e.g., {@code @param},
   *     {@code @return}) associated with this executable member
   */
  DocumentedExecutable(
      Executable executable, List<DocumentedParameter> parameters, BlockTags blockTags) {
    Checks.nonNullParameter(executable, "executable");
    Checks.nonNullParameter(parameters, "parameters");

    this.executable = executable;
    checkParametersConsistency(executable.getParameters(), parameters);
    this.parameters = parameters;
    this.tags = blockTags;
  }

  /**
   * Checks that provided parameter types are consistent with executable (reflection) parameter
   * types.
   *
   * @param executableParams array of reflection parameter types
   * @param params list of {@code DocumentedParameter}
   */
  private void checkParametersConsistency(
      java.lang.reflect.Parameter[] executableParams, List<DocumentedParameter> params) {
    if (executableParams.length != params.size()) {
      throw new IllegalArgumentException(
          "Expected "
              + executableParams.length
              + " parameters, but "
              + params.size()
              + " provided.");
    }

    for (int i = 0; i < executableParams.length; i++) {
      final DocumentedParameter sourceParam = params.get(i);
      final java.lang.reflect.Parameter execParam = executableParams[i];
      if (!execParam.getType().equals(sourceParam.getType())) {
        throw new IllegalArgumentException(
            "Expected parameter type is " + execParam + " while provided type is " + sourceParam);
      }
    }
  }

  /**
   * Returns an unmodifiable view of the param tags in this method.
   *
   * @return an unmodifiable view of the param tags in this method.
   */
  public List<ParamTag> paramTags() {
    return tags.paramTags();
  }

  /**
   * Returns the return tag in this method.
   *
   * @return the return tag in this method. Null if there is no @return comment
   */
  public ReturnTag returnTag() {
    return tags.returnTag();
  }

  /**
   * Returns an unmodifiable view of the throws tags in this method.
   *
   * @return an unmodifiable view of the throws tags in this method
   */
  public List<ThrowsTag> throwsTags() {
    return tags.throwsTags();
  }

  /**
   * Returns the Javadoc comments introduced by a block tag (e.g., @param, @return, ...) of this
   * executable member.
   *
   * @return the Javadoc comments introduced by a block tag (e.g., @param, @return, ...) of this
   *     executable member
   */
  public BlockTags getTags() {
    return tags;
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
  public List<DocumentedParameter> getParameters() {
    return Collections.unmodifiableList(parameters);
  }

  /**
   * Returns the signature of this method in the format "executable_simple_name(parameter_list)"
   * where "parameter_list" has the format "arg1_qualified_type_name arg1_name,
   * arg2_qualified_type_name arg2_name, ...".
   *
   * @return the signature of this method
   */
  public String getSignature() {
    StringJoiner joiner = new StringJoiner(", ", "(", ")");
    parameters.forEach(param -> joiner.add(param.toString()));
    return executable.getName() + joiner.toString();
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
   * Returns the class in which this executable member is declared.
   *
   * @return the class in which this executable member is declared
   */
  public Class<?> getDeclaringClass() {
    return executable.getDeclaringClass();
  }

  /**
   * Returns the executable member of this constructor/method.
   *
   * @return the executable member of this constructor/method
   */
  public Executable getExecutable() {
    return executable;
  }

  /**
   * Returns true if this {@code DocumentedExecutable} and the specified object are equal.
   *
   * @param obj the object to test for equality
   * @return true if this object and {@code obj} are equal
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DocumentedExecutable)) {
      return false;
    }

    DocumentedExecutable that = (DocumentedExecutable) obj;
    return this.executable.equals(that.executable)
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
    return Objects.hash(executable, parameters, tags);
  }

  /**
   * Returns the string representation of this executable member as returned by {@code
   * java.lang.reflect.Executable#toString}.
   *
   * @return return the string representation of this method
   */
  @Override
  public String toString() {
    return executable.toString();
  }
}
