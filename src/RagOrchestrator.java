public class RagOrchestrator {

    private final IntentDetector intentDetector;
    private final QueryWriter queryWriter;
    private final Retriever retriever;
    private final Reranker reranker;
    private final AnswerAgent answerAgent;
    private final TraceBus traceBus;
    private final KeywordIndex keywordIndex;

    public RagOrchestrator(IntentDetector intentDetector,
                           QueryWriter queryWriter,
                           Retriever retriever,
                           Reranker reranker,
                           AnswerAgent answerAgent,
                           TraceBus traceBus,
                           KeywordIndex keywordIndex) {
        this.intentDetector = intentDetector;
        this.queryWriter = queryWriter;
        this.retriever = retriever;
        this.reranker = reranker;
        this.answerAgent = answerAgent;
        this.traceBus = traceBus;
        this.keywordIndex = keywordIndex;
    }

    // DÖNÜŞ TİPİ DEĞİŞTİ: String -> Answer
    public Answer run(String question) {
        System.out.println("\n>>> Pipeline Başlatılıyor: " + question);
        
        Context context = new Context(question);

        // 1. Niyet Tespiti
        context.intent = intentDetector.detect(context.originalQuestion);
        traceBus.publish("IntentDetected", context);

        // 2. Sorgu Oluşturma
        context.queryTerms = queryWriter.write(context.originalQuestion, context.intent);
        traceBus.publish("QueryGenerated", context);

        // 3. Arama (Retrieval)
        context.hits = retriever.retrieve(context.queryTerms, keywordIndex);
        traceBus.publish("Retrieved", context);

        // 4. Sıralama (Reranking)
        context.hits = reranker.rerank(context.hits, context.queryTerms);
        traceBus.publish("Reranked", context);

        // 5. Cevap Oluşturma
        context.finalAnswer = answerAgent.answer(
            context.originalQuestion, 
            context.queryTerms, 
            context.hits
        );
        traceBus.publish("Answered", context);

        // ARTIK NESNEYİ DÖNDÜRÜYORUZ (Metin + Kaynakça)
        return context.finalAnswer;
    }
}
