package com.minirag.pipeline;

import java.util.List;

public interface QueryWriter {
    // Takes the question and intent, returns a word list
    List<String> write(String question, String intent);
}