package org.toradocu.output.util;

/** Created by arianna on 28/06/17. */
public class Type {
  String qualifiedName;
  String name;
  boolean isArray;

  public Type(String qualifiedName, String name, boolean isArray) {
    this.qualifiedName = qualifiedName;
    this.name = name;
    this.isArray = isArray;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Type type = (Type) o;

    if (isArray != type.isArray) return false;
    if (qualifiedName != null
        ? !qualifiedName.equals(type.qualifiedName)
        : type.qualifiedName != null) return false;
    return name != null ? name.equals(type.name) : type.name == null;
  }
}
