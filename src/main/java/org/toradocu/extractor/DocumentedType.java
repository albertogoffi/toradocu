package org.toradocu.extractor;

import java.util.List;
import java.util.Objects;
import org.toradocu.util.Checks;

/** Represent a class or interface that is documented with Javadoc comments. */
public final class DocumentedType {

  /** Documented class or interface (or enum, ...). */
  private final Class<?> documentedType;
  /** Constructors and methods of this documented type. */
  private final List<ExecutableMember> executableMembers;

  /**
   * Creates a new DocumentedType wrapping the given class and with the given constructors and
   * methods.
   *
   * @param documentedClass the {@code Class} of this documentedClass
   * @param executableMembers constructors and methods of {@code documentedClass}
   * @throws NullPointerException if either documentedClass or executableMembers is null
   */
  DocumentedType(Class<?> documentedClass, List<ExecutableMember> executableMembers) {
    Checks.nonNullParameter(documentedClass, "documentedClass");
    Checks.nonNullParameter(executableMembers, "executableMembers");
    this.documentedType = documentedClass;
    this.executableMembers = executableMembers;
  }

  /**
   * Returns the documented type class.
   *
   * @return the documented type class
   */
  Class<?> getDocumentedClass() {
    return documentedType;
  }

  /**
   * Returns constructors and methods of this {@code documentedType}.
   *
   * @return constructors and methods of this {@code documentedType}
   */
  public List<ExecutableMember> getExecutableMembers() {
    return executableMembers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DocumentedType)) {
      return false;
    }
    DocumentedType that = (DocumentedType) o;
    return documentedType.equals(that.documentedType)
        && executableMembers.equals(that.executableMembers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentedType, executableMembers);
  }
}
