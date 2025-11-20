public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   RAG");
        System.out.println("==================================================");

        // 1. Veriyi Yükle
        System.out.println("\n[Sistem]: Veri yükleniyor...");
        KeywordIndex index = new KeywordIndex();

        // 2. Gerçek Bileşenleri Oluştur
        
        IntentDetector intentDetector = new RuleBasedIntentDetector();
        QueryWriter queryWriter = new HeuristicQueryWriter();
        Retriever retriever = new KeywordRetriever();
        Reranker reranker = new BasicReranker();
        AnswerAgent answerAgent = new TemplateAnswerAgent();
        TraceBus traceBus = new TraceBus();

        // 3. Orkestratörü Kur
        RagOrchestrator orchestrator = new RagOrchestrator(
                intentDetector,
                queryWriter,
                retriever,
                reranker,
                answerAgent,
                traceBus,
                index
        );

        System.out.println("[Sistem]: Hazır! Senaryolar çalıştırılıyor.\n");

        // 4. Senaryoları Test Et
        runScenario(orchestrator, "Murat hocanın ofisi nerede?");
        runScenario(orchestrator, "Staj zorunlu mu?");
        runScenario(orchestrator, "CSE3063 dersinin ön koşulu nedir?");
    }

    private static void runScenario(RagOrchestrator orchestrator, String question) {
        System.out.println("##################################################");
        System.out.println("SORU: " + question);
        
        // Cevabı al (Answer nesnesi döner)
        Answer result = orchestrator.run(question);

        System.out.println("\n>>> SONUÇ ÇIKTISI <<<");
        System.out.println("📝 Cevap: " + result.getText());
        System.out.println("📚 Kaynak: " + result.getCitations());
        System.out.println("\n");
    }
}