package search;

import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;
import networking.OnRequestCallBack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchWorker implements OnRequestCallBack {
    private final String ENDPOINT = "/task";
    @Override
    public byte[] handleRequest(byte[] inputByte) {
        Task task = (Task) SerializationUtils.deserialize(inputByte);
        Result result = createResult( task );
        return SerializationUtils.serialize(result);
    }

    private Result createResult (Task task){
        List<String> documents = task.getSearchDocuments();
        System.out.printf("There are %d documents to process%n", documents.size());
        Result result = new Result();
        for(String document : documents){
            System.out.println("Processing on document:  " + document.replace("../resources/books/", ""));
            List<String> words = parseWordsFromFile(document);
            DocumentData documentData = TFIDF.frequencyToDocument(words, task.getSearchTerms());
            System.out.println("TF for this document: ");
            for(String term : task.getSearchTerms()){
                System.out.println(term + " tf is " + documentData.getFrequency(term));
            }
            result.addToDocumentMap(document, documentData);
        }
        return result;
    }

    private static List<String> parseWordsFromFile(String document){
        FileReader file = null;
        try {
            file = new FileReader(document);
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
        BufferedReader bufferedReader = new BufferedReader(file);
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        List<String> words = TFIDF.getWordFromLines(lines);
        return words;
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
}
