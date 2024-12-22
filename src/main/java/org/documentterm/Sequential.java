package org.documentterm;

import org.documentterm.model.DocumentData;
import org.documentterm.search.TFIDF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Sequential {

    public static final String BOOKS_DIR = "src/main/resources/books";

    public static final String SEARCH_QUERY_1 = "the";
    public static final String SEARCH_QUERY_2 = "the girl that falls";
    public static final String SEARCH_QUERY_3 = "the best detective";
    public static final String SEARCH_QUERY_4 = "A war between";
    public static final String SEARCH_QUERY_5 = "cold winter";
    public static final String SEARCH_QUERY_6 = "detective methods";

    public static void main(String[] args) throws FileNotFoundException {
        File document = new File(BOOKS_DIR);
        List<String> documents= Arrays.asList(document.list())
                .stream()
                .map(documentName -> BOOKS_DIR + "/"+documentName)
                .collect(Collectors.toList());

        List<String> terms = TFIDF.getWordsFromLine(SEARCH_QUERY_2);
        findMostRelevantDocuments(documents, terms);
    }

    private static void findMostRelevantDocuments(List<String> documents, List<String> terms) throws FileNotFoundException {
        Map<String, DocumentData> documentDataMap = new HashMap<>();
        for(String document: documents){
            BufferedReader reader = new BufferedReader(new FileReader(document));
            List<String> lines = reader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromLine(lines);
            DocumentData documentData = TFIDF.createDocumentData(words, terms);
            documentDataMap.put(document, documentData);
        }

        Map<Double, List<String>> documentByScore = TFIDF.getDocumentsSortedByScore(terms, documentDataMap);
        printResults(documentByScore);
    }

    private static void printResults(Map<Double, List<String>> documentByScore) {
        for(Map.Entry<Double, List<String>> entry: documentByScore.entrySet()){
            double score = entry.getKey();
            for(String document: entry.getValue()){
                System.out.println(String.format("Score: %s, Document: %s", score, document));
            }
        }
    }
}
