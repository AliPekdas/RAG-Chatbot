package com.minirag.pipeline;

public interface IntentDetector {
    // Takes the question, returns the intent
    String detect(String question);
}