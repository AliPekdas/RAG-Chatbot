from abc import ABC, abstractmethod
from typing import List, Dict
from models import Hit
from keyword_index import KeywordIndex


class Retriever(ABC):
    @abstractmethod
    def retrieve(self, query_terms: List[str], index: KeywordIndex) -> List[Hit]:
        pass


class KeywordRetriever(Retriever):
    def __init__(self, config):
        # Load maxResults from Config
        self.max_results = config['parameters']['retriever']['maxResults']

    def retrieve(self, query_terms: List[str], index: KeywordIndex) -> List[Hit]:
        hits_map: Dict[str, Hit] = {}

        for term in query_terms:
            entries = index.get_entries(term)
            for entry in entries:
                key = f"{entry.doc_id}_{entry.chunk_id}"
                if key not in hits_map:
                    body_text = index.get_chunk_text(entry.doc_id, entry.chunk_id)
                    hits_map[key] = Hit(entry.doc_id, entry.chunk_id, 0.0, body_text)
                hits_map[key].score += entry.term_frequency

        results = list(hits_map.values())
        results.sort(key=lambda h: (-h.score, h.doc_id, h.chunk_id))

        return results[:self.max_results]