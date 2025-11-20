import java.util.List;

public interface Reranker {
    List<Hit> rerank(List<Hit> hits, List<String> queryTerms);
}