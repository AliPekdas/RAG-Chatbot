import java.util.*;

public class TemplateAnswerAgent implements AnswerAgent {

    @Override
    public Answer answer(String question, List<String> terms, List<Hit> rankedHits) {
        // 1. Eğer hiç sonuç yoksa
        if (rankedHits == null || rankedHits.isEmpty()) {
            return new Answer("Üzgünüm, veritabanında bu konuyla ilgili bilgi bulamadım.", new ArrayList<>());
        }

        // 2. En iyi sonucu al (Top-1)
        Hit bestHit = rankedHits.get(0);

        // 3. Basit bir cevap şablonu oluştur [cite: 128]
        // Gerçek hayatta burada LLM kullanılır ama proje "Template" istiyor.
        // En iyi chunk'ın ilk cümlesini veya tamamını alıyoruz.
        String content = bestHit.body;
        if (content.length() > 150) {
            content = content.substring(0, 150) + "..."; // Çok uzunsa kısalt
        }

        String finalText = "Cevabınız: " + content;

        // 4. Kaynakça (Citation) Oluştur [cite: 131]
        // Format: docId:chunkId
        String citation = "Doc:" + bestHit.docId + " Chunk:" + bestHit.chunkId;
        List<String> citationList = new ArrayList<>();
        citationList.add(citation);

        return new Answer(finalText, citationList);
    }
}