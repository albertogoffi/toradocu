package org.toradocu.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  public static boolean isSpecCompilable(DocumentedExecutable method, Guard guard) {
    FakeSourceBuilder fakeSourceBuilder = addCommonInfo(method);
    addGuardInformation(method, guard, fakeSourceBuilder);
    String sourceCode = fakeSourceBuilder.buildSource();
    try {
      compileSource(sourceCode);
    } catch (Exception e) {
      log.info(
          "The following specification was generated but discarded:\n"
              + guard.getConditionText()
              + "\n"
              + e.getMessage()
              + "\n");
      return false;
    }
    return true;
  }

  public static boolean isPostSpecCompilable(
      DocumentedExecutable method, Guard guard, Property property) {
    List<String> classpath = new ArrayList<>();
    for (URL url : Configuration.INSTANCE.classDirs) {
      classpath.add(url.getPath());
    }
    FakeSourceBuilder fakeSourceBuilder = addCommonInfo(method);
    String methodReturnType = method.getReturnType().getType().getTypeName();
    if (!methodReturnType.equals("void")) {
      fakeSourceBuilder.addArgument(methodReturnType, Configuration.RETURN_VALUE);
    }
    addGuardInformation(method, guard, fakeSourceBuilder);
    addPropertyInformation(method, property, fakeSourceBuilder);
    String sourceCode = fakeSourceBuilder.buildSource();
    try {
      compileSource(sourceCode);
    } catch (Exception e) {
      log.info(
          "The following specification was generated but discarded:\n"
              + guard.getConditionText()
              + " ? "
              + property.getConditionText()
              + "\n"
              + e.getMessage()
              + "\n");
      return false;
    }
    return true;
  }

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

  private static FakeSourceBuilder addCommonInfo(DocumentedExecutable method) {
    FakeSourceBuilder fakeSourceBuilder = new FakeSourceBuilder();
    Class<?> declaringClass = method.getDeclaringClass();
    Class<?>[] interfaces = declaringClass.getInterfaces();
    //TODO extract "implements ..." and add to source code

    fakeSourceBuilder.addImport(declaringClass.getName());
    fakeSourceBuilder.addArgument(declaringClass.getName(), Configuration.RECEIVER);
    fakeSourceBuilder.copyClassTypeArguments(declaringClass.getTypeParameters());
    fakeSourceBuilder.copyTypeArguments(method.getExecutable().getTypeParameters());
    return fakeSourceBuilder;
  }

  private static void addGuardInformation(
      DocumentedExecutable method, Guard guard, FakeSourceBuilder fakeSourceBuilder) {
    String guardText = substituteArgs(fakeSourceBuilder, method, guard.getConditionText());
    fakeSourceBuilder.addCondition(guardText);
    Matcher matcher = Pattern.compile(" instanceof ([A-Z][A-Za-z]+)").matcher(guardText);
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
          fakeSourceBuilder.addImport(classInPackage);
          break;
        }
      }
    }
  }

  private static void addPropertyInformation(
      DocumentedExecutable method, Property property, FakeSourceBuilder fakeSourceBuilder) {
    String propertyText = substituteArgs(fakeSourceBuilder, method, property.getConditionText());
    fakeSourceBuilder.addCondition(propertyText);
  }

  private static String substituteArgs(
      FakeSourceBuilder fakeSourceBuilder, DocumentedExecutable method, String text) {
    if (text != null) {
      final String ARGS_REGEX = "args\\[([0-9])\\]";
      java.util.regex.Matcher argsMatcher = Pattern.compile(ARGS_REGEX).matcher(text);
      while (argsMatcher.find()) {
        int argIndex = Integer.valueOf(argsMatcher.group(1));
        String parameter = method.getParameters().get(argIndex).getName();
        text = text.replace(argsMatcher.group(0), parameter);
        DocumentedParameter argument = method.getParameters().get(argIndex);
        if (argument.asReflectionParameter().isVarArgs()) {
          fakeSourceBuilder.addVarArgArgument(argument.toString());
        } else {
          fakeSourceBuilder.addArgument(argument.toString());
        }
      }
    }

    return text;
  }
}
