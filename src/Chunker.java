import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Chunker {

    // --- AYARLAR: RELATIVE PATHS (src klasöründen çalıştırıldığı varsayımıyla) ---
    private static final String INPUT_DIR = "../txt_files";
    private static final String OUTPUT_DIR = "../data";
    private static final String OUTPUT_FILE = "../data/corpus.json";
    
    private static final int CHUNK_SIZE_WORDS = 120;
    private static final int OVERLAP_WORDS = 30;

    public static void main(String[] args) {
        System.out.println("[Chunker] Başlatılıyor...");
        try {
            // 1. Data klasörünü oluştur (Yoksa)
            Path outPath = Paths.get(OUTPUT_DIR);
            if (!Files.exists(outPath)) {
                Files.createDirectories(outPath);
                System.out.println("[Chunker] '../data' klasörü oluşturuldu.");
            }

            // 2. txt_files klasörünü kontrol et
            File folder = new File(INPUT_DIR);
            if (!folder.exists()) {
                // Klasör yoksa oluştur ve kullanıcıyı uyar
                folder.mkdirs();
                System.err.println("HATA: '../txt_files' klasörü boş! Lütfen içine .txt dosyaları ekleyin.");
                return;
            }

            File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
            if (listOfFiles == null || listOfFiles.length == 0) {
                System.err.println("HATA: '../txt_files' içinde .txt dosyası bulunamadı.");
                return;
            }

            List<String> jsonChunks = new ArrayList<>();
            int globalChunkId = 0;

            // 3. Dosyaları İşle
            for (File file : listOfFiles) {
                System.out.println("   -> İşleniyor: " + file.getName());
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                // Normalizasyon
                content = content.toLowerCase(Locale.ENGLISH).trim()
                        .replace("\n", " ").replace("\r", " ").replaceAll("\\s+", " ");

                List<Chunk> fileChunks = createChunks(content, file.getName(), globalChunkId);
                for (Chunk chunk : fileChunks) {
                    jsonChunks.add(chunk.toJson());
                    globalChunkId++;
                }
            }

            // 4. Kaydet
            writeToFile(jsonChunks);
            System.out.println("[Chunker] Tamamlandı. Toplam Chunk: " + globalChunkId);

        } catch (IOException e) {
            System.err.println("[Chunker] Hata: " + e.getMessage());
        }
    }

    private static List<Chunk> createChunks(String content, String docId, int startId) {
        List<Chunk> chunks = new ArrayList<>();
        String[] words = content.split(" ");
        int i = 0, currentId = startId;

        while (i < words.length) {
            int end = Math.min(i + CHUNK_SIZE_WORDS, words.length);
            StringBuilder sb = new StringBuilder();
            for (int k = i; k < end; k++) sb.append(words[k]).append(" ");
            
            String text = sb.toString().trim();
            if (text.length() > 10) {
                chunks.add(new Chunk(currentId++, docId, text));
            }
            if (end == words.length) break;
            i += (CHUNK_SIZE_WORDS - OVERLAP_WORDS);
        }
        return chunks;
    }

    private static void writeToFile(List<String> jsonObjects) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_FILE), StandardCharsets.UTF_8)) {
            writer.write("[\n");
            for (int i = 0; i < jsonObjects.size(); i++) {
                writer.write("  " + jsonObjects.get(i));
                writer.write(i < jsonObjects.size() - 1 ? ",\n" : "\n");
            }
            writer.write("]");
        }
    }

    static class Chunk {
        int id; String docId; String text;
        public Chunk(int id, String docId, String text) { this.id = id; this.docId = docId; this.text = text; }
        
        public String toJson() {
            String safeText = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", " ");
            return String.format("{\"chunkId\": %d, \"docId\": \"%s\", \"text\": \"%s\"}", id, docId, safeText);
        }
    }
}
