package org.toradocu.util;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mdkt.compiler.CompilationException;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;
import org.toradocu.extractor.JavadocExtractor;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Property;

public class ComplianceChecks {

  /** Logger of this class. */
  private static final Logger log = LoggerFactory.getLogger(ComplianceChecks.class);

  /**
   * Tries to compile the boolean condition in the given {@code Guard} and tells whether the
   * compilation was successful.
   *
   * @param method documented executable the guard belongs to
   * @param guard the guard which condition must be checked for compliance
   * @return true if the condition was compilable, false otherwise
   */
  public static boolean isSpecCompilable(DocumentedExecutable method, Guard guard) {
    if (Modifier.isPrivate(method.getDeclaringClass().getModifiers())) {
      // if the target class is private we cannot apply compliance check.
      return true;
    }
    SourceCodeBuilder sourceCodeBuilder = addCommonInfo(method);
    addConditionCodeInformation(method, guard.getConditionText(), sourceCodeBuilder);
    String sourceCode = sourceCodeBuilder.buildSource();
    try {
      compileSource(sourceCode);
    } catch (CompilationException e) {
      log.info(
          "The following specification was generated but discarded:\n"
              + guard.getConditionText()
              + "\n"
              + e.getLocalizedMessage()
              + "\n");
      return false;
    } catch (ClassNotFoundException e) {
      // ignore
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  /**
   * Tries to compile the boolean conditions in the given {@code Guard} and {@code Property} and
   * tells whether the compilation was successful.
   *
   * @param method documented executable the guard belongs to
   * @param guard the guard which condition must be checked for compliance
   * @param property the property which condition must be checked for compliance
   * @return true if the condition was compilable, false otherwise
   */
  public static boolean isPostSpecCompilable(
      DocumentedExecutable method, Guard guard, Property property) {
    if (Modifier.isPrivate(method.getDeclaringClass().getModifiers())) {
      // if the target class is private we cannot apply compliance check.
      return true;
    }
    SourceCodeBuilder sourceCodeBuilder = addCommonInfo(method);
    String methodReturnType = method.getReturnType().getType().getTypeName();
    if (!methodReturnType.equals("void")) {
      sourceCodeBuilder.addArgument(methodReturnType, Configuration.RETURN_VALUE);
    }
    addConditionCodeInformation(method, guard.getConditionText(), sourceCodeBuilder);
    addConditionCodeInformation(method, property.getConditionText(), sourceCodeBuilder);
    String sourceCode = sourceCodeBuilder.buildSource();
    try {
      compileSource(sourceCode);
    } catch (CompilationException e) {
      log.info(
          "The following specification was generated but discarded:\n"
              + guard.getConditionText()
              + " ? "
              + property.getConditionText()
              + "\n"
              + e.getLocalizedMessage()
              + "\n");
      return false;
    } catch (ClassNotFoundException e) {
      // ignore
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  /**
   * Invokes the in-memory compiler on the given source code.
   *
   * @param sourceCode source code to be compiled
   * @throws Exception if the compiler encounters problems
   */
  private static void compileSource(String sourceCode) throws Exception {
    InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance();
    compiler.ignoreWarnings();
    List<String> classpath = new ArrayList<>();
    for (URL url : Configuration.INSTANCE.classDirs) {
      classpath.add(url.getPath());
    }
    compiler.useOptions("-cp", String.join(":", classpath));
    compiler.compile("GeneratedSpecs", sourceCode);
  }

  /**
   * Add to the source code to be compiled the information common to any specification.
   *
   * @param method the method which specifications must be compiled
   * @return a {@code SourceCodeBuilder} object that wraps the source code
   */
  private static SourceCodeBuilder addCommonInfo(DocumentedExecutable method) {
    SourceCodeBuilder sourceCodeBuilder = new SourceCodeBuilder();
    Class<?> declaringClass = method.getDeclaringClass();

    if (!Modifier.isPublic(declaringClass.getModifiers())) {
      // class is package-private
      sourceCodeBuilder.addPackage(declaringClass);
    }

    sourceCodeBuilder.addImport(declaringClass.getName());
    sourceCodeBuilder.addArgument(declaringClass.getName(), Configuration.RECEIVER);
    sourceCodeBuilder.copyClassTypeArguments(declaringClass.getTypeParameters());
    sourceCodeBuilder.copyTypeArguments(method.getExecutable().getTypeParameters());
    return sourceCodeBuilder;
  }

  /**
   * Finds if the condition involves an {@code instanceof} invocation and imports in the source code
   * the classes it is referred to
   *
   * @param method the method which specifications must be compiled
   * @param sourceCodeBuilder {@code SourceCodeBuilder} object that wraps the source code
   * @param conditionText condition text
   */
  private static void importClassesInInstanceOf(
      DocumentedExecutable method, SourceCodeBuilder sourceCodeBuilder, String conditionText) {
    Matcher matcher = Pattern.compile(" instanceof ([A-Z][A-Za-z]+)").matcher(conditionText);
    while (matcher.find()) {
      String className = matcher.group(1);

      Configuration configuration = Configuration.INSTANCE;
      final String sourceFile =
          configuration.sourceDir.toString()
              + File.separator
              + method.getDeclaringClass().getCanonicalName().replaceAll("\\.", File.separator)
              + ".java";
      List<String> classesInPackage =
          JavadocExtractor.getClassesInSamePackage(
              method.getDeclaringClass().getCanonicalName(), sourceFile);
      for (String classInPackage : classesInPackage) {
        if (classInPackage.endsWith("." + className)) {
          sourceCodeBuilder.addImport(classInPackage);
          break;
        }
      }
    }
  }
  /**
   * Extracts and add to the source code information expressed in the given condition text.
   *
   * @param method documented executable the guard belongs to
   * @param conditionText the condition text
   * @param sourceCodeBuilder {@code SourceCodeBuilder} object that wraps the source code
   */
  private static void addConditionCodeInformation(
      DocumentedExecutable method, String conditionText, SourceCodeBuilder sourceCodeBuilder) {
    String substitutedText = substituteArgs(sourceCodeBuilder, method, conditionText);
    sourceCodeBuilder.addCondition(substitutedText);
    importClassesInInstanceOf(method, sourceCodeBuilder, substitutedText);
  }

  /**
   * Substitutes in the condition the actual arguments names, since Toradocu-generated conditions
   * refer to the nth argument with the {@code args[n]} notation.
   *
   * @param sourceCodeBuilder {@code SourceCodeBuilder} object that wraps the source code
   * @param method documented executable the guard belongs to
   * @param text condition text
   * @return condition text with argument names substituted
   */
  private static String substituteArgs(
      SourceCodeBuilder sourceCodeBuilder, DocumentedExecutable method, String text) {
    if (text != null) {
      final String ARGS_REGEX = "args\\[([0-9])\\]";
      java.util.regex.Matcher argsMatcher = Pattern.compile(ARGS_REGEX).matcher(text);
      while (argsMatcher.find()) {
        int argIndex = Integer.valueOf(argsMatcher.group(1));
        String parameter = method.getParameters().get(argIndex).getName();
        text = text.replace(argsMatcher.group(0), parameter);
        DocumentedParameter argument = method.getParameters().get(argIndex);
        if (argument.asReflectionParameter().isVarArgs()) {
          sourceCodeBuilder.addVarArgArgument(argument.toString());
        } else {
          sourceCodeBuilder.addArgument(argument.toString());
        }
      }
    }

    return text;
  }
}
