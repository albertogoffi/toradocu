package tcomment

import org.toradocu.extractor.DocumentedMethod
import org.toradocu.extractor.ThrowsTag

/**
 * Translates the tags in the given methods using @tComment algorithm. This method sets
 * [ThrowsTag.condition][ThrowsTag] for each @throws tag of the given methods.
 *
 * @param methods a list of [DocumentedMethod]s whose throws tags has to be translated
 */
fun translate(methods: List<DocumentedMethod>) {
  // Translate @throws comments using @tComment algorithm.
  for (method in methods) {
    val parameters = method.parameters.map { it.name }
    method.throwsTags().forEach { translateTagComment(it, parameters) }
  }
}

/**
 * Translates [tag] comment text into a Java boolean condition.
 *
 * @param tag the tag whose text has to be translated
 * @param parameterNames names of the method's parameters to which [tag] belongs
 */
fun translateTagComment(tag: ThrowsTag, parameterNames: List<String>) {
  val commentWords = tag.getComment().toLowerCase().replace(",", " ").split(" ")
  var condition = ""
  if (commentWords.contains("null")) {
    commentWords.intersect(parameterNames).forEach {
      if (condition.isNotEmpty()) {
        condition += " || "
      }
      condition += "args[${parameterNames.indexOf(it)}]==null"
      if (!commentWords.contains("or") && !commentWords.contains("either")) return@forEach
    }
  }
  tag.setCondition(condition)
}

//fun nullAnyCheck(commentWords: List<String>): PropertyType {
//  for ((index, word) in commentWords.withIndex()) {
//    if (word.equals("null")) {
//      var i = index - 1
//      while (i >= 0 && i >= index - 3) {
//        if (commentWords[i] == "not" || commentWords[i] == "never")
//          return PropertyType .NULL_ANY_EXCEPTION
//        --i
//      }
//      var j = index + 1
//      while (j <= index + 3 && j < commentWords.size) {
//        if (commentWords[j] == "not" || commentWords[i] == "never")
//          return PropertyType.NULL_ANY_EXCEPTION
//        ++j
//      }
//    }
//  }
//  return PropertyType.NULL_NORMAL
//}

//data class Property(val parameter: String, val type: PropertyType, val exception: String = "")
//
//enum class PropertyType {
//  NULL_NORMAL, NULL_ANY_EXCEPTION, NULL_SPECIFIC_EXCEPTION, NULL_UNKNOWN
//}
//
//enum class Tag {
//  PARAM, THROWS
//}
