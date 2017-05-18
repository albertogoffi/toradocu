package org.toradocu.translator;

import org.toradocu.extractor.DocumentedMethod;

/**
 * Created by arianna on 18/05/17.
 *
 * <p>This class ties a String comment to its DocumentedMethod.
 */
public class MethodComment {
  private String comment;
  private DocumentedMethod method;

  public MethodComment(String comment, DocumentedMethod method) {
    this.comment = comment;
    this.method = method;
  }

  public String getComment() {
    return comment;
  }

  public DocumentedMethod getMethod() {
    return method;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MethodComment that = (MethodComment) o;

    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    return method != null ? method.equals(that.method) : that.method == null;
  }

  @Override
  public int hashCode() {
    int result = comment != null ? comment.hashCode() : 0;
    result = 31 * result + (method != null ? method.hashCode() : 0);
    return result;
  }
}
