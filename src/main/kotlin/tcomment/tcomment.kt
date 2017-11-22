package tcomment

import org.toradocu.extractor.DocumentedExecutable
import org.toradocu.extractor.ParamTag
import org.toradocu.extractor.ThrowsTag
import randoop.condition.specification.*

/**
 * Translates the @param and @throws/@exception comments in the given executables using @tComment
 * algorithm.
 *
 * @param executables a list of [DocumentedExecutable]s whose tags has to be translated
 */
fun translate(executables: List<DocumentedExecutable>):
    Map<DocumentedExecutable, OperationSpecification> {
  val specs = mutableMapOf<DocumentedExecutable, OperationSpecification>()
  for (method in executables) {
    val parameters = method.parameters.map { it.name }
    val preSpecs = method.paramTags().map { translateTagComment(it, parameters) }
    val throwsSpecs = method.throwsTags().map { translateTagComment(it, parameters) }

    val operation = Operation.getOperation(method.executable)
    val spec = OperationSpecification(operation)
    spec.addParamSpecifications(preSpecs)
    spec.addThrowsSpecifications(throwsSpecs)
    specs.put(method, spec)
  }
  return specs
}

/**
 * Translates [tag] comment text into a Java boolean condition.
 *
 * @param tag the tag whose text has to be translated
 * @param parameterNames names of the method's parameters to which [tag] belongs
 */
private fun translateTagComment(tag: ParamTag, parameterNames: List<String>): PreSpecification {
  val parameterName = tag.parameter.name
  val description = tag.comment.text
  val condition = if (mustBeNotNull(tag.comment.text)) {
    "(args[${parameterNames.indexOf(parameterName)}]==null)==false"
  } else {
    ""
  }
  val guard = Guard(description, condition)
  return PreSpecification(description, guard)
}

/**
 * Translates [tag] comment text into a Java boolean condition.
 *
 * @param tag the tag whose text has to be translated
 * @param parameterNames names of the method's parameters to which [tag] belongs
 */
private fun translateTagComment(tag: ThrowsTag, parameterNames: List<String>): ThrowsSpecification {
  val commentWords = getWords(tag.comment.text)
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
  val guard = Guard(tag.comment.text, condition)
  return ThrowsSpecification(tag.comment.text, guard, tag.exception.name)
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
