# CSE3063F25Grp12
## Ebubekir Bağdaş  
- RagOrchestrator.java (Merkezi Kontrolör):  
GRASP Controller desenini uygular ve tüm RAG akışını (Pipeline) yönetir.  
Template Method benzeri bir yapıyla, işlemleri belirli bir sırada (Niyet Tespiti - Sorgu Yazma - Arama - Sıralama - Cevaplama) çalıştırır.  
Dependency Injection (Bağımlılık Enjeksiyonu) kullanarak tüm stratejileri (Retriever, QueryWriter vb.) yapıcı metodunda alır, bu da sistemi gevşek bağlı (Low Coupling) tutar.  

- Context.java (Bağlam/Veri Taşıyıcı):  
Pipeline'ın aşamaları arasında veri taşınmasını sağlayan Data Transfer Object (DTO) yapısıdır.  
İşlem boyunca oluşan durumu (Kullanıcı sorusu, tespit edilen niyet, arama terimleri, bulunan sonuçlar ve final cevap) merkezi bir nesnede tutar.  
Metotların parametre sayısını azaltır ve aşamalar arasındaki veri akışını standartlaştırır.  

- TraceBus.java (İzleme ve Loglama):  
Observer Pattern (Gözlemci Deseni) mantığıyla çalışan basitleştirilmiş bir olay (event) yayıncısıdır.  
Sistemin her aşamasında (QueryGenerated, Retrieved vb.) tetiklenerek, o anki Context durumunu ve işlem detaylarını (bulunan sonuç sayısı, üretilen terimler) konsola basar.  
Hata ayıklama ve sistemin şeffaflığı (Traceability) için kullanılır.  

- Hit.java (Arama Sonucu Modeli):  
Arama motorunun bulduğu tek bir doküman parçasını temsil eden Domain Model sınıfıdır.  
Parçanın kimlik bilgilerini (docId, chunkId), içeriğini (body) ve alaka düzeyini belirten puanını (score) saklar.  
Sıralama (Ranking) algoritmaları bu nesneler üzerindeki score değerini değiştirerek çalışır4.

## Doğukan Şahin  
- Chunker.java (Parçalayıcı):
  Ham metin belgelerini okur ve belirlenen boyutta (500 karakter) küçük parçalara (Chunks) ayırır.
    Bağlamın kopmaması için parçalar arasında Overlap (100 karakterlik örtüşme) bırakır.
    Akıllı Bölme: Kelimelerin ortadan bölünmesini engellemek için kesme işlemini en yakın boşluk karakterine göre yapar.  

- IndexBuilder.java (İndeksleyici):
  Oluşturulan chunk'ları analiz eder.
  Metni normalize eder (küçük harfe çevirme, noktalama temizliği).
  Hangi kelimenin (Token) hangi dökümanda kaç kez geçtiğini hesaplar (Term Frequency) ve arama yapısını inşa eder.

Sistem, veritabanı yerine iki ana JSON dosyası üzerinden çalışır:  

- corpus.json (Veri Deposu):
  Kütüphanenin "rafları" gibidir. Metinlerin gerçek içeriğini saklar.
  Her parça için docId, chunkId, başlangıç-bitiş indeksleri ve metnin kendisini tutar. Cevap üretilirken metin buradan çekilir.  

- index.json (Tersine İndeks / Arama Haritası):
  Kütüphanenin "kataloğu" gibidir. Metin içermez, sadece adres tutar.
  Kelime -> [Bulunduğu Dokümanlar] eşleşmesini tutar.
  Örnek: "staj" kelimesi arandığında, sistem bu dosyaya bakarak kelimenin Yonetmelik.txt içinde 10. parçada geçtiğini anında bulur.  
  
## Ali Doğan Pekdaş  
- QueryWriter.java & Retriever.java (Arayüzler):  
Sistemin modüler olmasını sağlayan soyut katmanlardır.  
Strategy Design Pattern uygulanarak, ileride farklı algoritmaların (örneğin Vektör tabanlı arama veya LLM tabanlı sorgu yazma) mevcut kodu bozmadan sisteme entegre edilebilmesini sağlar.  

- HeuristicQueryWriter.java (Sezgisel Sorgu Yazıcı):  
Kullanıcının girdiği ham soruyu normalize eder (küçük harfe çevirme, noktalama işaretlerini temizleme
Türkçe ve İngilizce için hazırlanmış geniş kapsamlı bir Stop-Word (Yasaklı Kelime) listesi kullanarak, arama kalitesini düşüren bağlaç ve zamirleri eler.  
Intent Booster: Tespit edilen niyete (Intent) göre, sorguya konuyla ilgili ekstra anahtar kelimeler (Örn: STAFF_LOOKUP için "office", "email") ekleyerek kapsamı genişletir.  

- KeywordRetriever.java (Anahtar Kelime Getirici):  
Temizlenmiş sorgu terimlerini KeywordIndex üzerinde arar.  
Eşleşen her doküman parçası (Chunk) için Terim Frekansı (TF) toplamına dayalı bir skor hesaplar.  
Bulunan sonuçları proje gereksinimlerine uygun Deterministik Tie-Break kurallarına göre sıralar: Önce Skor (Azalan), eşitlik varsa DocID (Artan), sonra ChunkID (Artan).  
En alakalı ilk 10 sonucu (Top-K) döndürür.  

- KeywordIndex.java (Veri Katmanı):  
Uygulama başladığında diskteki işlenmiş verileri (corpus.json ve index.json) okuyarak belleğe (In-Memory) yükler.  
Verilere O(1) karmaşıklığında hızlı erişim sağlamak için HashMap yapılarını kullanır.  
Dosya isimleri (String) ile sistemin kullandığı sayısal ID'ler (int) arasındaki dönüşümü yönetir ("Look-Up" tablosu).  

- Intent.java (Niyet Enumu):  
Sistemin desteklediği kullanıcı niyetlerini (Örn: REGISTRATION, STAFF_LOOKUP, POLICY_FAQ) tanımlayan sabit veri yapısıdır.  
Sorgu genişletme (Query Expansion) ve akış yönlendirme aşamalarında kullanılır.  

## İlker Elgin
-
