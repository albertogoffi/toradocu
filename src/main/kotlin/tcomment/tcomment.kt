package tcomment

import org.toradocu.extractor.DocumentedMethod
import org.toradocu.extractor.ParamTag
import org.toradocu.extractor.ThrowsTag

/**
 * Translates the tags in the given methods using @tComment algorithm. This method sets
 * [ThrowsTag.condition][ThrowsTag] for each @throws tag of the given methods.
 *
 * @param methods a list of [DocumentedMethod]s whose throws tags has to be translated
 */
fun translate(methods: List<DocumentedMethod>) {
  // Translate @param and @throws comments using @tComment algorithm.
  for (method in methods) {
    val parameters = method.parameters.map { it.name }
    method.paramTags().forEach { translateTagComment(it, parameters) }
    method.throwsTags().forEach { translateTagComment(it, parameters) }
  }
}

/**
 * Translates [tag] comment text into a Java boolean condition.
 *
 * @param tag the tag whose text has to be translated
 * @param parameterNames names of the method's parameters to which [tag] belongs
 */
private fun translateTagComment(tag: ParamTag, parameterNames: List<String>) {
  val parameterName = tag.parameter().name
  val condition = if (mustBeNotNull(tag.comment)) {
    "(args[${parameterNames.indexOf(parameterName)}]==null)==false"
  } else {
    ""
  }
  tag.setCondition(condition)
}

/**
 * Translates [tag] comment text into a Java boolean condition.
 *
 * @param tag the tag whose text has to be translated
 * @param parameterNames names of the method's parameters to which [tag] belongs
 */
private fun translateTagComment(tag: ThrowsTag, parameterNames: List<String>) {
  val commentWords = getWords(tag.comment)
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

/**
 * Checks whether [comment] is expressing a non-null pre-condition, for example: "must not be null".
 *
 * @param comment the words composing the comment
 * @return true if the comment is expressing a non-null pre-condition, false otherwise
 */
private fun mustBeNotNull(comment: String): Boolean {
  val commentWords = getWords(comment)
  for ((index, word) in commentWords.withIndex()) {
    if (word == "null") {
      var i = index - 1
      while (i >= 0 && i >= index - 3) {
        if (commentWords[i] == "not" || commentWords[i] == "never")
          return true
        --i
      }
      var j = index + 1
      while (j <= index + 3 && j < commentWords.size) {
        if (commentWords[j] == "not" || commentWords[j] == "never")
          return true
        ++j
      }
    }
  }
  return false
}

private fun getWords(sentence: String) = sentence.toLowerCase().replace(",", " ").split(" ")
