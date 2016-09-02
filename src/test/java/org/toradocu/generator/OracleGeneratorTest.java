package org.toradocu.generator;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.toradocu.Toradocu;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.Parameter;
import org.toradocu.extractor.ThrowsTag;
import org.toradocu.extractor.Type;

import com.beust.jcommander.JCommander;

public class OracleGeneratorTest {

  private final Type npe = new Type("java.lang.NullPointerException");

  @Test
  public void oracleGeneratorTest() throws Exception {
    String[] args =
        new String[] {"--target-class", "example.util.Arrays", "--oracle-generation", "true"};
    new JCommander(Toradocu.CONFIGURATION, args);

    OracleGenerator oracleGenerator = new OracleGenerator();
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(new Parameter(new Type("java.lang.Integer[]"), "array"));
    parameters.add(new Parameter(new Type("java.lang.Integer"), "element"));
    parameters.add(new Parameter(new Type("java.lang.String[]"), "names"));
    List<ThrowsTag> tags = new ArrayList<>();
    ThrowsTag tag = new ThrowsTag(npe, "if array or element is null");
    tag.setCondition("args[0]==null || args[1]==null");
    tags.add(tag);
    DocumentedMethod method =
        new DocumentedMethod(
            new Type("example.util.Arrays"),
            "count",
            new Type("java.lang.Integer"),
            parameters,
            false,
            tags);

    List<DocumentedMethod> methods = new ArrayList<>();
    methods.add(method);

    oracleGenerator.createAspects(methods);

    //TODO: check created aspects

    //		FileUtils.deleteDirectory(new File(Toradocu.CONFIGURATION.getAspectsOutputDir()));
  }
}
