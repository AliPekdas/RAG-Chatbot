from typing import Optional
from models import Answer

class QueryCache:
    """Daha önce verilmiş cevapları hafızasında tutar."""
    
    def __init__(self):
        # Soru-Cevap çiftlerini saklayan sözlük
        self._cache = {}

    def get_answer(self, question: str) -> Optional[Answer]:
        # Normalize edilmiş soru ile cache kontrolü
        query = question.lower().strip()
        return self._cache.get(query)

    def put_answer(self, question: str, answer: Answer):
        # Cevabı cache'e kaydet
        query = question.lower().strip()
        self._cache[query] = answer