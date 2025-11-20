import java.util.List;

public interface AnswerAgent {
    Answer answer(String question, List<String> terms, List<Hit> rankedHits);
}
