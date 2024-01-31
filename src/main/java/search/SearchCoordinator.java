package search;

import cluster.management.ServiceRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MapEntry;
import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;
import model.proto.SearchModel;
import networking.OnRequestCallBack;
import networking.WebClient;
import org.apache.zookeeper.KeeperException;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SearchCoordinator implements OnRequestCallBack {

    private static final String ENDPOINT = "/search";
    private static final String BOOK_DIRECTORY = "../resources/books";

    private List<String> documents;
    private final ServiceRegistry workerServiceRegistry;
    private final WebClient client;


    public SearchCoordinator(ServiceRegistry serviceRegistry, WebClient client){
        this.workerServiceRegistry = serviceRegistry;
        this.client = client;
        System.out.println("hehe2");
        System.out.println("hehe3");
        System.out.println(readDocumentList() == null);
        this.documents = readDocumentList();
        System.out.println(documents);
    }
    @Override
    public byte[] handleRequest(byte[] payload) {
        try {
            SearchModel.Request request = SearchModel.Request.parseFrom(payload);
            SearchModel.Response response = createResponse(request);

            return response.toByteArray();
        } catch (InvalidProtocolBufferException | InterruptedException | KeeperException e) {
            e.printStackTrace();
            return SearchModel.Response.getDefaultInstance().toByteArray();
        }
    }
    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }

    private SearchModel.Response createResponse (SearchModel.Request request) throws InterruptedException, KeeperException {
        SearchModel.Response.Builder searchResponse = SearchModel.Response.newBuilder();
        System.out.println("Received search query: " + request.getSearchQuery());

        List<String> searchTerms = TFIDF.getWordFromLine(request.getSearchQuery());
        System.out.println("there are " + searchTerms.size() + " terms");

        List<String> workers = workerServiceRegistry.getAllAddresses();
        if( workers.isEmpty() ) {
            System.out.println("There is no search worker available");
            return searchResponse.build();
        }

        List<Task> tasks = createTasks(workers.size(), searchTerms);
        List<Result> results = sendTasksToWokers(workers, tasks);
        List<SearchModel.Response.DocumentStats> sortedDocument = agregateResults(results, searchTerms);
        searchResponse.addAllDocumentRelevent(sortedDocument);

        return searchResponse.build();
    }

    private List<SearchModel.Response.DocumentStats> agregateResults(List<Result> results, List<String> searchTerms){
        Map<String, DocumentData> allDocumentResults = new HashMap<>();
        for( Result result : results ){
            allDocumentResults.putAll(result.getDocumentToDocData());
        }
        System.out.println("calculating score for all documents");
        Map<Double, List<String>> scoreToDocument = TFIDF.getDocumentSortedByScore(searchTerms, allDocumentResults);

        return sortedDocumentsByScore(scoreToDocument);
    }

    private List<SearchModel.Response.DocumentStats> sortedDocumentsByScore(Map<Double, List<String>> scoreToDocument){
        List<SearchModel.Response.DocumentStats> documentStatsList =  new ArrayList<>();

        for(Map.Entry<Double, List<String>> docScorePair : scoreToDocument.entrySet()) {
            double score = docScorePair.getKey();
            for( String document : docScorePair.getValue()){
                File file = new File(document);

                SearchModel.Response.DocumentStats documentStats = SearchModel.Response.DocumentStats.newBuilder()
                        .setScore(score)
                        .setDocumentName(file.getName())
                        .setDocumentSize(file.length())
                        .build();

                documentStatsList.add(documentStats);
            }
        }
        return documentStatsList;
    }

    private List<Result> sendTasksToWokers(List<String> workers, List<Task> tasks ) {
        CompletableFuture<Result>[] futures = new CompletableFuture[workers.size()];
        for(int i = 0; i < workers.size(); i ++) {
            String curWorker = workers.get(i);
            Task task = tasks.get(i);
            byte[] payload = SerializationUtils.serialize(task);

            futures[i] = client.sendTask(curWorker, payload);
        }

        List<Result> results = new ArrayList<>();
        for( CompletableFuture<Result> future : futures){
            try {
                Result result = future.get();
                results.add(result);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        System.out.println("Received " + results.size() + " results");
        return results;
    }
    private List<Task> createTasks( int numWorker, List<String> terms ){
        List<List<String>> splittedDocument = splitDocuments(numWorker, documents);
        List<Task > result = new ArrayList<>();
        for( int i = 0; i < numWorker; i ++){
            List<String> subDoc = splittedDocument.get(i);
            Task curTask = new Task(terms, subDoc);
            result.add(curTask);
        }
        return result;
    }
    private List<List<String>> splitDocuments( int numWorker, List<String> documents ){
        List<List<String>> ans = new ArrayList<>();
        for( int i = 0; i < documents.size(); i ++){
            int currentIndex = i % numWorker;
            if ( ans.size() <= currentIndex ){
                List<String> subList = new ArrayList<>();
                ans.add(subList);
            }
            List<String> curSubDoc = ans.get(currentIndex);
            curSubDoc.add(documents.get(i));
        }
        return ans;
    }
    public static List<String> readDocumentList() {
        File file = new File(BOOK_DIRECTORY);
        return Arrays.asList(file.list())
                .stream()
                .map(documentName -> BOOK_DIRECTORY + "/" + documentName)
                .collect(Collectors.toList());
    }
}
