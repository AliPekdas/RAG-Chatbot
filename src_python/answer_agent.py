from abc import ABC, abstractmethod
from typing import List
from models import Hit, Answer


class AnswerAgent(ABC):
    @abstractmethod
    def answer(self, question: str, terms: List[str], ranked_hits: List[Hit]) -> Answer:
        pass


class TemplateAnswerAgent(AnswerAgent):
    def __init__(self, config):
        # Load maxLength from Config
        self.max_length = config['parameters']['answer']['maxLength']

    def answer(self, question: str, terms: List[str], ranked_hits: List[Hit]) -> Answer:
        if not ranked_hits:
            return Answer("Üzgünüm, veritabanında bu konuyla ilgili bilgi bulamadım.", [])

        best_hit = ranked_hits[0]
        content = best_hit.body

        if len(content) > self.max_length:
            content = content[:self.max_length] + "..."

        final_text = f"Cevabınız: {content}"
        citation = f"Doc:{best_hit.doc_id} Chunk:{best_hit.chunk_id}"

        return Answer(final_text, [citation])