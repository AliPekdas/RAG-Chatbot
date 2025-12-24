from typing import List
from models import Hit

class HybridReranker:
    """Keyword ve Vector sonuçlarını normalize eder ve birleştirir."""
    
    def combine_and_rank(self, keyword_hits: List[Hit], vector_hits: List[Hit]) -> List[Hit]:
        combined_map = {}

        # Keyword sonuçlarını haritaya ekle
        for hit in keyword_hits:
            key = f"{hit.doc_id}_{hit.chunk_id}"
            combined_map[key] = hit

        # Vektör sonuçlarını birleştir ve puanları güncelle
        for hit in vector_hits:
            key = f"{hit.doc_id}_{hit.chunk_id}"
            if key in combined_map:
                # Mevcut skora vektör skorunu ekleyerek harmanlama
                combined_map[key].score += hit.score
            else:
                combined_map[key] = hit

        # Tek bir listede birleştir ve sırala
        results = list(combined_map.values())
        results.sort(key=lambda h: (-h.score, h.doc_id, h.chunk_id))
        
        return results