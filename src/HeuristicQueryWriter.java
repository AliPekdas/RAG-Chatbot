import java.util.*;

public class HeuristicQueryWriter implements QueryWriter {
    
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        // --- Articles & Determiners ---
        "the", "a", "an", "this", "that", "these", "those", "all", "any", "both", "each", 
        "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same",

        // --- Prepositions ---
        "of", "in", "on", "at", "to", "for", "with", "by", "from", "up", "down", "about", 
        "into", "over", "after", "beneath", "under", "above", "below", "through", "during", 
        "before", "between", "out", "off", "once", "here", "there", "where", "why", "how",

        // --- Pronouns ---
        "i", "me", "my", "myself", "we", "our", "ours", "ourselves", 
        "you", "your", "yours", "yourself", "yourselves", 
        "he", "him", "his", "himself", "she", "her", "hers", "herself", 
        "it", "its", "itself", "they", "them", "their", "theirs", "themselves",

        // --- Auxiliary Verbs & To Be ---
        "be", "been", "being", "am", "is", "are", "was", "were", 
        "have", "has", "had", "having", "do", "does", "did", "doing",
        "can", "could", "will", "would", "shall", "should", "may", "might", "must",

        // --- Conjunctions ---
        "and", "but", "if", "or", "because", "as", "until", "while", "when", "where", "why", "how",

        // --- Common Question Starters & Fillers ---
        "what", "which", "who", "whom", "whose", "why", "how", "where", "when",
        "please", "tell", "ask", "question", "answer", "regarding", "related",

        // --- Bağlaçlar ve Edatlar ---
        "ve", "veya", "ile", "ama", "fakat", "lakin", "ancak", "çünkü", "oysa", 
        "yada", "yahut", "ise", "de", "da", "ki", "diye", "üzere", "için", "gibi", 
        "kadar", "göre", "rağmen", "dair", "karşı", "dolayı", "tarafından",

        // --- Zamirler ---
        "bu", "şu", "o", "bunlar", "şunlar", "onlar", "bunu", "şunu", "onu", 
        "buna", "şuna", "ona", "bunda", "şunda", "onda", "bundan", "şundan", "ondan",
        "ben", "sen", "biz", "siz", "kendi", "kendisi", "birbiri", "biri", "kimse", "herkes",

        // --- Soru Ekleri ve Kelimeleri ---
        "ne", "nedir", "neyi", "kim", "kimdir", "kime", "hangi", "hangisi",
        "nasıl", "niye", "neden", "niçin", "kaç", "nerede", "nereye",
        "mı", "mi", "mu", "mü", "mıdır", "midir", "mudur", "müdür",

        // --- Genel ---
        "bir", "tek", "ilk", "son", "önce", "sonra", "daha", "en", "çok", "az",
        "var", "yok", "olan", "olarak", "olduğu", "ilgili", "hakkında"
    ));

    @Override
    public List<String> write(String question, Intent intent) {
        // 1. Convert to lowercase and remove punctuation marks
        String clean = question.toLowerCase().replaceAll("[^a-z0-9çğıİöşü ]", "");
        
        // 2. Split into words
        String[] tokens = clean.split("\\s+");
        
        List<String> terms = new ArrayList<>();
        
        // 3. Stopword cleaning
        for (String token : tokens) {
            if (!STOP_WORDS.contains(token) && token.length() > 1) {
                terms.add(token);
            }
        }
        
        // 4. Intent Booster
        switch (intent) {
            case STAFF_LOOKUP -> {
                terms.add("staff");
                terms.add("advisor");
                terms.add("office");
                terms.add("contact");
                terms.add("email");
                terms.add("danışman");
                terms.add("hocası");
            }

            case REGISTRATION -> {
                terms.add("registration");
                terms.add("enrollment");
                terms.add("course selection");
                terms.add("ders seçimi");
                terms.add("kayıt yenileme");
                terms.add("add drop");
                terms.add("harç");
            }

            case COURSE -> {
                terms.add("syllabus");
                terms.add("curriculum");
                terms.add("prerequisite");
                terms.add("ön şart");
                terms.add("credit");
                terms.add("ects");
                terms.add("akts");
                terms.add("müfredat");
            }

            case POLICY_FAQ -> {
                terms.add("regulation");
                terms.add("directive");
                terms.add("yönerge");
                terms.add("mevzuat");
                terms.add("yönetmelik");
                terms.add("exam");
                terms.add("sınav");
                terms.add("grading");
                terms.add("internship");
                terms.add("staj");
            }
            
            case UNKNOWN -> {
            }
        }
        
        return terms;
    }
}