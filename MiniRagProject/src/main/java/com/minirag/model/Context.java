package com.minirag.model;

import java.util.List;
import java.util.ArrayList;

public class Context {
    // Inputs
    public String originalQuestion;
    
    // Interim Results
    public String intent;                 // IntentDetector fill in
    public List<String> queryTerms;       // QueryWriter fill in
    public List<Hit> hits;                // Retriever fill in, Reranker update
    
    // Output
    public String finalAnswer;            // AnswerAgent fill in

    public Context(String originalQuestion) {
        this.originalQuestion = originalQuestion;
        this.queryTerms = new ArrayList<>();
        this.hits = new ArrayList<>();
    }
}