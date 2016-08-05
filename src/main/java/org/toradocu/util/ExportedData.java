package org.toradocu.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.toradocu.extractor.DocumentedMethod;
import org.toradocu.translator.TranslatedThrowsTag;

/**
 * ExportedData holds data about translated conditions that is to be exported to other applications.
 * It allows for the data to be exported as JSON.
 */
public class ExportedData {
	
	/** Maps each {@code DocumentedMethod} to the (translated) throws tags for it. */
	private Map<String, List<TranslatedThrowsTag>> methodToThrowsTags;
	
	/** Maps each method signature to a {@code DocumentedMethod} object. */
	private Map<String, DocumentedMethod> methods;
	
	/**
	 * Constructs an {@code ExportedData} object with no data to export initially.
	 */
	public ExportedData() {
		methodToThrowsTags = new HashMap<>();
		methods = new HashMap<>();
	}
	
	/**
	 * Adds all throws tags from the given collection to the data to export.
	 * 
	 * @param tags a collection of throws tags to export
	 */
	public void addTranslatedThrowsTags(Collection<? extends TranslatedThrowsTag> tags) {
		for (TranslatedThrowsTag tag : tags) {
			addTranslatedThrowsTag(tag);
		}
	}
	
	/**
	 * Adds the following tag to the data to export.
	 * 
	 * @param tag a throws tag to export
	 */
	public void addTranslatedThrowsTag(TranslatedThrowsTag tag) {
		if (!methodToThrowsTags.containsKey(tag.getMethod())) {
			methodToThrowsTags.put(tag.getMethod().getSignature(), new ArrayList<>());
		}
		methodToThrowsTags.get(tag.getMethod().getSignature()).add(tag);
		methods.put(tag.getMethod().getSignature(), tag.getMethod());
	}
	
	/**
	 * Takes a collection of {@code DocumentedMethod}s and adds those methods that have
	 * nullness constraints.
	 * 
	 * @param methods a collection of methods
	 */
	public void addMethodsWithNullnessConstraints(Collection<? extends DocumentedMethod> methods) {
		for (DocumentedMethod method : methods) {
			if (method.getParameters().stream().anyMatch(p -> p.getNullability() != null)) {
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
