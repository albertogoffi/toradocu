package org.toradocu.translator;

import static java.util.stream.Collectors.toList;

import edu.stanford.nlp.semgraph.SemanticGraph;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import org.toradocu.extractor.ExecutableMember;
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
   * @param executableMember the method from which to start to collect the code elements
   * @return the collected code elements
   */
  public static Set<CodeElement<?>> collect(ExecutableMember executableMember) {
    Set<CodeElement<?>> collectedElements = new LinkedHashSet<>();
    final Class<?> containingClass = executableMember.getContainingClass();

    // Add the containing class.
    collectedElements.add(containingClassOf(executableMember));

    // Add the parameters of the executable member.
    collectedElements.addAll(parametersOf(executableMember));

    // Add fields of the containing class.
    collectedElements.addAll(fieldsOf(containingClass));

    // Add methods of the containing class (all but the method corresponding to executableMember).
    collectedElements.addAll(methodsOf(containingClass, executableMember));

    return collectedElements;
  }

  // Executable member is ignored and not included in the returned list of methods.
  private static List<CodeElement<?>> methodsOf(
      Class<?> containingClass, ExecutableMember executableMember) {
    final List<Method> methods = new ArrayList<>();
    Collections.addAll(methods, containingClass.getMethods());
    final Executable executable = executableMember.getExecutable();
    if (executable instanceof Method) {
      Method method = (Method) executable;
      methods.remove(method);
    }
    List<Class<?>> inScopeTypes = collectInScopeTypes(executableMember);
    methods.removeIf(method -> !invokableWithParameters(method, inScopeTypes));

    List<CodeElement<?>> codeElements = new ArrayList<>();
    for (Method method : methods) {
      if (Modifier.isStatic(method.getModifiers())) {
        codeElements.add(new StaticMethodCodeElement(method));
      } else if (!executableMember.isConstructor()) {
        codeElements.add(new MethodCodeElement("target", method));
      }
    }
    return codeElements;
  }

  private static List<Class<?>> collectInScopeTypes(ExecutableMember executableMember) {
    final List<Class<?>> availableTypes = new ArrayList<>();
    final Class<?> containingClass = executableMember.getContainingClass();

    // Add parameters of the executable member.
    Collections.addAll(availableTypes, executableMember.getExecutable().getParameterTypes());

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
        .map(field -> new FieldCodeElement("target", field))
        .collect(toList());
  }

  private static List<ParameterCodeElement> parametersOf(ExecutableMember executableMember) {
    List<ParameterCodeElement> paramCodeElements = new ArrayList<>();

    // The first two parameters of enum constructors are synthetic and must be removed to reflect
    // the source code.
    final List<org.toradocu.extractor.Parameter> parameters = executableMember.getParameters();
    if (executableMember.getContainingClass().isEnum() && executableMember.isConstructor()) {
      parameters.remove(0);
      parameters.remove(0);
    }

    int i = 0;
    HashMap<String, Integer> countIds = new HashMap<>();
    for (org.toradocu.extractor.Parameter parameter : parameters) {
      final Parameter reflectionParam = parameter.asReflectionParameter();
      final String parameterName = parameter.getName();
      final Set<String> identifiers =
          extractIdentifiersFromParamTags(executableMember, parameterName);
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
      Set<String> ids = p.getOtherIdentifiers();
      for (Map.Entry<String, Integer> countId : countIds.entrySet()) {
        String identifier = countId.getKey();
        if (ids.contains(identifier) && countId.getValue() > 0) p.removeIdentifier(identifier);
      }
      p.mergeIdentifiers();
    }

    return paramCodeElements;
  }

  private static ClassCodeElement containingClassOf(ExecutableMember executableMember) {
    return new ClassCodeElement(executableMember.getContainingClass());
  }

  /**
   * For the parameter in input, find its param tag in the method's Javadoc and produce the
   * SemanticGraphs of the comment. For every graph, keep the root as identifier.
   *
   * @param method the ExecutableMember which the parameter belongs to
   * @param param the parameter
   * @return the extracted ids
   */
  private static Set<String> extractIdentifiersFromParamTags(
      ExecutableMember method, String param) {
    List<ParamTag> paramTags = method.paramTags();
    Set<String> ids = new HashSet<>();
    for (ParamTag pt : paramTags) {
      String paramName = pt.getParameter().getName();
      if (paramName.equals(param)) {
        List<SemanticGraph> sgs =
            Parser.parse(pt.getComment(), method)
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
