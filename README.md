# CSE3063F25Grp12
## Doğukan Şahin  
-Chunker.java (Parçalayıcı):
Ham metin belgelerini okur ve belirlenen boyutta (500 karakter) küçük parçalara (Chunks) ayırır.
Bağlamın kopmaması için parçalar arasında Overlap (100 karakterlik örtüşme) bırakır.
Akıllı Bölme: Kelimelerin ortadan bölünmesini engellemek için kesme işlemini en yakın boşluk karakterine göre yapar.  

-IndexBuilder.java (İndeksleyici):
Oluşturulan chunk'ları analiz eder.
Metni normalize eder (küçük harfe çevirme, noktalama temizliği).
Hangi kelimenin (Token) hangi dökümanda kaç kez geçtiğini hesaplar (Term Frequency) ve arama yapısını inşa eder.  

Sistem, veritabanı yerine iki ana JSON dosyası üzerinden çalışır:  

-corpus.json (Veri Deposu):
Kütüphanenin "rafları" gibidir. Metinlerin gerçek içeriğini saklar.
Her parça için docId, chunkId, başlangıç-bitiş indeksleri ve metnin kendisini tutar. Cevap üretilirken metin buradan çekilir.  

-index.json (Tersine İndeks / Arama Haritası):
Kütüphanenin "kataloğu" gibidir. Metin içermez, sadece adres tutar.
Kelime -> [Bulunduğu Dokümanlar] eşleşmesini tutar.
Örnek: "staj" kelimesi arandığında, sistem bu dosyaya bakarak kelimenin Yonetmelik.txt içinde 10. parçada geçtiğini anında bulur.


