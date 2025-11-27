from abc import ABC, abstractmethod
from typing import List
from models import Hit


class Reranker(ABC):
    @abstractmethod
    def rerank(self, hits: List[Hit], query_terms: List[str]) -> List[Hit]:
        pass


class BasicReranker(Reranker):
    def __init__(self, config):
        # Load Parameters from Config
        params = config['parameters']['reranker']
        self.proximity_bonus = params['proximityBonus']
        self.proximity_distance = params['proximityDistance']

    def rerank(self, hits: List[Hit], query_terms: List[str]) -> List[Hit]:
        if not hits: return []

        for hit in hits:
            bonus = 0.0
            content = hit.body.lower()

            if len(query_terms) > 1:
                found_close = False
                for i in range(len(query_terms)):
                    for j in range(i + 1, len(query_terms)):
                        idx1 = content.find(query_terms[i])
                        idx2 = content.find(query_terms[j])
                        if idx1 != -1 and idx2 != -1:
                            if abs(idx1 - idx2) <= self.proximity_distance:
                                found_close = True
                                break
                    if found_close: break

                if found_close: bonus += self.proximity_bonus

            hit.score += bonus

        hits.sort(key=lambda h: (-h.score, h.doc_id, h.chunk_id))
        return hits