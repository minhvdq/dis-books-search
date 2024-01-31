package model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Task implements Serializable {
    private final List<String> terms;
    private final List<String> documents;

    public Task(List<String> terms, List<String> documents) {
        this.terms = terms;
        this.documents = documents;
    }

    public List<String> getSearchTerms(){
        return Collections.unmodifiableList(terms);
    }
    public List<String> getSearchDocuments() {
        return Collections.unmodifiableList(documents);
    }
}
