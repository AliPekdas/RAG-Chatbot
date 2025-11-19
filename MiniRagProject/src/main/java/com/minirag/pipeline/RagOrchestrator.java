package com.minirag.pipeline;

import com.minirag.model.Context;
import java.util.List;

public class RagOrchestrator {
    
    // Bu değişkenler "Interface" tipindedir. 
    // Yani RagOrchestrator, arka planda hangi algoritmanın çalıştığını bilmez (Polymorphism).
    private final IntentDetector intentDetector;
    private final QueryWriter queryWriter;
    private final Retriever retriever;
    private final Reranker reranker;
    private final AnswerAgent answerAgent;
    private final TraceBus traceBus;

    // Constructor (Dependency Injection)
    // Tüm parçaları dışarıdan alır ve birleştirir.
    public RagOrchestrator(IntentDetector intentDetector,
                           QueryWriter queryWriter,
                           Retriever retriever,
                           Reranker reranker,
                           AnswerAgent answerAgent,
                           TraceBus traceBus) {
        this.intentDetector = intentDetector;
        this.queryWriter = queryWriter;
        this.retriever = retriever;
        this.reranker = reranker;
        this.answerAgent = answerAgent;
        this.traceBus = traceBus;
    }

    // Template Method: İş akışını yönetir
    public String run(String question) {
        System.out.println("Pipeline basliyor: " + question);
        
        // 0. Context oluştur (Tüm veriyi taşıyan kutu)
        Context context = new Context(question);

        // 1. Niyet Tespiti (Intent Detection)
        context.intent = intentDetector.detect(context.originalQuestion);
        traceBus.publish("IntentDetected", context);

        // 2. Sorgu Oluşturma (Query Writing)
        context.queryTerms = queryWriter.write(context.originalQuestion, context.intent);
        traceBus.publish("QueryGenerated", context);

        // 3. Arama (Retrieval)
        context.hits = retriever.retrieve(context.queryTerms);
        traceBus.publish("Retrieved", context);

        // 4. Sıralama (Reranking)
        context.hits = reranker.rerank(context.hits, context.queryTerms);
        traceBus.publish("Reranked", context);

        // 5. Cevap Oluşturma (Answer Generation)
        context.finalAnswer = answerAgent.answer(context);
        traceBus.publish("Answered", context);

        return context.finalAnswer;
    }
}