package org.toradocu.translator.semantic;

import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import de.jungblut.math.DoubleVector;
import edu.stanford.nlp.ling.CoreLabel;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.translator.CodeElement;
import org.toradocu.translator.MethodCodeElement;
import org.toradocu.translator.Proposition;
import org.toradocu.translator.StanfordParser;

/**
 * Created by arianna on 29/05/17.
 *
 * <p>Main component. Contains all the methods to compute the {@code SemantichMatch}es for a given
 * class. This implements the "basic" semantic semantic, i.e. the one that uses plain vector sums.
 * Other kinds of matcher will extend this class.
 */
public class SemanticMatcher {

  static boolean stopwordsRemoval;
  static boolean posSelect;
  static float distanceThreshold;
  static List<String> stopwords;
  /** Stores all the {@code SemanticMatch}es collected during a test. */
  public static Set<SemanticMatch> semanticMatches;

  private static GloveRandomAccessReader gloveDB;

  public SemanticMatcher(boolean stopwordsRemoval, boolean posSelect, float distanceThreshold) {

    this.stopwordsRemoval = stopwordsRemoval;
    this.posSelect = posSelect;
    this.distanceThreshold = distanceThreshold;
    semanticMatches = new HashSet<SemanticMatch>();

    //TODO very naive list. Not the best to use.
    stopwords =
        new ArrayList<>(
            Arrays.asList(
                "true", "false", "the", "a", "if", "for", "be", "have", "this", "do", "not", "of",
                "in", "null", "only", "already", "specify"));

    gloveDB = setUpGloveBinaryDB();
  }

  /**
   * Takes a goal file of a certain class in order to extract all its {@code DocumentedMethod}s and
   * the list of Java code elements that can be used in the translation.
   *
   * @param codeElements the list of Java code elements for the translation
   * @param method
   * @param comment
   */
  public LinkedHashMap<CodeElement<?>, Double> runVectorMatch(
      List<CodeElement<?>> codeElements,
      DocumentedExecutable method,
      Proposition proposition,
      String comment)
      throws IOException {

    //    this.stopwords.add(proposition.getSubject().getSubject().toLowerCase());
    try {
      return vectorsMatch(comment, proposition, method, codeElements);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Computes semantic semantic through GloVe vectors.
   *
   * @param proposition
   * @param method the method the tag belongs to
   * @param codeElements the code elements that are possible candidates to use in the translation
   * @throws IOException if the GloVe database couldn't be read
   */
  LinkedHashMap<CodeElement<?>, Double> vectorsMatch(
      String comment,
      Proposition proposition,
      DocumentedExecutable method,
      List<CodeElement<?>> codeElements)
      throws IOException {
    Set<String> commentWordSet = this.parseComment(comment, method);

    if (commentWordSet.size() > 4)
      //TODO vectors sum doesn't work well with long comments. Consider sub-sentences, or just prefer WMD
      commentWordSet = this.parseComment(proposition.getPredicate(), method);

    String parsedComment = String.join(" ", commentWordSet).replaceAll("\\s+", " ").trim();

    Map<String, Double> freq = new HashMap<String, Double>();
    CosineDistance cos = new CosineDistance();

    DoubleVector commentVector = getCommentVector(commentWordSet);

    Map<CodeElement<?>, Double> distances = new LinkedHashMap<>();

    String subject = proposition.getSubject().getSubject().toLowerCase();
    String wordToReward = null;
    // For each code element, I want to take the vectors of its identifiers (like words componing the method name)
    // and compute the semantic similarity with the predicate (or the whole comment, we'll see)
    if (codeElements != null && !codeElements.isEmpty()) {
      for (CodeElement<?> codeElement : codeElements) {
        if (codeElement instanceof MethodCodeElement) {
          //          if(!((MethodCodeElement) codeElement).getReceiver().equals("target")){
          //            //if receiver is not target, this code element is a method invoked from the subject.
          //            if(subject.lastIndexOf(" ")!=-1)
          //              wordToReward = subject.substring(subject.lastIndexOf(" ")+1, subject.length());
          //            else
          //              wordToReward = subject;
          //          }

          DoubleVector methodVector =
              getCodeElementVector(freq, (MethodCodeElement) codeElement, wordToReward);

          if (methodVector != null && commentVector != null) {
            double dist = cos.measureDistance(methodVector, commentVector);
            distances.put(codeElement, dist);
          }
        }
      }
      return retainMatches(parsedComment, method.getSignature(), comment, distances);
    }
    return null;
  }

  /**
   * Build the vector representing a code element, made by its IDs camel case-splitted
   *
   * @param freq TFID map
   * @param codeElement the code element
   * @param wordToReward
   * @return a {@code DoubleVector} representing the code element vector
   * @throws IOException if the database couldn't be read
   */
  private static DoubleVector getCodeElementVector(
      Map<String, Double> freq, MethodCodeElement codeElement, String wordToReward)
      throws IOException {
    int index;
    DoubleVector codeElementVector = null;
    ArrayList<String> camelId =
        new ArrayList<String>(
            Arrays.asList(codeElement.getJavaCodeElement().getName().split("(?<!^)(?=[A-Z])")));
    String joinedId = String.join(" ", camelId).replaceAll("\\s+", " ").toLowerCase().trim();
    if (wordToReward != null) {
      joinedId = wordToReward + " " + joinedId;
      camelId.add(0, wordToReward);
    }

    index = 0;
    for (CoreLabel lemma : StanfordParser.lemmatize(joinedId)) {
      if (lemma != null) {
        camelId.remove(index);
        camelId.add(index, lemma.lemma());
      }
      index++;
    }

    for (int i = 0; i < camelId.size(); i++) {
      DoubleVector v = gloveDB.get(camelId.get(i).toLowerCase());
      if (stopwordsRemoval && stopwords.contains(camelId.get(i).toLowerCase())) continue;
      if (v != null) {
        if (codeElementVector == null) codeElementVector = v;
        else codeElementVector = codeElementVector.add(v);
      }
    }

    return codeElementVector;
  }

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

  protected double computeSim(GloveRandomAccessReader db, String commentT, String codeElemT)
      throws IOException {
    DoubleVector ctVector = db.get(commentT);
    DoubleVector cetVector = db.get(codeElemT);
    CosineDistance cos = new CosineDistance();
    if (ctVector != null && cetVector != null)
      return (1 + cos.measureDistance(ctVector, cetVector)) / 2;

    return 1;
  }

  /**
   * Parse the original tag comment according to the configuration parameters.
   *
   * @param method the {@code DocumentedMethod} containing the tag
   * @return the parsed comment in form of array of strings (words)
   */
  Set<String> parseComment(String comment, DocumentedExecutable method) {
    //        if (posSelect) comment = POSUtils.findSubjectPredicate(tag.getComment(), method);
    //        else comment = tag.getComment().getText();
    comment = comment.replaceAll("[^A-Za-z0-9! ]", "").toLowerCase();

    String[] wordComment = comment.split(" ");
    int index = 0;
    List<CoreLabel> lemmas = StanfordParser.lemmatize(comment);
    for (CoreLabel lemma : lemmas) {
      if (lemma != null) wordComment[index] = lemma.lemma();
      index++;
    }

    return removeStopWords(wordComment, method.getContainingClass().getSimpleName());
  }

  /**
   * Compute and instantiate the {@code SemantiMatch} for a tag.
   *
   * @param parsedComment the parse tag comment
   * @param methodName name of the method the tag belongs to
   * @param tag the {@code Tag}
   * @param distances the computed distance, for every possible code element candidate, from the
   */
  LinkedHashMap<CodeElement<?>, Double> retainMatches(
      String parsedComment, String methodName, String tag, Map<CodeElement<?>, Double> distances) {
    SemanticMatch aMatch = new SemanticMatch(tag, methodName, parsedComment, distanceThreshold);
    // Select as candidates only code elements that have a semantic distance below the chosen threshold.
    if (distanceThreshold != -1) {
      distances
          .values()
          .removeIf(
              new Predicate<Double>() {
                @Override
                public boolean test(Double aDouble) {
                  return aDouble > distanceThreshold;
                }
              });
    }

    LinkedHashMap<CodeElement<?>, Double> orderedDistances;
    orderedDistances =
        distances
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    aMatch.setCandidates(orderedDistances);

    if (!aMatch.candidates.isEmpty()) semanticMatches.add(aMatch);

    return orderedDistances;
  }

  static Set<String> removeStopWords(String[] words, String className) {

    if (stopwordsRemoval) {
      for (int i = 0; i != words.length; i++) {
        if (stopwords.contains(words[i].toLowerCase())
            || words[i].toLowerCase().equals(className.toLowerCase()))
        //         Class name often is not useful at all (usually it's the subject). Try removing it
        {
          words[i] = "";
        }
      }
    }
    Set<String> wordList = new HashSet<>(Arrays.asList(words));
    wordList.removeAll(Arrays.asList(""));
    return wordList;
  }

  private GloveRandomAccessReader setUpGloveBinaryDB() {
    GloveRandomAccessReader gloveBinaryDb = null;
    try {
      gloveBinaryDb =
          new GloveBinaryRandomAccessReader(
              Paths.get("/home/arianna/Scaricati/glove-master/target/glove-binary"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return gloveBinaryDb;
  }
}
