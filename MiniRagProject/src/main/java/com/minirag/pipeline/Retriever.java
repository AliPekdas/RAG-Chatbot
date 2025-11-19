package com.minirag.pipeline;

import com.minirag.model.Hit;
import java.util.List;

public interface Retriever {
    // Takes search terms, returns a list of hits (results)
    List<Hit> retrieve(List<String> queryTerms);
}