package org.toradocu.generator;

import static org.toradocu.Toradocu.configuration;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.util.Checks;
import randoop.condition.specification.OperationSpecification;

/**
 * The oracle generator. The method {@code createAspects} of this class creates the aspects for a
 * list of {@code ExecutableMember}.
 */
public class OracleGenerator {

  /** {@code Logger} for this class. */
  private static final Logger log = LoggerFactory.getLogger(OracleGenerator.class);

  /**
   * Creates aspects that check the given {@code specs}. This method creates one aspect for each
   * method with specifications.
   *
   * <p>Created aspects can be used to embed oracles in existing test suites.
   *
   * @param specs the specifications that created aspects will check at runtime. Must not be null.
   */
  public static void createAspects(Map<DocumentedExecutable, OperationSpecification> specs) {
    Checks.nonNullParameter(specs, "specs");

    if (!configuration.isOracleGenerationEnabled()) {
      log.info("Oracle generator disabled: aspect generation skipped.");
      return;
    }

    String aspectDir = configuration.getAspectsOutputDir();
    new File(aspectDir).mkdirs();

    if (specs.isEmpty()) {
      return;
    }

    final String junitAspect = configuration.getJUnitTestCaseAspect();
    final String aspectPath = configuration.getAspectsOutputDir() + File.separator + junitAspect;
    try {
      addWithinDeclarationToPointcut(aspectPath);
    } catch (IOException e) {
      return;
    }

    // Create aspects.
    final List<String> createdAspectNames = new ArrayList<>();
    final String junitAspectName = junitAspect.substring(0, junitAspect.lastIndexOf("."));
    createdAspectNames.add(junitAspectName);
    int aspectNumber = 1;
    for (DocumentedExecutable method : specs.keySet()) {
      String aspectName = "Aspect_" + aspectNumber++;
      createAspect(method, specs.get(method), aspectName);
      createdAspectNames.add(aspectName);
    }

    createAopXml(aspectDir, createdAspectNames);
  }

  // Add "within" declaration to pointcut definition.
  private static void addWithinDeclarationToPointcut(String aspectPath) throws IOException {
    try (InputStreamReader template =
            new InputStreamReader(
                Object.class.getResourceAsStream("/" + configuration.getJUnitTestCaseAspect()));
        FileOutputStream output = new FileOutputStream(new File(aspectPath))) {
      CompilationUnit cu = JavaParser.parse(template);
      new JUnitTestCaseAspectChangerVisitor().visit(cu, null);
      output.write(cu.toString().getBytes());
    } catch (IOException e) {
      log.error("Oracle generation stopped: Impossible to create file " + aspectPath);
      throw e;
    }
  }

  /**
   * Creates a new aspect for the given {@code method}.
   *
   * @param method method for which an aspect will be created, must not be null
   * @param specification the specs the created aspect has to check, must not be null
   * @param aspectName name of the file where the newly created aspect is saved, must not be null
   */
  private static void createAspect(
      DocumentedExecutable method, OperationSpecification specification, String aspectName) {
    Checks.nonNullParameter(method, "method");
    Checks.nonNullParameter(specification, "specification");
    Checks.nonNullParameter(aspectName, "aspectName");

    String aspectPath = configuration.getAspectsOutputDir() + File.separator + aspectName;

    try (InputStreamReader template =
            new InputStreamReader(
                Object.class.getResourceAsStream("/" + configuration.getAspectTemplate()));
        FileOutputStream output = new FileOutputStream(new File(aspectPath + ".java"))) {
      CompilationUnit cu = JavaParser.parse(template);

      new MethodChangerVisitor().visit(cu, Pair.of(method, specification));
      new ClassChangerVisitor().visit(cu, aspectName);
      output.write(cu.toString().getBytes());
    } catch (IOException e) {
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
      content.append("\t\t<aspect name=\"").append(aspect).append("\"/>\n");
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
