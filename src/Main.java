import java.io.File;

public class Main {
    public static void main(String[] args) {
        // 1. Sadece '--q' Parametresini Oku (Config'i sildik)
        String cliQuestion = null;

        for (int i = 0; i < args.length; i++) {
            // Sadece soruyu alıyoruz
            if (args[i].equals("--q") && i + 1 < args.length) {
                cliQuestion = args[i+1];
            }
        }

        System.out.println("==================================================");
        System.out.println("   RAG SİSTEMİ");
        System.out.println("==================================================");

        // KURAL: Soru yoksa hata ver ve kapat
        if (cliQuestion == null || cliQuestion.trim().isEmpty()) {
            System.err.println("\n❌ HATA: Soru belirtilmedi!");
            System.err.println("Kullanım: java -jar rag.jar --q \"Sorunuz burada\"");
            System.exit(1); 
        }

        // 2. Veri Dosyalarını Kontrol Et
        ensureDataExists();
        
        // 3. Bileşenleri Başlat
        System.out.println("[Sistem]: Başlatılıyor...");
        KeywordIndex index = new KeywordIndex(); // Veriyi yükle

        // Gerçek sınıfları kullanıyoruz
        IntentDetector intentDetector = new RuleBasedIntentDetector();
        QueryWriter queryWriter = new HeuristicQueryWriter();
        Retriever retriever = new KeywordRetriever();
        Reranker reranker = new BasicReranker();
        AnswerAgent answerAgent = new TemplateAnswerAgent();
        TraceBus traceBus = new TraceBus();

        // 4. Orkestratörü Kur
        RagOrchestrator orchestrator = new RagOrchestrator(
                intentDetector,
                queryWriter,
                retriever,
                reranker,
                answerAgent,
                traceBus,
                index
        );

        // 5. Soruyu Çalıştır
        runScenario(orchestrator, cliQuestion);
    }

    // --- YARDIMCI METOTLAR ---

    private static void ensureDataExists() {
        File indexFile = new File("../data/index.json");
        
        if (!indexFile.exists()) {
            System.out.println("[Sistem]: Veri dosyaları eksik. Otomatik oluşturuluyor...");
            try {
                Chunker.main(new String[]{}); 
                IndexBuilder.main(new String[]{});
                System.out.println("[Sistem]: Veri hazırlandı.\n");
            } catch (Exception e) {
                System.err.println("KRİTİK HATA: Veri oluşturulamadı.");
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }

    private static void runScenario(RagOrchestrator orchestrator, String question) {
        System.out.println("##################################################");
        System.out.println("SORU: " + question);
        try {
            Answer result = orchestrator.run(question);
            System.out.println("\n>>> SONUÇ ÇIKTISI <<<");
            System.out.println("📝 Cevap: " + result.getText());
            System.out.println("📚 Kaynak: " + result.getCitations());
        } catch (Exception e) {
            System.err.println("HATA: " + e.getMessage());
        }
        System.out.println("\n");
    }
}
