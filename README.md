# MiniRAG System
## Ana Yönetim ve Akış  
- RagOrchestrator (rag_orchestrator.py): Sistemin "beyni" rolündedir; niyet algılama, sorgu yazımı, veri getirme, sıralama ve cevap üretme aşamalarını sırasıyla yönetir.
- EvalHarness (eval_harness.py): Sistemin performansını test eder; belirli bir soru seti üzerinden "Recall@k" (doğru dokümanı bulma oranı) ve "Latency" (gecikme süresi) metriklerini hesaplar.
- QueryCache (query_cache.py): Daha önce sorulmuş soruları ve verilen cevapları hafızasında tutarak aynı sorular sorulduğunda sistemi tekrar çalıştırmadan hızlı yanıt verir.

## Veri Hazırlama ve İndeksleme
- Chunk (chunker.py): Doküman parçalarını temsil eden bir veri yapısıdır; doküman ID'si, parça ID'si ve metin içeriğini tutar.
- KeywordIndex (keyword_index.py): Ters dizin (inverted index) yapısını yönetir; kelimelerin hangi doküman parçalarında kaç kez geçtiğini (TF - Term Frequency) saklar ve getirir.
- InMemoryVectorIndex (inmemory_vector_index.py): Doküman parçalarını vektör formunda hafızada saklar ve bir sorgu geldiğinde matematiksel benzerlik hesabı yaparak en yakın parçaları bulur.

## Analiz ve İşleme
- RuleBasedIntentDetector (intent_detector.py): Kullanıcının sorusundaki anahtar kelimelere bakarak sorunun kategorisini (Personel, Ders, Kayıt vb.) belirler.
- HeuristicQueryWriter (query_writer.py): Kullanıcı sorusundaki gereksiz kelimeleri temizler ve belirlenen niyete (intent) göre sorguya "booster" terimler (ek anahtar kelimeler) ekler.
- PolicyRoute (policy_route.py): Belirlenen niyete göre hangi arama stratejisinin (Anahtar kelime, Vektör veya Hibrit) kullanılacağına karar verir.

## Arama ve Sıralama
-  (retriever.py): KeywordIndex üzerinden klasik anahtar kelime araması yapar ve doküman parçalarına terim frekansına göre skor verir.
- VectorRetriever (vector_retriever.py): Vektör indeksi kullanarak semantik (anlamsal) arama gerçekleştirir.
- BasicReranker (reranker.py): Getirilen sonuçları kelime yakınlığına (proximity) göre tekrar puanlayarak en alakalı olanları üst sıraya taşır.
- HybridReranker (hybrid_reranker.py): Hem anahtar kelime hem de vektör aramasından gelen sonuçları birleştirerek tek bir puanlanmış liste oluşturur.

## Çıktı ve İzleme
- TemplateAnswerAgent (answer_agent.py): En alakalı doküman parçalarını kullanarak kullanıcıya nihai cevabı hazırlar ve kaynak (citation) bilgilerini ekler.
- TraceBus (trace_bus.py): Sistemin çalışma anındaki tüm olayları (event) yakalar, işlem sürelerini ölçer ve bu bilgileri kayıt sistemine gönderir.
- JsonlTraceSink (jsonl_sink.py): Sistemden gelen izleme (trace) bilgilerini .jsonl formatında log dosyalarına kaydeder.

## Yardımcı Altyapı
- StubEmbeddingProvider (stub_embedding_provider.py): Gerçek bir yapay zeka modeli yerine, metinlerden hızlıca basit vektörler üreten bir test bileşenidir.
- Hit, Answer, Context (models.py): Sistem içindeki veri akışını sağlayan temel veri sınıflarıdır; arama sonuçlarını, nihai cevabı ve tüm işlem sürecinin hafızasını tutarlar.
