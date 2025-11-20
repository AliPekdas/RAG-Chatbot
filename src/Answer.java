import java.util.List;

public class Answer {
    public String text;
    public List<String> citations;

    public Answer(String text, List<String> citations) {
        this.text = text;
        this.citations = citations;
    }

    public String getText() {
        return text;
    }

    public List<String> getCitations() {
        return citations;
    }

    @Override
    public String toString() {
        return text + " " + citations;
    }
}
