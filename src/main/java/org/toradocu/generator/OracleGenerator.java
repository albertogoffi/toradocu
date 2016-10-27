package org.toradocu.generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.util.Checks;

/**
 * The oracle generator. The method {@code createAspects} of this class creates the aspects for a
 * list of {@code DocumentedMethod}.
 */
public class OracleGenerator {

  /** {@code Logger} for this class. */
  private static final Logger log = LoggerFactory.getLogger(OracleGenerator.class);

  /**
   * Creates one aspect for each method in the given {@code methods} list if the method has at least
   * one comment translated by the condition translator.
   *
   * @param methods the {@code List} of methods to create aspects for. Must not be null.
   */
  public static void createAspects(List<DocumentedMethod> methods) {
    if (!Toradocu.configuration.isOracleGenerationEnabled()) {
      log.info("Oracle generator disabled: skipped aspect generation.");
      return;
    }

    if (methods.isEmpty()) {
      return;
    }

    String aspectDir = Toradocu.configuration.getAspectsOutputDir();
    new File(aspectDir).mkdir();

    List<String> createdAspectNames = new ArrayList<>();
    int aspectNumber = 1;
    for (DocumentedMethod method : methods) {
      for (ThrowsTag throwTag : method.throwsTags()) {
        // Create an aspect for each method that has at least one translated comment (condition)
        if (!throwTag.getCondition().orElse("").isEmpty()) {
          String aspectName = "Aspect_" + aspectNumber;
          createAspect(method, aspectName);
          createdAspectNames.add(aspectName);
          aspectNumber++;
          break;
        }
      }
    }
    createAopXml(aspectDir, createdAspectNames);
  }

  /**
   * Creates a new aspect for the given {@code method}.
   *
   * @param method method for which an aspect will be created
   * @param aspectName name of the file where the newly created aspect is saved
   * @throws NullPointerException if {@code method} or {@code aspectName} is null
   */
  private static void createAspect(DocumentedMethod method, String aspectName) {
    Checks.nonNullParameter(method, "method");
    Checks.nonNullParameter(aspectName, "aspectName");

    String aspectPath = Toradocu.configuration.getAspectsOutputDir() + File.separator + aspectName;

    try (InputStream template =
            Object.class.getResourceAsStream("/" + Toradocu.configuration.getAspectTemplate());
        FileOutputStream output = new FileOutputStream(new File(aspectPath + ".java"))) {
      CompilationUnit cu = JavaParser.parse(template);

      new MethodChangerVisitor().visit(cu, method);
      new ClassChangerVisitor().visit(cu, aspectName);
      output.write(cu.toString().getBytes());
    } catch (IOException | ParseException e) {
      log.error("Error during aspect creation.", e);
    }
  }

  /**
   * Creates the file aop.xml needed by AspectJ compiler for the instrumentation. The file aop.xml
   * lists all the aspects that must be woven into a target source code.
   *
   * @param folder where the file aop.xml is created
   * @param createdAspects list of the aspects to be mentioned in the aop.xml file
   */
  private static void createAopXml(String folder, List<String> createdAspects) {
    final String HEADER =
        "<aspectj>\n\t<weaver options=\"-verbose -showWeaveInfo\"/>\n\t<aspects>\n";
    final String FOOTER = "\t</aspects>\n</aspectj>";
    StringBuilder content = new StringBuilder(HEADER);
    for (String aspect : createdAspects) {
      content.append("\t\t<aspect name=\"" + aspect + "\"/>\n");
    }
    content.append(FOOTER);
    try (FileOutputStream output =
        new FileOutputStream(new File(folder + File.separator + "aop.xml"))) {
      output.write(content.toString().getBytes());
    } catch (IOException e) {
      log.error("Error while creating aop.xml file.", e);
      System.exit(1);
    }
  }
}
