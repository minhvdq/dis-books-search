package search;

import model.DocumentData;

import javax.print.Doc;
import java.util.*;
import java.lang.*;
public class TFIDF {
    public static double calculateTermFrequency( String term, List<String> words ){
        int count = 0;
        for( String word : words ){
            if( word.equalsIgnoreCase(term)){
                count ++;
            }
        }
        double termFrequency = (double) count/ words.size();
        return  termFrequency;
    }

    public static DocumentData frequencyToDocument( List<String> words, List<String> terms ){
        DocumentData documentData = new DocumentData();
        for(String term : terms){
            double termFrequency = calculateTermFrequency(term, words);
            documentData.addFrequency(term, termFrequency);
        }
        return documentData;
    }

    private static double getInverseDocumentFrequency( String term, Map<String, DocumentData> documentResults){
        int totalCount = 0;
        for( String docName : documentResults.keySet()){
            DocumentData documentData = documentResults.get(docName);
            double currentTF = documentData.getFrequency(term);
            if( currentTF > 0.0) {
                totalCount ++;
            }
        }
        return totalCount == 0 ? 0 : Math.log10((double) documentResults.size() / totalCount);
    }

    private static Map<String, Double> getAllIDF( List<String> terms, Map<String, DocumentData> documentResults) {
        Map<String, Double> result = new HashMap<>();
        for( String term : terms ){
            double termIDF = getInverseDocumentFrequency(term, documentResults);
            result.put(term, termIDF);
        }
        return result;
    }

    private static double calculateDocumentScore(List<String> terms, DocumentData documentData, Map<String, Double> IDFs){
        double score = 0;
        for( String term : terms ) {
            double termFrequency = documentData.getFrequency(term);
            double currentIDF = IDFs.get(term);
            score += termFrequency * currentIDF;
        }
        return score;
    }

    public static Map<Double, List<String>> getDocumentSortedByScore(List<String> terms, Map<String, DocumentData> documentResults){
        TreeMap<Double, List<String>> result = new TreeMap<>();

        Map<String, Double> allIDF = getAllIDF(terms,documentResults);
        for( String docName : documentResults.keySet()){
            DocumentData documentData = documentResults.get(docName);
            double curScore = calculateDocumentScore(terms, documentData,allIDF);
            addCurScoreToMap( result, curScore, docName);

        }
        return result.descendingMap();
    }

    private static void addCurScoreToMap( TreeMap< Double, List<String>> result, double curScore, String docName){
        List<String> sortedDocuments = result.get(curScore);
        if( sortedDocuments == null){
            sortedDocuments = new ArrayList<String>();
        }
        sortedDocuments.add(docName);
        result.put(curScore, sortedDocuments);
    }

    public static List<String> getWordFromLine( String line ){
        return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    public static List<String> getWordFromLines( List<String> lines ){
        List<String> words = new ArrayList<>();
        for( String line : lines ){
            words.addAll(getWordFromLine(line));
        }
        return words;
    }

}
