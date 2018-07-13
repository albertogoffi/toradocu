package org.toradocu.translator;

import static java.util.stream.Collectors.toList;

import edu.stanford.nlp.semgraph.SemanticGraph;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.Comment;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedParameter;
import org.toradocu.extractor.ParamTag;

/**
 * Collects all the Java elements that can be used for the condition translation. Java elements are
 * collected through Java reflection.
 */
public class JavaElementsCollector {

  /**
   * Collects all the Java code elements that can be used for the condition translation. The code
   * elements are collected using reflection starting from the given method.
   *
   * @param documentedExecutable the method from which to start to collect the code elements
   * @return the collected code elements
   */
  public static Set<CodeElement<?>> collect(DocumentedExecutable documentedExecutable) {
    Set<CodeElement<?>> collectedElements = new LinkedHashSet<>();
    final Class<?> containingClass = documentedExecutable.getDeclaringClass();

    // Add the containing class.
    collectedElements.add(containingClassOf(documentedExecutable));

    // Add the parameters of the executable member.
    collectedElements.addAll(parametersOf(documentedExecutable));

    // Add fields of the containing class.
    collectedElements.addAll(fieldsOf(containingClass));

    // Add methods of the containing class (all but the method corresponding to
    // documentedExecutable).
    collectedElements.addAll(methodsOf(containingClass, documentedExecutable));

    return collectedElements;
  }

  // Executable member is ignored and not included in the returned list of methods.
  private static List<CodeElement<?>> methodsOf(
      Class<?> containingClass, DocumentedExecutable documentedExecutable) {
    final List<Method> methods = new ArrayList<>();
    Collections.addAll(methods, containingClass.getMethods());
    final Executable executable = documentedExecutable.getExecutable();
    if (executable instanceof Method) {
      Method method = (Method) executable;
      methods.remove(method);
    }
    List<Class<?>> inScopeTypes = collectInScopeTypes(documentedExecutable);
    methods.removeIf(method -> !invokableWithParameters(method, inScopeTypes));

    List<CodeElement<?>> codeElements = new ArrayList<>();
    for (Method method : methods) {
      if (Modifier.isStatic(method.getModifiers())) {
        codeElements.add(new StaticMethodCodeElement(method));
      } else if (!documentedExecutable.isConstructor()) {
        codeElements.add(new MethodCodeElement(Configuration.RECEIVER, method));
      }
    }
    return codeElements;
  }

  private static List<Class<?>> collectInScopeTypes(DocumentedExecutable documentedExecutable) {
    final List<Class<?>> availableTypes = new ArrayList<>();
    final Class<?> containingClass = documentedExecutable.getDeclaringClass();

    // Add parameters of the executable member.
    Collections.addAll(availableTypes, documentedExecutable.getExecutable().getParameterTypes());

    // Add target class.
    availableTypes.add(containingClass);

    // Add target class' fields.
    for (Field field : containingClass.getFields()) {
      availableTypes.add(field.getType());
    }

    return availableTypes;
  }

  private static List<FieldCodeElement> fieldsOf(Class<?> aClass) {
    return Arrays.stream(aClass.getFields())
        .map(field -> new FieldCodeElement(Configuration.RECEIVER, field))
        .collect(toList());
  }

  private static List<ParameterCodeElement> parametersOf(
      DocumentedExecutable documentedExecutable) {
    List<ParameterCodeElement> paramCodeElements = new ArrayList<>();

    // The first two parameters of enum constructors are synthetic and must be removed to reflect
    // the source code.
    final List<DocumentedParameter> parameters = documentedExecutable.getParameters();
    if (documentedExecutable.getDeclaringClass().isEnum() && documentedExecutable.isConstructor()) {
      parameters.remove(0);
      parameters.remove(0);
    }

    int i = 0;
    HashMap<String, Integer> countIds = new HashMap<>();
    for (DocumentedParameter parameter : parameters) {
      final Parameter reflectionParam = parameter.asReflectionParameter();
      final String parameterName = parameter.getName();
      final Set<String> identifiers =
          extractIdentifiersFromParamTags(documentedExecutable, parameterName);
      for (String id : identifiers) {
        Integer oldValue = countIds.getOrDefault(id, -1);
        countIds.put(id, ++oldValue);
      }

      // TODO Generate code ids.
      ParameterCodeElement param =
          new ParameterCodeElement(reflectionParam, parameterName, i++, identifiers);
      paramCodeElements.add(param);
    }

    // TODO Create a parameter code element directly with unique identifiers, thus removing
    // mergeIdentifiers() and related methods in ParameterCodeElement.
    // Select only valid identifiers for the parameters, i.e. the unique ones (count in map is 0)
    for (ParameterCodeElement p : paramCodeElements) {
      //      Set<String> ids = p.getOtherIdentifiers();
      //      for (Map.Entry<String, Integer> countId : countIds.entrySet()) {
      //        String identifier = countId.getKey();
      //        if (ids.contains(identifier) && countId.getValue() > 0)
      //          p.removeIdentifier(identifier);
      //      }
      p.mergeIdentifiers();
    }

    return paramCodeElements;
  }

  private static ClassCodeElement containingClassOf(DocumentedExecutable documentedExecutable) {
    return new ClassCodeElement(documentedExecutable.getDeclaringClass());
  }

  /**
   * For the parameter in input, find its param tag in the method's Javadoc and produce the
   * SemanticGraphs of the comment. For every graph, keep the root as identifier.
   *
   * @param method the DocumentedExecutable which the parameter belongs to
   * @param param the parameter
   * @return the extracted ids
   */
  private static Set<String> extractIdentifiersFromParamTags(
      DocumentedExecutable method, String param) {
    List<ParamTag> paramTags = method.paramTags();
    Set<String> ids = new HashSet<>();
    for (ParamTag pt : paramTags) {
      String paramName = pt.getParameter().getName();
      String originalComment = pt.getComment().getText();
      int semicolon = originalComment.indexOf(";");
      if (semicolon != -1)
        // Often param comments have a semicolon followed by a further description,
        // not useful for our purpose here and possibly affecting the semantic graph
        originalComment = originalComment.substring(0, semicolon);

      if (paramName.equals(param)) {
        List<SemanticGraph> sgs =
            Parser.parse(new Comment(originalComment), method)
                .stream()
                .map(PropositionSeries::getSemanticGraph)
                .collect(toList());
        for (SemanticGraph sg : sgs) {
          ids.add(sg.getFirstRoot().word());
        }
      }
    }
    return ids;
  }

  private static boolean invokableWithParameters(Method method, List<Class<?>> inScopeTypes) {
    final List<? extends Class<?>> methodParamTypes =
        Arrays.stream(method.getParameters()).map(Parameter::getType).collect(toList());
    return inScopeTypes.containsAll(methodParamTypes);
  }
}
