package org.toradocu.util;

import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;

public class FakeSourceBuilder {

  private Set<String> conditions = new HashSet<>();
  private Set<String> arguments = new HashSet<>();
  private Set<String> imports = new HashSet<>();
  private Set<String> methodTypeParameters = new HashSet<>();
  private Set<String> classTypeParameters = new HashSet<>();

  public String buildSource() {
    StringBuilder fakeSource = new StringBuilder();

    for (String anImport : imports) {
      fakeSource.append("import ");
      fakeSource.append(anImport);
      fakeSource.append(";");
      fakeSource.append("\n");
    }
    fakeSource.append("public class GeneratedSpecs");
    if (!classTypeParameters.isEmpty()) {
      fakeSource.append(" <");
      fakeSource.append(String.join(",", classTypeParameters));
      fakeSource.append("> ");
    }
    fakeSource.append("{");
    fakeSource.append("\n");
    fakeSource.append("public ");
    if (!methodTypeParameters.isEmpty()) {
      fakeSource.append(" <");
      fakeSource.append(String.join(",", methodTypeParameters));
      fakeSource.append("> ");
    }

    fakeSource.append("void");
    fakeSource.append(" foo (");
    fakeSource.append(String.join(",", arguments));
    fakeSource.append(") {");
    fakeSource.append("\n");
    for (String condition : conditions) {
      fakeSource.append("if(");
      fakeSource.append(condition);
      fakeSource.append(")");
      fakeSource.append("\n");
    }
    fakeSource.append("return;} }");
    return fakeSource.toString();
  }

  public void addArgument(String type, String argument) {
    arguments.add(type + " " + argument);
  }

  public void addArgument(String argumentDeclaration) {
    arguments.add(argumentDeclaration);
  }

  public void addCondition(String condition) {
    conditions.add(condition);
  }

  public void addImport(String anImport) {
    imports.add(anImport);
  }

  public void copyTypeArguments(TypeVariable<?>[] typeArguments) {
    if (typeArguments != null) {
      for (TypeVariable<?> typeParam : typeArguments) {
        this.methodTypeParameters.add(typeParam.getName());
      }
    }
  }

  public void copyClassTypeArguments(TypeVariable<? extends Class<?>>[] typeArguments) {
    if (typeArguments != null) {
      for (TypeVariable<? extends Class<?>> typeParam : typeArguments) {
        this.classTypeParameters.add(typeParam.getName());
      }
    }
  }
}
