package com.minirag.pipeline;

import com.minirag.model.Hit;
import java.util.List;

public interface Reranker {
    // Retrieves the current results and query, returns the updated list of scores
    List<Hit> rerank(List<Hit> hits, List<String> queryTerms);
}