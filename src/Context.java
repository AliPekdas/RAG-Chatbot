import java.util.List;
import java.util.ArrayList;

public class Context {
    public String originalQuestion;
    public Intent intent;
    public List<String> queryTerms;
    public List<Hit> hits;
    
    public Answer finalAnswer;

    public Context(String question) {
        this.originalQuestion = question;
        this.queryTerms = new ArrayList<>();
        this.hits = new ArrayList<>();
    }
}