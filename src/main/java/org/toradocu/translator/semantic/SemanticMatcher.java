package org.toradocu.translator.semantic;

import com.crtomirmajer.wmd4j.WordMovers;
import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import de.jungblut.math.DoubleVector;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.translator.*;

/**
 * Created by arianna on 29/05/17.
 *
 * <p>Main component. Contains all the methods to compute the {@code SemantichMatch}es for a given
 * class. This implements the "basic" semantic semantic, i.e. the one that uses plain vector sums.
 * Other kinds of matcher will extend this class.
 */
public class SemanticMatcher {

  private boolean stopWordsRemoval;
  private float distanceThreshold;
  private float wmdThreshold;
  private boolean isWmdUsed = false;
  private List<String> stopwords;

  private static GloveRandomAccessReader gloveDB;

  public SemanticMatcher(boolean stopWordsRemoval, float distanceThreshold, float wmdThreshold) {
    this.stopWordsRemoval = stopWordsRemoval;
    this.distanceThreshold = distanceThreshold;
    this.wmdThreshold = wmdThreshold;

    //TODO can this naive list be improved?
    stopwords =
        new ArrayList<>(
            Arrays.asList(
                "true", "false", "the", "a", "if", "for", "be", "have", "this", "do", "not", "of",
                "can", "in", "null", "only", "already", "specify"));

    try {
      gloveDB = setUpGloveBinaryDB();
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      gloveDB = null;
    }
  }

  /**
   * Entry point to run the semantic matching through vector sums. Takes the list of candidates
   * involved in the matching, the method for which calculating the matching, the comment to match
   * and the corresponding proposition. Returns the best matches.
   *
   * @param codeElements list of potentially candidate {@code CodeElement}s
   * @param method the method which the comment to match belongs
   * @param proposition the {@code Proposition} extracted from the comment
   * @param comment comment text
   * @return a map containing the best matches together with the distance computed in respect to the
   *     comment
   * @throws IOException if there were problems reading the vector model
   */
  public LinkedHashMap<CodeElement<?>, Double> runVectorMatch(
      List<CodeElement<?>> codeElements,
      DocumentedExecutable method,
      Proposition proposition,
      String comment)
      throws IOException {

    stopwords.add(method.getDeclaringClass().getSimpleName().toLowerCase());
    try {
      return vectorsMatch(comment, proposition, method, codeElements);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Computes semantic distances through GloVe vectors. The vector corresponding to the comment must
   * be compared with each vector representing a code element among the candidates. Both comment and
   * code elements names must be parsed first (stopwords removal, trailing spaces removal, lowercase
   * normalization). The computed distances are stored in a map that will be filtered at the end of
   * the process.
   *
   * @param comment the comment text for which computing the distances
   * @param proposition the {@code Proposition} extracted from the comment
   * @param method the method which the comment to match belongs
   * @param codeElements ist of potentially candidate {@code CodeElement}s
   * @return a map containing the best matches together with the distance computed in respect to the
   *     comment
   * @throws IOException if there were problems reading the vector model
   */
  private LinkedHashMap<CodeElement<?>, Double> vectorsMatch(
      String comment,
      Proposition proposition,
      DocumentedExecutable method,
      List<CodeElement<?>> codeElements)
      throws IOException {

    String rightSentence = splitInSentences(comment, proposition);
    Set<String> commentWordSet = parseComment(rightSentence);

    if (commentWordSet.size() > 3) {
      // Vectors sum doesn't work well with long comments: use WMD
      if (commentWordSet.size() > 7)
        //Increase the threshold in case of very long comments
        this.wmdThreshold = (float) 6.35;

      return wmdMatch(commentWordSet, proposition, method, codeElements);
    }

    String parsedComment = String.join(" ", commentWordSet).replaceAll("\\s+", " ").trim();

    DoubleVector originalCommentVector = getCommentVector(commentWordSet);

    Map<CodeElement<?>, Double> distances = new LinkedHashMap<>();

    String subject = proposition.getSubject().getSubject().toLowerCase();
    String wordToIgnore = "";
    if (codeElements != null && !codeElements.isEmpty()) {
      for (CodeElement<?> codeElement : codeElements) {
        //For each code element, compute the corresponding vector and compute the distance
        //between it and the comment vector. Store the distances and filter them lately.
        if (codeElement instanceof MethodCodeElement
            && !((MethodCodeElement) codeElement).getReceiver().equals("target")) {
          // if receiver is not target, this is a method invoked from the subject, which for the reason
          // is implicit and will be excluded from the vector computation
          if (subject.lastIndexOf(" ") != -1)
            // in case of composed subject take just the last word (may be the most significant)
            wordToIgnore = subject.substring(subject.lastIndexOf(" ") + 1, subject.length());
          else wordToIgnore = subject;

          DoubleVector codeElementVector =
              getCodeElementVector((MethodCodeElement) codeElement, wordToIgnore);

          Set<String> modifiedComment = new LinkedHashSet<String>(commentWordSet);
          modifiedComment.remove(wordToIgnore);
          DoubleVector modifiedCommentVector = getCommentVector(modifiedComment);

          measureAndStoreDistance(modifiedCommentVector, codeElementVector, codeElement, distances);
        } else {
          DoubleVector methodVector = getCodeElementVector(codeElement, null);
          measureAndStoreDistance(originalCommentVector, methodVector, codeElement, distances);
        }
      }
      return retainMatches(parsedComment, method.getSignature(), distances);
    }
    return null;
  }

  /**
   * If the comment is made of more than one sentence, identify the one containing the proposition
   * (thus the right one to translate).
   *
   * @param comment the whole comment
   * @param proposition the proposition to translate
   * @return sub-sentence containing the proposition
   */
  private String splitInSentences(String comment, Proposition proposition) {
    String rightSentence = comment;

    String[] sentences = comment.split("\\.");
    for (String sentence : sentences) {
      if (sentence.contains(proposition.getPredicate())) rightSentence = sentence;
    }
    return rightSentence;
  }

  /**
   * Measure the cosine distance between two vectors.
   *
   * @param commentVector vector representing the comment
   * @param codeElementVector the vector representing the code element
   * @param codeElement the code element
   * @param distances map where to store the code element together with the distance from the
   *     comment
   */
  private void measureAndStoreDistance(
      DoubleVector commentVector,
      DoubleVector codeElementVector,
      CodeElement<?> codeElement,
      Map<CodeElement<?>, Double> distances) {
    CosineDistance cos = new CosineDistance();
    if (codeElementVector != null && commentVector != null) {
      double dist = cos.measureDistance(codeElementVector, commentVector);
      distances.put(codeElement, dist);
    }
  }

  /**
   * Build the vector representing a code element, made by its name camelCase-splitted
   *
   * @param codeElement the code element
   * @param wordToIgnore word to exclude from the building, if any
   * @return a {@code DoubleVector} representing the code element vector
   * @throws IOException if the GloVe model couldn't be read
   */
  private DoubleVector getCodeElementVector(CodeElement<?> codeElement, String wordToIgnore)
      throws IOException {
    int index;
    DoubleVector codeElementVector = null;
    String name = "";
    if (codeElement instanceof MethodCodeElement)
      name = ((MethodCodeElement) codeElement).getJavaCodeElement().getName();
    else if (codeElement instanceof GeneralCodeElement)
      name = ((GeneralCodeElement) codeElement).getIdentifiers().stream().findFirst().get();
    else return null;
    ArrayList<String> camelId = new ArrayList<String>(Arrays.asList(name.split("(?<!^)(?=[A-Z])")));
    if (camelId.size() > 3) return null;

    String joinedId = String.join(" ", camelId).replaceAll("\\s+", " ").toLowerCase().trim();
    ArrayList<String> parsedId = new ArrayList<String>(parseComment(joinedId));

    if (wordToIgnore != null) parsedId.remove(wordToIgnore);

    for (int i = 0; i < parsedId.size(); i++) {
      DoubleVector v = gloveDB.get(parsedId.get(i).toLowerCase());
      if (this.stopWordsRemoval && this.stopwords.contains(parsedId.get(i).toLowerCase())) continue;
      if (v != null) {
        if (codeElementVector == null) codeElementVector = v;
        else codeElementVector = codeElementVector.add(v);
      }
    }

    return codeElementVector;
  }

  /**
   * Build the vector representing the comment.
   *
   * @param wordComment the {@code Set<String>} of words componing the comment
   * @return a {@code DoubleVector} representing the comment vector
   * @throws IOException if the GloVe model couldn't be read
   */
  private static DoubleVector getCommentVector(Set<String> wordComment) throws IOException {
    DoubleVector commentVector = null;
    Iterator<String> wordIterator = wordComment.iterator();
    while (wordIterator.hasNext()) {
      String word = wordIterator.next();
      if (word != null) {
        DoubleVector v = gloveDB.get(word.toLowerCase());
        if (v != null) {
          if (commentVector == null) commentVector = v;
          else commentVector = commentVector.add(v);
        } else return null;
      }
    }
    return commentVector;
  }

  /**
   * Parse the original tag comment. Special characters are removed. Then the comment is normalized
   * to lower case and lemmatization is applied. As a last step, stopwords are removed.
   *
   * @return the parsed comment in form of array of strings (words retained from the original
   *     comment)
   */
  private Set<String> parseComment(String comment) {
    comment = comment.replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();

    ArrayList<String> wordComment = new ArrayList<String>(Arrays.asList(comment.split(" ")));
    int index = 0;
    List<CoreLabel> lemmas = StanfordParser.lemmatize(comment);
    for (CoreLabel lemma : lemmas) {
      if (lemma != null) {
        if (index < wordComment.size()) {
          wordComment.remove(index);
        }
        wordComment.add(index, lemma.lemma());
      }
      index++;
    }

    return this.removeStopWords(wordComment);
  }

  /**
   * Compute semantic distance through Word Mover's Distance.
   *
   * @param commentWordSet set of {@code String} words composing the comment
   * @param proposition the proposition extracted from the comment that must be translated
   * @param method the method to which the comment belongs
   * @param codeElements list of code elements for which computing the distance
   * @return a map containing the best matches together with the distance computed in respect to the
   *     comment
   */
  private LinkedHashMap<CodeElement<?>, Double> wmdMatch(
      Set<String> commentWordSet,
      Proposition proposition,
      DocumentedExecutable method,
      List<CodeElement<?>> codeElements) {

    this.isWmdUsed = true;
    Map<CodeElement<?>, Double> distances = new LinkedHashMap<>();

    WordMovers wm = null;
    try {
      wm =
          WordMovers.Builder()
              .wordVectors(GloveModelWrapper.getInstance().getGloveTxtVectors())
              .build();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    String subject = proposition.getSubject().getSubject();
    String finalComment = "";
    String finalID = "";

    if (codeElements != null && !codeElements.isEmpty()) {
      for (CodeElement<?> codeElement : codeElements) {
        //For each code element, compute the corresponding vector and compute the distance
        //between it and the comment vector. Store the distances and filter them lately.
        String name = "";
        if (codeElement instanceof MethodCodeElement)
          name = ((MethodCodeElement) codeElement).getJavaCodeElement().getName();
        else if (codeElement instanceof GeneralCodeElement)
          name = ((GeneralCodeElement) codeElement).getIdentifiers().stream().findFirst().get();
        else continue;

        List<String> camelId = parseCodeElementName(name);
        Set<String> codeElementWordSet = removeStopWords(camelId);

        if (codeElement instanceof MethodCodeElement
            && !((MethodCodeElement) codeElement).getReceiver().equals("target")) {
          // if receiver is not target, this is a method invoked from the subject, which for the reason
          // is implicit and will be excluded from the vector computation

          String wordToIgnore = "";
          if (subject.lastIndexOf(" ") != -1)
            // in case of composed subject take just the last word (may be the most significant)
            wordToIgnore = subject.substring(subject.lastIndexOf(" ") + 1, subject.length());
          else wordToIgnore = subject;

          codeElementWordSet.remove(wordToIgnore);

          Set<String> modifiedComment = new LinkedHashSet<String>(commentWordSet);
          modifiedComment.remove(wordToIgnore);
          finalComment = String.join(" ", modifiedComment).replaceAll("\\s+", " ").trim();
        } else {
          finalComment = String.join(" ", commentWordSet).replaceAll("\\s+", " ").trim();
        }
        finalID = String.join(" ", codeElementWordSet).replaceAll("\\s+", " ").trim().toLowerCase();

        double dist = 10;
        try {
          dist = wm.distance(finalComment, finalID);
        } catch (Exception e) {
          //do nothing
        }
        distances.put(codeElement, dist);
      }
    }

    return retainMatches(finalComment, method.getSignature(), distances);
  }

  /**
   * Split code element name according to camel case
   *
   * @param name code element name
   * @return list of words composing the code element name
   */
  private List<String> parseCodeElementName(String name) {
    ArrayList<String> camelId = new ArrayList<String>(Arrays.asList(name.split("(?<!^)(?=[A-Z])")));
    String joinedId = String.join(" ", camelId).replaceAll("\\s+", " ").trim().toLowerCase();
    int index = 0;
    for (CoreLabel lemma : StanfordParser.lemmatize(joinedId)) {
      if (lemma != null) {
        if (index < camelId.size()) {
          camelId.remove(index);
        }
        camelId.add(index, lemma.lemma());
      }
      index++;
    }
    return camelId;
  }

  /**
   * Compute and instantiate the {@code SemantiMatch} computed for a tag.
   *
   * @param parsedComment the parsed comment
   * @param methodName name of the method the tag belongs to
   * @param distances the computed distance, for every possible code element candidate, from the
   *     comment
   */
  private LinkedHashMap<CodeElement<?>, Double> retainMatches(
      String parsedComment, String methodName, Map<CodeElement<?>, Double> distances) {
    // Select as candidates only code elements that have a semantic distance below the chosen threshold.
    LinkedHashMap<CodeElement<?>, Double> orderedDistances =
        new LinkedHashMap<CodeElement<?>, Double>();

    if (!distances.isEmpty()) {
      if (!isWmdUsed) {
        distances
            .values()
            .removeIf(
                new Predicate<Double>() {
                  @Override
                  public boolean test(Double aDouble) {
                    return aDouble > distanceThreshold;
                  }
                });
      } else {
        distances
            .values()
            .removeIf(
                new Predicate<Double>() {
                  @Override
                  public boolean test(Double aDouble) {
                    return aDouble > wmdThreshold;
                  }
                });
      }

      // Order the retained distances from the lowest (best one) to the highest (worst one).
      orderedDistances =
          distances
              .entrySet()
              .stream()
              .sorted(Map.Entry.comparingByValue())
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
    return orderedDistances;
  }

  /**
   * Remove stopwords from given list of {@code String}s
   *
   * @param words list of {@code String}s to be cleaned
   * @return the cleaned list of words
   */
  private Set<String> removeStopWords(List<String> words) {
    if (this.stopWordsRemoval) {
      for (int i = 0; i < words.size(); i++) {
        String word = words.get(i).toLowerCase();
        if (this.stopwords.contains(word)) {
          words.remove(i);
          words.add(i, "");
        }
      }
    }
    Set<String> wordList = new HashSet<>(words);
    wordList.removeAll(Collections.singletonList(""));
    return wordList;
  }

  /**
   * Search for GloVe model in the resources and instantiate the reader.
   *
   * @return the reader
   */
  private GloveRandomAccessReader setUpGloveBinaryDB() throws URISyntaxException, IOException {
    URL gloveBinaryModel = ClassLoader.getSystemClassLoader().getResource("glove-binary");
    Path modelsPath = Paths.get(gloveBinaryModel.toURI());
    return new GloveBinaryRandomAccessReader(modelsPath);
  }
}
