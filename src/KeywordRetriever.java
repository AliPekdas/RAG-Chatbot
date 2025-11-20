import java.util.*;

public class KeywordRetriever implements Retriever {

    @Override
    public List<Hit> retrieve(List<String> queryTerms, KeywordIndex index) {
        Map<String, Hit> hitsMap = new HashMap<>();

        // 1. Search the index for each search term
        for (String term : queryTerms) {
            
            // Get entries for the term
            List<KeywordIndex.IndexEntry> entries = index.getEntries(term);
            
            // Process each entry
            for (KeywordIndex.IndexEntry entry : entries) {
                String key = entry.docId + "_" + entry.chunkId;
                
                // If it exists, take it; if not, create it.
                Hit hit = hitsMap.computeIfAbsent(key, k -> 
                    new Hit(entry.docId, entry.chunkId, 0.0, index.getChunkText(entry.docId, entry.chunkId))
                );
                
                // Add score (Sum of TF)
                hit.score += entry.termFrequency;
            }
        }

        // 2. Convert to list
        List<Hit> results = new ArrayList<>(hitsMap.values());

        // 3. Ranking: Score DESC, then DocID ASC, then ChunkID ASC
        results.sort(Comparator.comparingDouble((Hit h) -> h.score).reversed() // Score DESC
                .thenComparingInt(h -> h.docId)       // DocID ASC
                .thenComparingInt(h -> h.chunkId));   // ChunkID ASC

        // 4. Return the first 10 result
        int K = 10;
        if (results.size() > K) {
            return results.subList(0, K);
        }
        return results;
    }
}