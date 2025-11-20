import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndexBuilder {

    private static final String INPUT_FILE = "data/corpus.json";
    private static final String OUTPUT_FILE = "data/index.json";

    // Basit stopwords listesi (İstersen genişletebilirsin)
    private static final Set<String> STOP_WORDS = Set.of(
            "ve", "ile", "bir", "bu", "şu", "o", "de", "da", "ki", "için", "gibi", "ne", "mi", "mı", "mu", "mü", "ama", "fakat", "lakin"
    );

    public static void main(String[] args) {
        System.out.println("IndexBuilder çalışıyor...");

        try {
            // 1. corpus.json dosyasını oku
            Path inputPath = Paths.get(INPUT_FILE);
            if (!Files.exists(inputPath)) {
                System.err.println("HATA: " + INPUT_FILE + " bulunamadı! Önce Chunker çalıştırın.");
                return;
            }
            String jsonContent = Files.readString(inputPath, StandardCharsets.UTF_8);

            // 2. JSON'ı Parse Et (Regex ile manuel parsing)
            // Format: {"chunkId": 0, "docId": "...", ... "text": "..."}
            List<ChunkData> chunks = parseJsonManual(jsonContent);

            // 3. İndeksi Oluştur (Inverted Index)
            // Map<Kelime, List<BulunduğuYer>>
            Map<String, List<Posting>> index = new TreeMap<>(); // TreeMap alfabetik sıralar

            for (ChunkData chunk : chunks) {
                // Metni kelimelere ayır (Sadece harf ve sayıları al, noktalama işaretlerini sil)
                String[] terms = chunk.text.split("[^\\p{L}0-9]+");

                for (String term : terms) {
                    if (term.length() < 2 || STOP_WORDS.contains(term)) continue; // Çok kısa kelimeleri ve bağlaçları atla

                    index.putIfAbsent(term, new ArrayList<>());
                    List<Posting> postings = index.get(term);

                    // Bu kelime bu chunk'ta daha önce geçti mi?
                    Posting existingPosting = null;
                    for (Posting p : postings) {
                        if (p.chunkId == chunk.chunkId) {
                            existingPosting = p;
                            break;
                        }
                    }

                    if (existingPosting != null) {
                        existingPosting.tf++; // Frekansı artır
                    } else {
                        postings.add(new Posting(chunk.docId, chunk.chunkId, 1)); // Yeni kayıt ekle
                    }
                }
            }

            // 4. İndeksi JSON olarak kaydet
            writeIndexToJson(index);
            System.out.println("İndeksleme tamamlandı! Toplam terim sayısı: " + index.size());
            System.out.println("Dosya: " + OUTPUT_FILE);

        } catch (IOException e) {
            System.err.println("HATA: IO hatası oluştu: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    // --- YARDIMCI METOTLAR ---

    // Regex ile basit JSON okuyucu (Kütüphanesiz)
    private static List<ChunkData> parseJsonManual(String jsonContent) {
        List<ChunkData> chunks = new ArrayList<>();
        // Regex: chunkId, docId ve text alanlarını yakalar
        Pattern pattern = Pattern.compile("\"chunkId\":\\s*(\\d+),.*?\"docId\":\\s*\"(.*?)\",.*?\"text\":\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(jsonContent);

        while (matcher.find()) {
            int chunkId = Integer.parseInt(matcher.group(1));
            String docId = matcher.group(2);
            String text = matcher.group(3);
            // Unicode kaçış karakterlerini (varsa) düzeltmek gerekebilir ama şimdilik basit tutalım
            chunks.add(new ChunkData(chunkId, docId, text));
        }
        return chunks;
    }

    // JSON Yazıcı
    private static void writeIndexToJson(Map<String, List<Posting>> index) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_FILE), StandardCharsets.UTF_8)) {
            writer.write("{\n");
            int termCount = 0;
            for (Map.Entry<String, List<Posting>> entry : index.entrySet()) {
                writer.write("  \"" + entry.getKey() + "\": [");

                List<Posting> postings = entry.getValue();
                for (int i = 0; i < postings.size(); i++) {
                    writer.write(postings.get(i).toJson());
                    if (i < postings.size() - 1) writer.write(", ");
                }

                writer.write("]");
                if (++termCount < index.size()) writer.write(",\n");
                else writer.write("\n");
            }
            writer.write("}");
        }
    }

    // Veri sınıfları
    static class ChunkData {
        int chunkId;
        String docId;
        String text;

        public ChunkData(int chunkId, String docId, String text) {
            this.chunkId = chunkId;
            this.docId = docId;
            this.text = text;
        }
    }

    static class Posting {
        String docId;
        int chunkId;
        int tf; // Term Frequency

        public Posting(String docId, int chunkId, int tf) {
            this.docId = docId;
            this.chunkId = chunkId;
            this.tf = tf;
        }

        public String toJson() {
            return String.format("{\"docId\": \"%s\", \"chunkId\": %d, \"tf\": %d}", docId, chunkId, tf);
        }
    }
}
