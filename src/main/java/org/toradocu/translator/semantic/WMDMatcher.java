package org.toradocu.translator.semantic; //package org.toradocu.translator.semantic;
//
//import com.crtomirmajer.wmd4j.WordMovers;
//import edu.stanford.nlp.ling.CoreLabel;
//import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
//import org.toradocu.extractor.DocumentedMethod;
//import org.toradocu.extractor.Tag;
//import org.toradocu.translator.StanfordParser;
//import util.OutputUtil;
//import util.SimpleMethodCodeElement;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Created by arianna on 10/07/17.
// */
//public class WMDMatcher extends SemanticMatcher {
//    WMDMatcher(String className, boolean stopwordsRemoval, boolean posSelect, boolean tfid, float distanceThreshold) {
//        super(className, stopwordsRemoval, posSelect, tfid, distanceThreshold);
//    }
//
//    void runWmdMatch(File goalFile, Set<SimpleMethodCodeElement> codeElements, WordVectors vectors){
//        Set<DocumentedMethod> methods = this.readMethodsFromJson(goalFile);
//
//        WordMovers wm = WordMovers.Builder().wordVectors(vectors).build();
//
//        for(DocumentedMethod m : methods){
//            HashSet<SimpleMethodCodeElement> referredCodeElements = codeElements
//                    .stream()
//                    .filter(forMethod -> forMethod.getForMethod().equals(m.getSignature()))
//                    .collect(Collectors.toCollection(HashSet::new));
//
//            if(m.returnTag() != null){
//                String condition = m.returnTag().getCondition().get();
//                if(!condition.equals("")) {
//                    wmdMatch(wm, m.returnTag(), m, referredCodeElements);
//                }
//            }
//            if(!m.throwsTags().isEmpty()){
//                for(Tag throwTag : m.throwsTags()){
//                    String condition = throwTag.getCondition().get();
//                    if(!condition.equals("")) {
//                        wmdMatch(wm, throwTag, m, referredCodeElements);
//                    }
//                }
//            }
//        }
//        try {
//            OutputUtil.exportTojson(true, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void wmdMatch(WordMovers wm, Tag tag, DocumentedMethod method, Set<SimpleMethodCodeElement> codeElements){
//        Map<SimpleMethodCodeElement, Double> distances = new HashMap<SimpleMethodCodeElement, Double>();
//        Set<String> commentWordSet = super.parseComment(tag, method);
//        String parsedComment = String.join(" ", commentWordSet).replaceAll("\\s+", " ").trim();
//        if (codeElements != null && !codeElements.isEmpty()) {
//            for(SimpleMethodCodeElement codeElement : codeElements){
//                Set<String> ids = codeElement.getCodeElementIds();
//                for (String id : ids) {
//                    String[] camelId = id.split("(?<!^)(?=[A-Z])");
//                    String joinedId = String.join(" ", camelId).replaceAll("\\s+", " ").trim().toLowerCase();
//                    int index = 0;
//                    for (CoreLabel lemma : StanfordParser.lemmatize(joinedId)) {
//                        if (lemma != null) camelId[index] = lemma.lemma();
//                        index++;
//                    }
//                    Set<String> codeElementWordSet = removeStopWords(camelId);
//                    joinedId = String.join(" ", codeElementWordSet).replaceAll("\\s+", " ").trim().toLowerCase();
//                    double dist = 10;
//                    try{
//                        dist = wm.distance(parsedComment, joinedId);
//                    }catch(NoSuchElementException e){
//                        //do nothing
//                    }
//                    distances.put(codeElement, dist);
//                }
//            }
//        }
//        retainMatches(parsedComment, method.getSignature(), tag, distances);
//    }
//}
