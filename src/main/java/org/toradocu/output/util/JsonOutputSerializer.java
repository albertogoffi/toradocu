package org.toradocu.output.util;

import com.google.gson.*;
import java.lang.reflect.*;

/** Created by arianna on 29/06/17. */
public class JsonOutputSerializer implements JsonSerializer<JsonOutput> {

  @Override
  public JsonElement serialize(
      JsonOutput src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jObj = (JsonObject) new GsonBuilder().create().toJsonTree(src);
    if (src.containingClass.qualifiedName.equals(src.name)) {
      jObj.remove("returnType");
    }
    return jObj;
  }
}
