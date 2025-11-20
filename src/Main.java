import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== RAG SİSTEMİ ===");
        
        // 1. OTOMATİK KURULUM (Veri Yoksa Oluştur)
        ensureDataExists();

        // 2. Başlat
        KeywordIndex index = new KeywordIndex();
        IntentDetector intentDetector = new RuleBasedIntentDetector();
        QueryWriter queryWriter = new HeuristicQueryWriter();
        Retriever retriever = new KeywordRetriever();
        Reranker reranker = new BasicReranker();
        AnswerAgent answerAgent = new TemplateAnswerAgent();
        TraceBus traceBus = new TraceBus();

        RagOrchestrator orchestrator = new RagOrchestrator(
                intentDetector, queryWriter, retriever, reranker, answerAgent, traceBus, index
        );

        // 3. Test
        String soru = (args.length > 1 && args[0].equals("--q")) ? args[1] : "Murat hocanın ofisi nerede?";
        runScenario(orchestrator, soru);
    }

    private static void ensureDataExists() {
        // Dosyaları ../data klasöründe arıyoruz
        File indexFile = new File("../data/index.json");
        
        if (!indexFile.exists()) {
            System.out.println("\n[Sistem]: Veri dosyaları eksik. Otomatik oluşturuluyor...");
            try {
                // Chunker ve IndexBuilder'ı çalıştır
                Chunker.main(new String[]{});
                IndexBuilder.main(new String[]{});
            } catch (Exception e) {
                System.err.println("KURULUM HATASI: " + e.getMessage());
            }
            System.out.println("[Sistem]: Kurulum bitti. Devam ediliyor...\n");
        }
    }

    private static void runScenario(RagOrchestrator orchestrator, String question) {
        System.out.println("SORU: " + question);
        Answer result = orchestrator.run(question);
        System.out.println("CEVAP: " + result.getText());
        System.out.println("KAYNAK: " + result.getCitations());
    }
}
