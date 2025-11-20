import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Chunker {

    // --- AYARLAR ---
    // Proje dokümanında 100-150 kelime denmiş.
    private static final int CHUNK_SIZE_WORDS = 120;
    // Örtüşme (Overlap) kesilmeleri önler (50-100 karakter veya ~20-30 kelime).
    private static final int OVERLAP_WORDS = 30;

    // Dosya Yolları
    private static final String INPUT_DIR = "../txt_files";
    private static final String OUTPUT_FILE = "data/corpus.json";

    public static void main(String[] args) {
        System.out.println("Chunking işlemi başlıyor...");
        try {
            // 1. Çıktı klasörünü kontrol et, yoksa oluştur
            Files.createDirectories(Paths.get("data"));

            // 2. txt_files klasöründeki tüm .txt dosyalarını bul
            File folder = new File(INPUT_DIR);
            File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

            if (listOfFiles == null || listOfFiles.length == 0) {
                System.err.println("HATA: txt_files klasöründe hiç .txt dosyası bulunamadı!");
                return;
            }

            List<String> jsonChunks = new ArrayList<>();
            int globalChunkId = 0;

            // 3. Her dosyayı tek tek işle
            for (File file : listOfFiles) {
                System.out.println("İşleniyor: " + file.getName());
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                // --- NORMALİZASYON ---
                // Küçük harfe çevir ve gereksiz boşlukları temizle
                content = content.toLowerCase(Locale.ENGLISH).trim();
                // Satır sonlarını boşlukla değiştir (paragraf bütünlüğü için)
                content = content.replace("\n", " ").replace("\r", " ");
                // Fazla boşlukları tek boşluğa indir
                content = content.replaceAll("\\s+", " ");

                // Dosyayı parçalara ayır (Chunking)
                List<Chunk> fileChunks = createChunks(content, file.getName(), globalChunkId);

                for (Chunk chunk : fileChunks) {
                    jsonChunks.add(chunk.toJson());
                    globalChunkId++;
                }
            }

            // 4. Sonucu JSON Array formatında kaydet
            writeToFile(jsonChunks);
            System.out.println("Bitti! Toplam " + globalChunkId + " adet chunk oluşturuldu.");
            System.out.println("Dosya konumu: " + OUTPUT_FILE);

        } catch (IOException e) {
            System.err.println("HATA: IO hatası oluştu: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    // Metni kelime bazlı kayan pencere (sliding window) ile böler
    private static List<Chunk> createChunks(String content, String docId, int startId) {
        List<Chunk> chunks = new ArrayList<>();
        String[] words = content.split(" "); // Kelimelere ayır

        int currentId = startId;
        int i = 0;

        while (i < words.length) {
            // Bitiş noktasını belirle (Dizi sınırını aşma)
            int end = Math.min(i + CHUNK_SIZE_WORDS, words.length);

            // Kelimeleri tekrar birleştirip metin yap
            StringBuilder chunkTextBuilder = new StringBuilder();
            for (int k = i; k < end; k++) {
                chunkTextBuilder.append(words[k]).append(" ");
            }
            String chunkText = chunkTextBuilder.toString().trim();

            // Çok kısa parçaları (örn. dosya sonundaki 3 kelime) yoksayabiliriz
            if (chunkText.length() > 10) {
                // Offset hesaplama (Basitçe kelime indeksi kullanıyoruz, karakter de olabilir)
                int startOffset = i;
                int endOffset = end;

                Chunk chunk = new Chunk(currentId, docId, chunkText, startOffset, endOffset);
                chunks.add(chunk);
                currentId++;
            }

            // Döngüden çıkış kontrolü
            if (end == words.length) break;

            // Pencereyi kaydır (Overlap mantığı)
            i += (CHUNK_SIZE_WORDS - OVERLAP_WORDS);
        }
        return chunks;
    }

    // JSON dosyasına yazma işlemi
    private static void writeToFile(List<String> jsonObjects) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_FILE), StandardCharsets.UTF_8)) {
            writer.write("[\n");
            for (int i = 0; i < jsonObjects.size(); i++) {
                writer.write("  " + jsonObjects.get(i));
                if (i < jsonObjects.size() - 1) {
                    writer.write(",\n"); // Son eleman hariç virgül koy
                } else {
                    writer.write("\n");
                }
            }
            writer.write("]");
        }
    }

    // Chunk Veri Yapısı (Inner Class)
    static class Chunk {
        int id;
        String docId;
        String text;
        int startOffset;
        int endOffset;

        public Chunk(int id, String docId, String text, int startOffset, int endOffset) {
            this.id = id;
            this.docId = docId;
            this.text = text;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        // Manuel JSON String oluşturucu (Kütüphanesiz)
        // DÜZELTİLMİŞ TOJSON METODU
        public String toJson() {
            // SIRALAMA ÇOK ÖNEMLİ!
            // 1. Önce ters eğik çizgileri (backslash) düzeltmeliyiz.
            // 2. Sonra tırnak işaretlerini düzeltmeliyiz.
            // Aksi takdirde tırnak için koyduğumuz çizgiyi de bozuyoruz.

            String safeText = text
                    .replace("\\", "\\\\")  // Önce backslash'i kaçır
                    .replace("\"", "\\\"")  // Sonra tırnakları kaçır
                    .replace("\n", " ")     // Yeni satır karakterlerini boşluk yap
                    .replace("\r", " ")     // Satır başı karakterlerini boşluk yap
                    .replace("\t", " ");    // Tab karakterlerini boşluk yap

            return String.format(
                    "{\"chunkId\": %d, \"docId\": \"%s\", \"startOffset\": %d, \"endOffset\": %d, \"text\": \"%s\"}",
                    id, docId, startOffset, endOffset, safeText
            );
        }
    }
}

