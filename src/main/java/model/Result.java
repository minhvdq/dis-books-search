package model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {
    Map<String, DocumentData> documentToDocData = new HashMap<>();

    public void addToDocumentMap(String document, DocumentData documentData){
        documentToDocData.put(document, documentData);
    }

    public Map<String, DocumentData> getDocumentToDocData(){
        return Collections.unmodifiableMap(documentToDocData);
    }
}
