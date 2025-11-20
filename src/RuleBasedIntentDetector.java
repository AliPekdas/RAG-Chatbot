import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class RuleBasedIntentDetector implements IntentDetector {

    // Kural Seti: Anahtar Kelime -> Niyet
    // YAML dosyası yerine basitlik için Map kullanılıyor.
    private static final Map<String, Intent> RULE_SET = new HashMap<>();

    static {
        // Personel Arama Kuralları
        RULE_SET.put("hoca", Intent.STAFF_LOOKUP);
        RULE_SET.put("danışman", Intent.STAFF_LOOKUP);
        RULE_SET.put("ofis", Intent.STAFF_LOOKUP);
        RULE_SET.put("kimin", Intent.STAFF_LOOKUP);

        // Yönetmelik/FAQ Kuralları
        RULE_SET.put("zorunlu", Intent.POLICY_FAQ);
        RULE_SET.put("yönetmelik", Intent.POLICY_FAQ);
        RULE_SET.put("staj", Intent.POLICY_FAQ);
        RULE_SET.put("sınav", Intent.POLICY_FAQ);
        RULE_SET.put("not sistemi", Intent.POLICY_FAQ);

        // Ders Kayıt Kuralları
        RULE_SET.put("kayıt", Intent.REGISTRATION);
        RULE_SET.put("ders seçimi", Intent.REGISTRATION);
        RULE_SET.put("harç", Intent.REGISTRATION);
        RULE_SET.put("ekle sil", Intent.REGISTRATION);

        // Ders Bilgisi Kuralları
        RULE_SET.put("ön şart", Intent.COURSE);
        RULE_SET.put("akts", Intent.COURSE);
        RULE_SET.put("müfredat", Intent.COURSE);
        RULE_SET.put("kredisi", Intent.COURSE);
    }

    @Override
    public Intent detect(String question) {
        // Soruyu küçük harfe çevir ve noktalama işaretlerini kaldır
        String cleanQuestion = question.toLowerCase(Locale.forLanguageTag("tr"))
                                       .replaceAll("[^a-z0-9çğıİöşü ]", "");


        // Kelime ve iki kelimelik dizileri kontrol et
        for (Map.Entry<String, Intent> entry : RULE_SET.entrySet()) {
            if (cleanQuestion.contains(entry.getKey())) {
                // Eşleşen kuralın niyetini döndür
                return entry.getValue();
            }
        }
        
        // Kural eşleşmezse UNKNOWN döndür
        return Intent.UNKNOWN;
    }
}