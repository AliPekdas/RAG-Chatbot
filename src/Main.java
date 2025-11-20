import java.io.File;
public class Main {
    public static void main(String[] args) {
        // --- 0. BAŞLANGIÇ KONTROLÜ (AUTO-SETUP) ---
        ensureDataExists();

        // --- 1. CLI Parametrelerini Oku ---
        String cliQuestion = null;
        String configFile = "config.yaml";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--q") && i + 1 < args.length) {
                cliQuestion = args[i+1];
            }
            if (args[i].equals("--config") && i + 1 < args.length) {
                configFile = args[i+1];
            }
        }

        // Kullanılan konfigürasyon dosyasını raporla (değişken artık kullanılıyor)
        System.out.println("[Sistem]: Kullanılan config dosyası: " + configFile);

        System.out.println("==================================================");
        System.out.println("   MiniRAG System");
        System.out.println("==================================================");
        
        // 2. Veriyi Yükle
        System.out.println("\n[Sistem]: İndeks yükleniyor...");
        KeywordIndex index = new KeywordIndex();

        // 3. Bileşenleri Oluştur
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

        // 5. Çalıştır
        if (cliQuestion != null) {
            runScenario(orchestrator, cliQuestion);
        } else {
            System.out.println("UYARI: Soru girilmedi. Varsayılan test çalışıyor.");
            runScenario(orchestrator, "Murat hocanın ofisi nerede?");
        }
    }

    // --- YENİ EKLENEN METOT: EKSİK DOSYALARI TAMAMLAR ---
    private static void ensureDataExists() {
        File indexFile = new File("../data/index.json");
        File corpusFile = new File("../data/corpus.json");

        // Eğer indeks veya corpus yoksa, sıfırdan oluştur
        if (!indexFile.exists() || !corpusFile.exists()) {
            System.out.println("\n[Sistem Uyarısı]: İndeks dosyaları bulunamadı.");
            System.out.println("[Sistem]: Otomatik kurulum başlatılıyor (Chunker + IndexBuilder)...");
            
            try {
                // 1. Chunker'ı çalıştır
                System.out.println("   -> Adım 1/2: Metinler parçalanıyor (Chunking)...");
                Chunker.main(new String[]{}); 
                
                // 2. IndexBuilder'ı çalıştır
                System.out.println("   -> Adım 2/2: İndeks oluşturuluyor...");
                IndexBuilder.main(new String[]{});
                
                System.out.println("[Sistem]: Kurulum tamamlandı! Sorguya geçiliyor.\n");
            } catch (Exception e) {
                System.err.println("KRİTİK HATA: Otomatik kurulum başarısız oldu!");
                System.err.println("Lütfen 'txt_files' klasörünün jar ile aynı yerde olduğundan emin olun.");
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
            System.err.println(e.getMessage());
        }
        System.out.println("\n");
    }
}
