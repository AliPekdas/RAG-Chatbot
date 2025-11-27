from dataclasses import dataclass, field
from typing import List, Optional, Any
from intent import Intent

@dataclass
class Hit:
    """Converted from Hit.java"""
    doc_id: str
    chunk_id: int
    score: float
    body: str

    def __str__(self):
        return f"Doc:{self.doc_id} Chunk:{self.chunk_id} Score:{self.score:.2f}"

@dataclass
class Answer:
    """Converted from Answer.java"""
    text: str
    citations: List[str]

    def __str__(self):
        return f"{self.text} {self.citations}"

@dataclass
class Context:
    """Converted from Context.java"""
    original_question: str
    intent: Intent = Intent.UNKNOWN
    query_terms: List[str] = field(default_factory=list)
    hits: List[Hit] = field(default_factory=list)
    final_answer: Optional[Answer] = None