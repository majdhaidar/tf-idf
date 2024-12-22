package org.documentterm.search;

import org.documentterm.model.DocumentData;

import java.util.*;

/**
 * Provides methods to compute the Term Frequency-Inverse Document Frequency (TF-IDF)
 * values for text processing and information retrieval tasks. This class allows
 * calculating term frequencies, inverse document frequencies, and overall scores
 * for documents based on TF-IDF weight.
 */
//Term frequency inverse document frequency class
public class TFIDF {
    /**
     * Calculates the term frequency of a specific term in a given list of words.
     * Term frequency is computed as the number of occurrences of the term
     * divided by the total number of words in the list.
     *
     * @param words the list of words from which to calculate the term frequency
     * @param term the specific term to calculate frequency for
     * @return the term frequency as a double value
     */
    public static double calculateTermFrequency(List<String> words, String term) {
        int count = 0;
        for (String word : words) {
            if (word.equals(term)) {
                count++;
            }
        }
        return (double) count / words.size();
    }

    /**
     * Creates a DocumentData object by calculating and adding term frequencies for
     * a given list of terms based on their occurrences in a list of words.
     *
     * @param words the list of words representing the content of a document
     * @param terms the list of specific terms for which to calculate frequencies
     * @return a DocumentData object populated with the term frequencies
     */
    public static DocumentData createDocumentData(List<String> words, List<String> terms) {
        DocumentData documentData = new DocumentData();
        for (String term : terms) {
            documentData.addTerm(term, calculateTermFrequency(words, term));
        }
        return documentData;
    }

    /**
     * Computes the Inverse Document Frequency (IDF) for a given term across a collection of documents.
     * IDF measures how significant a term is within the entire collection of documents by considering
     * the number of documents containing the term relative to the total number of documents. A term
     * that appears in fewer documents will have a higher IDF score, signifying its uniqueness.
     *
     * @param term the specific term for which to compute the IDF
     * @param documentResults a map where keys represent document identifiers, and values contain
     *                        the respective document's data, including term frequencies
     * @return the IDF value for the term as a double; if the term is not found in any document,
     *         the method returns 0
     */
    public static double getInverseDocumentFrequency(String term, Map<String, DocumentData> documentResults) {
        double numberOfDocuments = 0;
        for (String document : documentResults.keySet()) {
            DocumentData documentData = documentResults.get(document);
            double termFrequency = documentData.getTermFrequency(term);
            if (termFrequency > 0.0) {
                numberOfDocuments++;
            }
        }

        return numberOfDocuments == 0 ? 0 : Math.log10(documentResults.size() / numberOfDocuments);
    }

    /**
     * Generates a mapping of terms to their respective Inverse Document Frequency (IDF) values
     * based on the provided list of terms and document data. The IDF scores are computed using
     * the getInverseDocumentFrequency method.
     *
     * @param terms a list of terms for which the IDF values are to be calculated
     * @param documentResults a map where keys represent document identifiers and values contain
     *                        the respective document's data, including term frequencies
     * @return a map where the keys are terms and the values are their corresponding IDF scores
     */
    public static Map<String, Double> getTermToInverseDocumentFrequencyMap(List<String> terms, Map<String, DocumentData> documentResults) {
        Map<String, Double> termToInverseDocumentFrequencyMap = new HashMap<>();
        for (String term : terms) {
            termToInverseDocumentFrequencyMap.put(term, getInverseDocumentFrequency(term, documentResults));
        }
        return termToInverseDocumentFrequencyMap;
    }

    /**
     * Calculates the score of a document based on the provided terms, document data,
     * and a mapping of terms to their respective inverse document frequency (IDF) values.
     * The score is computed as the sum of the product of term frequency and IDF for each term.
     *
     * @param terms the list of terms for which the document score is to be calculated
     * @param documentData an instance of DocumentData containing term frequencies for the document
     * @param termToInverseDocumentFrequency a map where keys are terms and values are their respective IDF scores
     * @return the calculated document score as a double value
     */
    public static double calculateDocumentScore(List<String> terms, DocumentData documentData, Map<String, Double> termToInverseDocumentFrequency) {
        double score = 0;
        for (String term : terms) {
            double termFrequency = documentData.getTermFrequency(term);
            double inverseDocumentFrequency = termToInverseDocumentFrequency.get(term);
            score += termFrequency * inverseDocumentFrequency;
        }
        return score;
    }

    /**
     * Sorts documents based on their computed scores in descending order.
     * The score for each document is calculated using the provided terms and
     * their respective inverse document frequencies. Documents with the same score
     * are grouped together.
     *
     * @param terms a list of terms to be used for scoring the documents
     * @param documentResults a map where keys represent document identifiers and values
     *                        contain the respective document's data, including term frequencies
     * @return a map where the keys are scores (as Double values) in descending order, and the values are
     *         lists of document identifiers associated with each score
     */
    public static Map<Double, List<String>> getDocumentsSortedByScore(List<String> terms, Map<String, DocumentData> documentResults) {

        TreeMap<Double, List<String>> scoreToDocuments = new TreeMap<>();
        Map<String, Double> termToInverseDocumentFrequency = getTermToInverseDocumentFrequencyMap(terms, documentResults);
        for(String document : documentResults.keySet()){
            DocumentData documentData = documentResults.get(document);
            double score = calculateDocumentScore(terms, documentData, termToInverseDocumentFrequency);
            addDocumentScoreToTreeMap(scoreToDocuments, score, document);
        }
        return scoreToDocuments.descendingMap();
    }

    /**
     * Adds a document and its corresponding score to a TreeMap where scores are keys and values
     * are lists of document identifiers associated with those scores. If the score already exists
     * in the map, the document is added to the existing list. Otherwise, a new entry is created
     * with the score as the key and the document in a new list as the value.
     *
     * @param scoreToDocuments a TreeMap where the keys are scores (Double) and the values are lists of document identifiers
     * @param score the score of the document to be added
     * @param document the document identifier to be associated with the provided score
     */
    private static void addDocumentScoreToTreeMap(TreeMap<Double, List<String>> scoreToDocuments, double score, String document) {
        List<String> documentsWithCurrentScore = scoreToDocuments.get(score);
        if(documentsWithCurrentScore == null){
            documentsWithCurrentScore = new ArrayList<>();
        }
        documentsWithCurrentScore.add(document);
        scoreToDocuments.put(score, documentsWithCurrentScore);
    }

    /**
     * Extracts individual words from a given input string by splitting it using
     * various delimiters such as periods, commas, dashes, spaces, question marks,
     * exclamation marks, semicolons, colons, and numeric or newline characters.
     *
     * @param line the input string from which words are to be extracted
     * @return a list of words obtained by splitting the input string
     */
    public static List<String> getWordsFromLine(String line){
        return Arrays.asList(line.split("(\\.)+|(,)+|(-)+|( )+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    /**
     * Extracts individual words from a list of input strings by processing each string
     * and splitting it into words using various delimiters such as periods, commas, dashes,
     * spaces, question marks, exclamation marks, semicolons, colons, and numeric or newline characters.
     *
     * @param lines the list of input strings from which words are to be extracted
     * @return a list of words obtained by splitting each input string
     */
    public static List<String> getWordsFromLine(List<String> lines){
        List<String> words = new ArrayList<>();
        for(String line : lines){
            words.addAll(getWordsFromLine(line));
        }
        return words;
    }

    public static List<String> getWordsFromDocument(List<String> lines) {
        List<String> words = new ArrayList<>();
        for (String line : lines) {
            words.addAll(getWordsFromLine(line));
        }
        return words;
    }

    public static Map<Double, List<String>> getDocumentsScores(List<String> terms,
                                                               Map<String, DocumentData> documentResults) {
        TreeMap<Double, List<String>> scoreToDoc = new TreeMap<>();

        Map<String, Double> termToInverseDocumentFrequency = getTermToInverseDocumentFrequencyMap(terms, documentResults);

        for (String document : documentResults.keySet()) {
            DocumentData documentData = documentResults.get(document);

            double score = calculateDocumentScore(terms, documentData, termToInverseDocumentFrequency);

            addDocumentScoreToTreeMap(scoreToDoc, score, document);
        }
        return scoreToDoc.descendingMap();
    }
}
