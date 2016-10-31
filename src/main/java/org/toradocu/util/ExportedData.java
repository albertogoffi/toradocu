package org.toradocu.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.extractor.ThrowsTag;

/**
 * ExportedData holds data about translated conditions that is to be exported to other applications.
 * It allows for the data to be exported as JSON.
 */
public class ExportedData {

  /** Maps each {@code DocumentedMethod} to the (translated) throws tags for it. */
  private Map<String, List<ThrowsTag>> translatedThrowsTags;

  /** Maps each method signature to a {@code DocumentedMethod} object. */
  private Map<String, DocumentedMethod> methods;

  /** Constructs an {@code ExportedData} object with no data to export initially. */
  public ExportedData() {
    translatedThrowsTags = new HashMap<>();
    methods = new HashMap<>();
  }

  /**
   * Constructs an {@code ExportedData} with data to export initialized from the given collection of
   * {@code DocumentedMethod}s.
   *
   * @param methods a collection of methods
   */
  public ExportedData(Collection<? extends DocumentedMethod> methods) {
    this();
    addDataFromMethods(methods);
  }

  /**
   * Takes a collection of {@code DocumentedMethod}s and adds those methods that have nullness
   * constraints along with any that have translated throws tags. Also adds the translated throws
   * tags contained in the methods.
   *
   * @param methods a collection of methods
   */
  public void addDataFromMethods(Collection<? extends DocumentedMethod> methods) {
    for (DocumentedMethod method : methods) {
      boolean usefulMethod = false;
      if (method.getParameters().stream().anyMatch(p -> p.getNullability() != null)) {
        usefulMethod = true;
      }
      for (ThrowsTag tag : method.throwsTags()) {
        if (tag.getCondition().isPresent()) {
          if (!translatedThrowsTags.containsKey(method.getSignature())) {
            translatedThrowsTags.put(method.getSignature(), new ArrayList<>());
          }
          translatedThrowsTags.get(method.getSignature()).add(tag);
          usefulMethod = true;
        }
      }
      if (usefulMethod) {
        this.methods.put(method.getSignature(), method);
      }
    }
  }

  /**
   * Exports the data as a JSON string.
   *
   * @return a JSON string containing the data
   */
  public String asJson() {
    return GsonInstance.gson().toJson(this);
  }
}
