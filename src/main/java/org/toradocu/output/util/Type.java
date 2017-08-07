package org.toradocu.output.util;

/** Created by arianna on 28/06/17. */
import java.util.Objects;

public class Type {
  String qualifiedName;
  String name;
  boolean isArray;

  public Type(String qualifiedName, String name, boolean isArray) {
    this.qualifiedName = qualifiedName;
    this.name = name;
    this.isArray = isArray;
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Type that = (Type) o;

    return Objects.equals(qualifiedName, that.qualifiedName)
        && Objects.equals(name, that.name)
        && isArray == that.isArray;
  }

  @Override
  public int hashCode() {
    return Objects.hash(qualifiedName, name, isArray);
  }
}
