package org.toradocu.util;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.TypeParameter;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

public class FakeSourceBuilder {

  private List<String> conditions = new ArrayList<>();
  private List<String> arguments = new ArrayList<>();
  private List<String> imports = new ArrayList<>();
  private List<String> methodTypeParameters = new ArrayList<>();
  private List<String> classTypeParameters = new ArrayList<>();

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
