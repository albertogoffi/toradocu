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
}
