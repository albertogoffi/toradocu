package org.toradocu.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;

public class OracleGenerator {

  /** {@code Logger} for this class. */
  private final Logger LOG = LoggerFactory.getLogger(OracleGenerator.class);

  /**
   * Creates one aspect for each method in the given {@code methods} list
   *
   * @param methods the {@code List} of methods for which create aspects
   */
  public void createAspects(List<DocumentedMethod> methods) {
    if (!Toradocu.CONFIGURATION.isOracleGenerationEnabled()) {
      LOG.info("Oracle generator disabled: skipped aspect generation.");
      return;
    }

    if (methods.isEmpty()) {
      return;
    }

    createFolder(Toradocu.CONFIGURATION.getAspectsOutputDir());

    List<String> createdAspectNames = new ArrayList<>();
    int aspectNumber = 1;
    for (DocumentedMethod method : methods) {
      String aspectName = "Aspect_" + aspectNumber;
      createAspect(method, aspectName);
      createdAspectNames.add(aspectName);
      aspectNumber++;
    }
    createAOPXml(Toradocu.CONFIGURATION.getAspectsOutputDir(), createdAspectNames);
  }

  /**
   * Creates a new aspect for the given {@code method}.
   *
   * @param method method for which an aspect will be created
   * @param aspectName name of the file where the newly created aspect is saved
   */
  private void createAspect(DocumentedMethod method, String aspectName) {
    Objects.requireNonNull(method, "parameter method must not be null");
    Objects.requireNonNull(aspectName, "parameter aspectName must not be null");

    String aspectPath = Toradocu.CONFIGURATION.getAspectsOutputDir() + File.separator + aspectName;

    try (InputStream template =
            getClass().getResourceAsStream("/" + Toradocu.CONFIGURATION.getAspectTemplate());
        FileOutputStream output = new FileOutputStream(new File(aspectPath + ".java"))) {
      CompilationUnit cu = JavaParser.parse(template);

      new MethodChangerVisitor(method).visit(cu, null);
      String newAspect = cu.toString();
      newAspect = newAspect.replace("public class Aspect_Template", "public class " + aspectName);
      output.write(newAspect.getBytes());
    } catch (IOException | ParseException e) {
      LOG.error("Error during aspect creation.", e);
    }
  }

  /**
   * Creates the file aop.xml needed by Aspectj compiler for the instrumentation. The file aop.xml
   * lists all the aspects that must be woven into a target source code.
   *
   * @param folder where the file aop.xml is created
   * @param createdAspects list of the aspects to be mentioned in the aop.xml file
   */
  private void createAOPXml(String folder, List<String> createdAspects) {
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
      LOG.error("Error while creating aop.xml file.", e);
    }
  }

  /**
   * Creates a new folder with the given {@code folderPath} if it does not exist.
   *
   * @param folderPath the path of the folder to be created
   */
  private void createFolder(String folderPath) {
    File folderToCreate = new File(folderPath);
    if (!folderToCreate.exists()) {
      folderToCreate.mkdir();
    }
  }
}
