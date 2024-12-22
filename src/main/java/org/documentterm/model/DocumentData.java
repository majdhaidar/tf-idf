package org.documentterm.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a document's data structure used for storing term frequencies.
 * This class provides methods for adding terms along with their frequencies and
 * retrieving the frequency of a specific term. It is commonly utilized in text
 * processing and information retrieval tasks such as TF-IDF computation.
 */
public class DocumentData {
    private Map<String, Double> termFrequency = new HashMap<>();

    public void addTerm(String term, double frequency){
        termFrequency.put(term, frequency);
    }

    public double getTermFrequency(String term){
        return termFrequency.get(term);
    }
}
