import re
from abc import ABC, abstractmethod
from typing import List
from intent import Intent


class QueryWriter(ABC):
    @abstractmethod
    def write(self, question: str, intent: Intent) -> List[str]:
        pass


class HeuristicQueryWriter(QueryWriter):
    """Converted from HeuristicQueryWriter.java"""

    STOP_WORDS = {
        "the", "a", "an", "this", "that", "these", "those", "all", "any", "both", "each",
        "of", "in", "on", "at", "to", "for", "with", "by", "from", "up", "down", "about",
        "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your",
        "be", "been", "being", "am", "is", "are", "was", "were", "have", "has", "had",
        "and", "but", "if", "or", "because", "as", "until", "while", "when",
        "what", "which", "who", "whom", "whose", "please", "tell", "ask", "question",
        "ve", "veya", "ile", "ama", "fakat", "lakin", "ancak", "çünkü", "oysa",
        "yada", "yahut", "ise", "de", "da", "ki", "diye", "üzere", "için", "gibi",
        "bu", "şu", "o", "bunlar", "şunlar", "onlar", "bunu", "şunu", "onu",
        "ne", "nedir", "neyi", "kim", "kimdir", "kime", "hangi", "hangisi",
        "mı", "mi", "mu", "mü", "mıdır", "midir", "mudur", "müdür",
        "bir", "tek", "ilk", "son", "önce", "sonra", "daha", "en", "çok", "az",
        "var", "yok", "olan", "olarak", "olduğu", "ilgili", "hakkında"
    }

    def write(self, question: str, intent: Intent) -> List[str]:
        clean = re.sub(r'[^a-z0-9çğıİöşü ]', '', question.lower())
        tokens = clean.split()
        terms = [t for t in tokens if t not in self.STOP_WORDS and len(t) > 1]

        # Intent Booster
        if intent == Intent.STAFF_LOOKUP:
            terms.extend(["staff", "advisor", "office", "contact", "email", "danışman", "hocası"])
        elif intent == Intent.REGISTRATION:
            terms.extend(["registration", "enrollment", "course selection", "ders seçimi",
                          "kayıt yenileme", "add drop", "harç"])
        elif intent == Intent.COURSE:
            terms.extend(["syllabus", "curriculum", "prerequisite", "ön şart", "credit",
                          "ects", "akts", "müfredat"])
        elif intent == Intent.POLICY_FAQ:
            terms.extend(["regulation", "directive", "yönerge", "mevzuat", "yönetmelik",
                          "exam", "sınav", "grading", "internship", "staj"])
        return terms