import java.util.List;
import java.util.Comparator;
import java.util.Collections;

public class BasicReranker implements Reranker {
    
    // Proje dokümanındaki katsayılar 
    // "tf_sum x 10" işlemi Retriever'da yapılmış varsayıyoruz veya burada scale ediyoruz.
    // Burada sadece bonusları ekleyeceğiz.
    private static final double PROXIMITY_BONUS = 5.0; 
    private static final int PROXIMITY_DISTANCE = 15; // 15 karakter veya kelime mesafesi

    @Override
    public List<Hit> rerank(List<Hit> hits, List<String> queryTerms) {
        if (hits == null || hits.isEmpty()) return Collections.emptyList();

        for (Hit hit : hits) {
            double bonus = 0.0;
            String content = hit.body.toLowerCase();

            // --- 1. PROXIMITY BONUS (Yakınlık Bonusu) ---
            // Eğer sorgudaki birden fazla kelime, metin içinde birbirine yakın geçiyorsa
            // Bu basit bir heuristic: Kelimelerin metindeki ilk indekslerine bakıyoruz.
            
            if (queryTerms.size() > 1) {
                boolean foundClose = false;
                // Tüm kelime çiftlerini kontrol et
                for (int i = 0; i < queryTerms.size(); i++) {
                    for (int j = i + 1; j < queryTerms.size(); j++) {
                        String t1 = queryTerms.get(i);
                        String t2 = queryTerms.get(j);
                        
                        int idx1 = content.indexOf(t1);
                        int idx2 = content.indexOf(t2);
                        
                        // İkisi de metinde varsa ve aralarındaki mesafe azsa
                        if (idx1 != -1 && idx2 != -1) {
                            if (Math.abs(idx1 - idx2) <= PROXIMITY_DISTANCE) {
                                foundClose = true;
                                break;
                            }
                        }
                    }
                    if (foundClose) break;
                }
                
                if (foundClose) {
                    bonus += PROXIMITY_BONUS;
                }
            }

            // Mevcut skoru güncelle
            hit.score += bonus;
        }

        // --- 2. YENİDEN SIRALAMA (Tie-Break Kuralları) ---
        // Kural: Score DESC, sonra DocID ASC, sonra ChunkID ASC [cite: 122]
        hits.sort(Comparator.comparingDouble((Hit h) -> h.score).reversed()
                .thenComparingInt(h -> h.docId)
                .thenComparingInt(h -> h.chunkId));

        return hits;
    }
}