package org.toradocu.generator;

import static org.toradocu.Toradocu.configuration;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
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
   * @param specifications the specifications that created aspects will check at runtime. Must not
   *     be null.
   */
  public static void createAspects(Map<DocumentedExecutable, OperationSpecification> specifications)
      throws IOException {
    Checks.nonNullParameter(specifications, "specifications");

    // Create output directory where aspects are saved.
    final String aspectsOutputDir = configuration.getAspectsOutputDir();
    final boolean outputDirCreationSucceeded = createOutputDir(aspectsOutputDir);
    if (!outputDirCreationSucceeded || specifications.isEmpty()) {
      return;
    }

    // Create Junit tests aspect.
    final String inputAspectPath = "/" + configuration.getJUnitTestCaseAspect();
    final String junitAspect = configuration.getJUnitTestCaseAspect();
    final String outputAspectPath = aspectsOutputDir + File.separator + junitAspect;
    final String testClass = Toradocu.configuration.getTestClass();
    final String withinDeclaration = " && within(" + testClass + ")";
    createJunitTestsAspect(inputAspectPath, outputAspectPath, withinDeclaration);

    // Create oracle aspects.
    final List<String> createdAspectNames = new ArrayList<>();
    final String junitAspectName = junitAspect.substring(0, junitAspect.lastIndexOf("."));
    createdAspectNames.add(junitAspectName);
    int aspectNumber = 1;
    for (DocumentedExecutable method : specifications.keySet()) {
      OperationSpecification specification = specifications.get(method);
      if (!specification.isEmpty()) {
        String aspectName = "Aspect_" + aspectNumber++;
        createAspect(method, specification, aspectName);
        createdAspectNames.add(aspectName);
      }
    }

    // Create aop.xml file needed by AspectJ. Aop file lists available aspects.
    createAopXml(aspectsOutputDir, createdAspectNames);
  }

  /**
   * Creates the directory specified by {@code aspectsOutputDir}.
   *
   * @param aspectsOutputDir the directory to be created
   * @return {@code true} if the creation succeeded, {@code false} otherwise
   */
  private static boolean createOutputDir(String aspectsOutputDir) {
    boolean creationSucceeded;
    final File outputDir = new File(aspectsOutputDir);
    if (outputDir.exists()) {
      log.error("Directory where to store aspects already exists: " + aspectsOutputDir);
      creationSucceeded = true;
    } else {
      creationSucceeded = outputDir.mkdirs();
      if (!creationSucceeded) {
        log.error("Error during creation of directory: " + aspectsOutputDir);
      }
    }
    return creationSucceeded;
  }

  private static void createJunitTestsAspect(
      String inputAspectPath, String outputAspectPath, String withinDeclaration)
      throws IOException {
    final InputStream aspect = Object.class.getResourceAsStream(inputAspectPath);
    CompilationUnit cu = JavaParser.parse(aspect);
    cu.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("advice"))
        .ifPresent(
            m ->
                m.getAnnotation(0)
                    .ifStringLiteralExpr(e -> e.setValue(e.getValue() + withinDeclaration)));

    try (FileOutputStream output = new FileOutputStream(new File(outputAspectPath))) {
      output.write(cu.toString().getBytes());
    } catch (IOException e) {
      log.error("Error during creation of file: " + outputAspectPath, e);
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

    final InputStream aspectTemplate =
        Object.class.getResourceAsStream("/" + configuration.getAspectTemplate());
    CompilationUnit cu = JavaParser.parse(aspectTemplate);

    // Set the correct name to the newly created aspect class. Default name is "Aspect_Template".
    cu.findFirst(
            ClassOrInterfaceDeclaration.class, c -> c.getNameAsString().equals("Aspect_Template"))
        .ifPresent(c -> c.setName(aspectName));

    new MethodChangerVisitor().visit(cu, Pair.of(method, specification));

    final String aspectPath =
        configuration.getAspectsOutputDir() + File.separator + aspectName + ".java";
    try (FileOutputStream output = new FileOutputStream(new File(aspectPath))) {
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
