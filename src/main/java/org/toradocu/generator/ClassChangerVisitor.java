package org.toradocu.generator;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitorAdapter;
import org.toradocu.Checks;

/**
 * Visitor that modifies the aspect template (see method {@code visit}) to generate an aspect
 * (oracle). In particular, this visitor changes the aspect class name to the appropriate aspect
 * name.
 */
public class ClassChangerVisitor extends ModifierVisitorAdapter<String> {

  /**
   * Modifies the name of the class from the aspect template to {@code aspectName}.
   *
   * @param declaration the class declaration to change
   * @param aspectName the aspect name (class name)
   * @return the {@code declaration} modified as and when needed
   * @throws NullPointerException if {@code declaration} or {@code aspectName} is null
   */
  @Override
  public Node visit(ClassOrInterfaceDeclaration declaration, String aspectName) {
    Checks.nonNullParameter(declaration, "declaration");
    Checks.nonNullParameter(aspectName, "aspectName");

    if (declaration.getName().equals("Aspect_Template")) {
      declaration.setName(aspectName);
    }
    return declaration;
  }
}
