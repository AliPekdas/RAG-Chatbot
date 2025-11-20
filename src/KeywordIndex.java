import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordIndex {

    // Dosya yolları
    private static final String INDEX_PATH = "../data/index.json";
    private static final String CORPUS_PATH = "../data/corpus.json";

    // 1. Arama İndeksi: Kelime -> [ (docId, chunkId, tf), ... ]
    private final Map<String, List<IndexEntry>> indexMap = new HashMap<>();
    
    // 2. Metin Deposu: "DosyaIsmi_ChunkId" -> Metnin Kendisi
    private final Map<String, String> chunkStore = new HashMap<>();

    // 3. ID Çevirici: int (Hash) -> String (Dosya İsmi)
    private final Map<Integer, String> docIdLookup = new HashMap<>();

    public KeywordIndex() {
        loadCorpus();
        loadIndex();
    }

    // --- KULLANILAN METOTLAR ---

    public List<IndexEntry> getEntries(String term) {
        // Locale.ENGLISH, Türkçe karakter sorunlarını (I-ı dönüşümü) önler
        return indexMap.getOrDefault(term.toLowerCase(Locale.ENGLISH), Collections.emptyList());
    }

    public String getChunkText(int docIdInt, int chunkId) {
        String docIdStr = docIdLookup.get(docIdInt);
        if (docIdStr == null) {
            return "HATA: Bilinmeyen Doküman ID: " + docIdInt;
        }
        String key = docIdStr + "_" + chunkId;
        return chunkStore.getOrDefault(key, "Metin içeriği bulunamadı (Key: " + key + ")");
    }

    // --- DÜZELTİLMİŞ DOSYA OKUMA ---

    private void loadCorpus() {
        try {
            Path path = Paths.get(CORPUS_PATH);
            if (!Files.exists(path)) {
                System.err.println("UYARI: " + CORPUS_PATH + " bulunamadı. Önce Chunker çalıştırın.");
                return;
            }

            String content = Files.readString(path, StandardCharsets.UTF_8);
            Pattern p = Pattern.compile("\"chunkId\":\\s*(\\d+),.*?\"docId\":\\s*\"(.*?)\".*?\"text\":\\s*\"(.*?)\"");
            Matcher m = p.matcher(content);

            while (m.find()) {
                String chunkId = m.group(1);
                String docIdStr = m.group(2);
                String text = m.group(3).replace("\\\"", "\"").replace("\\\\", "\\");
                
                chunkStore.put(docIdStr + "_" + chunkId, text);
                int docIdInt = Math.abs(docIdStr.hashCode());
                docIdLookup.put(docIdInt, docIdStr);
            }
            System.out.println("KeywordIndex: Corpus yüklendi (" + chunkStore.size() + " parça).");

        } catch (IOException e) {
            System.err.println("HATA: IO hatası oluştu: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private void loadIndex() {
        try {
            Path path = Paths.get(INDEX_PATH);
            if (!Files.exists(path)) {
                System.err.println("UYARI: " + INDEX_PATH + " bulunamadı. Önce IndexBuilder çalıştırın.");
                return;
            }
            
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            String currentTerm = null;
            
            // Regex: {"docId": "...", "chunkId": 0, "tf": 1}
            Pattern postingPattern = Pattern.compile("\\{\"docId\":\\s*\"(.*?)\",\\s*\"chunkId\":\\s*(\\d+),\\s*\"tf\":\\s*(\\d+)\\}");

            for (String line : lines) {
                line = line.trim();
                
                // 1. SATIRDA TERİM VAR MI?
                if (line.startsWith("\"")) {
                    int colonIndex = line.indexOf("\":");
                    if (colonIndex > 0) {
                        currentTerm = line.substring(1, colonIndex); // Tırnaklar arasındaki kelimeyi al
                        indexMap.putIfAbsent(currentTerm, new ArrayList<>());
                    }
                }

                // 2. SATIRDA VERİ VAR MI? (Aynı satırda olabilir, bu yüzden 'else if' KULLANMIYORUZ)
                if (currentTerm != null && line.contains("{")) {
                    Matcher m = postingPattern.matcher(line);
                    while (m.find()) { // Satırdaki TÜM eşleşmeleri bul
                        String docIdStr = m.group(1);
                        int chunkId = Integer.parseInt(m.group(2));
                        int tf = Integer.parseInt(m.group(3));
                        
                        int docIdInt = Math.abs(docIdStr.hashCode());
                        indexMap.get(currentTerm).add(new IndexEntry(docIdInt, chunkId, tf));
                    }
                }
            }
            System.out.println("KeywordIndex: İndeks yüklendi (" + indexMap.size() + " kelime).");

        } catch (IOException e) {
            System.err.println("HATA: IO hatası oluştu: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    // Inner Class
    public static class IndexEntry {
        public int docId;
        public int chunkId;
        public int termFrequency;

        public IndexEntry(int docId, int chunkId, int tf) {
            this.docId = docId;
            this.chunkId = chunkId;
            this.termFrequency = tf;
        }
    }

}
