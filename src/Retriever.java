import java.util.List;

public interface Retriever {
    List<Hit> retrieve(List<String> queryTerms, KeywordIndex index);
}