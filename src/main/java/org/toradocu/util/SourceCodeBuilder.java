package org.toradocu.util;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * This class generates a source code ready to be compiled. Every element that must be included in
 * such source code is wrapped by a different field. All elements are then combined in the correct
 * fashion to obtain a compilable source code.
 *
 * <p>The final source code includes: - necessary imports - a class declaration {@code public class
 * GeneratedSpecs} - a method {@code public void foo} declared inside the class - {@code if}
 * statements in the {@code foo} method body that wrap the boolean conditions to be compiled
 */
public class SourceCodeBuilder {

  /** The boolean conditions to include in the source code. */
  private Set<String> conditions = new HashSet<>();
  /** The method arguments to include in the source code. */
  private Set<String> arguments = new HashSet<>();
  /** The varArg arguments to include in the source code. */
  private Set<String> varArgArguments = new HashSet<>();
  /** The imports to include in the source code. */
  private Set<String> imports = new HashSet<>();
  /** The method type parameters. */
  private Set<String> methodTypeParameters = new HashSet<>();
  /** The class type parameters. */
  private Set<String> classTypeParameters = new HashSet<>();
  /** The package declaration. */
  private String packageDeclaration = "";

  /**
   * Method to be invoked after all the field (source code elements) have been prepared. Composes
   * the source code.
   *
   * @return the source code to be compiled
   */
  public String buildSource() {
    StringBuilder fakeSource = new StringBuilder();

    if (!packageDeclaration.isEmpty()) {
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

  /**
   * Stores a new argument of the {@code foo} method. Such method will exercise all the boolean
   * condition that you wish to compile, so be sure to include every code element is needed.
   *
   * @param type type of the argument
   * @param argument argument name
   */
  public void addArgument(String type, String argument) {
    if (type.contains("$")) {
      type = type.replaceAll("\\$", ".");
    }
    arguments.add(type + " " + argument);
  }

  /**
   * Stores a new argument of the {@code foo} method. Such method will exercise all the boolean
   * condition that you wish to compile, so be sure to include every code element is needed.
   *
   * @param argumentDeclaration argument declaration in the form of "argument_type argument_name"
   *     e.g. "int bar"
   */
  public void addArgument(String argumentDeclaration) {
    int spaceBeforeArgName = argumentDeclaration.lastIndexOf(" ");
    String type = argumentDeclaration.substring(0, spaceBeforeArgName);
    String name = argumentDeclaration.substring(spaceBeforeArgName, argumentDeclaration.length());
    addArgument(type, name);
  }

  /**
   * Stores a new vararg argument to the {@code foo} method. Such method will exercise all the
   * boolean condition that you wish to compile, so be sure to include every code element is needed.
   *
   * @param type type of the vararg argument
   * @param argument vararg argument name
   */
  public void addVarArgArgument(String type, String argument) {
    if (type.contains("$")) {
      type = type.replaceAll("\\$", ".");
    }
    varArgArguments.add(type + " " + argument);
  }

  /**
   * Stores a new vararg argument of the {@code foo} method. Such method will exercise all the
   * boolean condition that you wish to compile, so be sure to include every code element is needed.
   *
   * @param argumentDeclaration argument declaration in the form of "argument_type argument_name"
   *     e.g. "int bar"
   */
  public void addVarArgArgument(String argumentDeclaration) {
    int spaceBeforeArgName = argumentDeclaration.lastIndexOf(" ");
    String type = argumentDeclaration.substring(0, spaceBeforeArgName);
    String name = argumentDeclaration.substring(spaceBeforeArgName, argumentDeclaration.length());
    addVarArgArgument(type, name);
  }

  /**
   * Stores a boolean condition to be included in a {@code if} statement of the {@code foo} method
   * body.
   *
   * @param condition the boolean condition
   */
  public void addCondition(String condition) {
    conditions.add(condition);
  }

  /**
   * Stores an import to be included in the source code.
   *
   * @param anImport the name to be imported
   */
  public void addImport(String anImport) {
    if (anImport.contains("$")) {
      // inner class
      anImport = anImport.replaceAll("\\$", ".");
    }
    imports.add(anImport);
  }

  /**
   * Stores the method type parameters of the {@code foo} method.
   *
   * @param typeArguments array of type arguments
   */
  public void copyTypeArguments(TypeVariable<?>[] typeArguments) {
    if (typeArguments != null) {
      for (TypeVariable<?> typeParam : typeArguments) {
        String typeParamDeclaration = typeParam.getName();
        if (typeParam.getBounds().length > 0) {
          typeParamDeclaration = includeBounds(typeParam, typeParamDeclaration);
        }
        this.methodTypeParameters.add(typeParamDeclaration);
      }
    }
  }

  /**
   * Includes bounds to a type parameter that must be included in the {@code foo} method.
   *
   * @param typeParam the type parameter
   * @param typeParamDeclaration the type parameter declaration
   * @return an updated type parameter declaration that specifies bounds
   */
  @NotNull
  private String includeBounds(TypeVariable<?> typeParam, String typeParamDeclaration) {
    typeParamDeclaration += " extends ";
    List<String> bounds =
        Arrays.stream(typeParam.getBounds()).map(Type::getTypeName).collect(Collectors.toList());
    typeParamDeclaration += String.join(",", bounds);
    return typeParamDeclaration;
  }

  /**
   * Stores type arguments of the final class.
   *
   * @param typeArguments array of type arguments
   */
  public void copyClassTypeArguments(TypeVariable<? extends Class<?>>[] typeArguments) {
    if (typeArguments != null) {
      for (TypeVariable<? extends Class<?>> typeParam : typeArguments) {
        String typeParamDeclaration = typeParam.getName();
        if (typeParam.getBounds().length > 0) {
          typeParamDeclaration = includeBounds(typeParam, typeParamDeclaration);
        }
        this.classTypeParameters.add(typeParamDeclaration);
      }
    }
  }

  /**
   * Store a package declaration to be included in the source code. This is needed for
   * package-private classes.
   *
   * @param declaringClass the package-private classes to extract the package from
   */
  public void addPackage(Class<?> declaringClass) {
    this.packageDeclaration = declaringClass.getPackage().getName();
  }
}
