package org.toradocu.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FakeSourceBuilder {

  private Set<String> conditions = new HashSet<>();
  private Set<String> arguments = new HashSet<>();
  private Set<String> varArgArguments = new HashSet<>();
  private Set<String> imports = new HashSet<>();
  private Set<String> methodTypeParameters = new HashSet<>();
  private Set<String> classTypeParameters = new HashSet<>();
  private String packageDeclaration = "";

  public String buildSource() {
    StringBuilder fakeSource = new StringBuilder();

    if(!packageDeclaration.isEmpty()){
      fakeSource.append("package ");
      fakeSource.append(packageDeclaration);
      fakeSource.append(";");
    }
    for (String anImport : imports) {
      fakeSource.append("import ");
      fakeSource.append(anImport);
      fakeSource.append(";");
      fakeSource.append("\n");
    }
    fakeSource.append("public class GeneratedSpecs ");
    if (!classTypeParameters.isEmpty()) {
      fakeSource.append("<");
      fakeSource.append(String.join(",", classTypeParameters));
      fakeSource.append("> ");
    }
    fakeSource.append("{");
    fakeSource.append("\n");
    fakeSource.append("public ");
    if (!methodTypeParameters.isEmpty()) {
      fakeSource.append("<");
      fakeSource.append(String.join(",", methodTypeParameters));
      fakeSource.append("> ");
    }

    fakeSource.append("void");
    fakeSource.append(" foo (");
    fakeSource.append(String.join(",", arguments));
    if (!arguments.isEmpty() && !varArgArguments.isEmpty()) {
      fakeSource.append(",");
    }
    fakeSource.append(String.join(",", varArgArguments));
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
    if (type.contains("$")) {
      type = type.replaceAll("\\$", ".");
    }
    arguments.add(type + " " + argument);
  }

  public void addArgument(String argumentDeclaration) {
    int spaceBeforeArgName = argumentDeclaration.lastIndexOf(" ");
    String type = argumentDeclaration.substring(0, spaceBeforeArgName);
    String name = argumentDeclaration.substring(spaceBeforeArgName, argumentDeclaration.length());
    addArgument(type, name);
  }

  public void addVarArgArgument(String type, String argument) {
    if (type.contains("$")) {
      type = type.replaceAll("\\$", ".");
    }
    varArgArguments.add(type + " " + argument);
  }

  public void addVarArgArgument(String argumentDeclaration) {
    int spaceBeforeArgName = argumentDeclaration.lastIndexOf(" ");
    String type = argumentDeclaration.substring(0, spaceBeforeArgName);
    String name = argumentDeclaration.substring(spaceBeforeArgName, argumentDeclaration.length());
    addVarArgArgument(type, name);
  }

  public void addCondition(String condition) {
    conditions.add(condition);
  }

  public void addImport(String anImport) {
    if (anImport.contains("$")) {
      // inner class
      anImport = anImport.replaceAll("\\$", ".");
    }
    imports.add(anImport);
  }

  public void copyTypeArguments(TypeVariable<?>[] typeArguments) {
    if (typeArguments != null) {
      for (TypeVariable<?> typeParam : typeArguments) {
        String typeParamDeclaration = typeParam.getName();
        if(typeParam.getBounds().length>0) {
          typeParamDeclaration = includeBounds(typeParam, typeParamDeclaration);
        }
        this.methodTypeParameters.add(typeParamDeclaration);
      }
    }
  }

  @NotNull
  private String includeBounds(TypeVariable<?> typeParam, String typeParamDeclaration) {
    typeParamDeclaration += " extends ";
    List<String> bounds = Arrays.stream(typeParam.getBounds()).
            map(Type::getTypeName).collect(Collectors.toList());
    typeParamDeclaration += String.join(",", bounds);
    return typeParamDeclaration;
  }

  public void copyClassTypeArguments(TypeVariable<? extends Class<?>>[] typeArguments) {
    if (typeArguments != null) {
      for (TypeVariable<? extends Class<?>> typeParam : typeArguments) {
        String typeParamDeclaration = typeParam.getName();
        if(typeParam.getBounds().length>0) {
          typeParamDeclaration = includeBounds(typeParam, typeParamDeclaration);
        }
        this.classTypeParameters.add(typeParamDeclaration);
      }
    }
  }

  public void addPackage(Class<?> declaringClass) {
    this.packageDeclaration = declaringClass.getPackage().getName();
  }
}
