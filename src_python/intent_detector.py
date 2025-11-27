import re
from abc import ABC, abstractmethod
from intent import Intent

class IntentDetector(ABC):
    @abstractmethod
    def detect(self, question: str) -> Intent:
        pass

class RuleBasedIntentDetector(IntentDetector):
    """Converted from RuleBasedIntentDetector.java"""

    RULE_SET = {
        "hoca": Intent.STAFF_LOOKUP, "danışman": Intent.STAFF_LOOKUP,
        "ofis": Intent.STAFF_LOOKUP, "kimin": Intent.STAFF_LOOKUP,
        "zorunlu": Intent.POLICY_FAQ, "yönetmelik": Intent.POLICY_FAQ,
        "staj": Intent.POLICY_FAQ, "sınav": Intent.POLICY_FAQ,
        "not sistemi": Intent.POLICY_FAQ,
        "kayıt": Intent.REGISTRATION, "ders seçimi": Intent.REGISTRATION,
        "harç": Intent.REGISTRATION, "ekle sil": Intent.REGISTRATION,
        "ön şart": Intent.COURSE, "akts": Intent.COURSE,
        "müfredat": Intent.COURSE, "kredisi": Intent.COURSE,
    }

    def detect(self, question: str) -> Intent:
        clean_question = re.sub(r'[^a-z0-9çğıİöşü ]', '', question.lower())
        for keyword, intent in self.RULE_SET.items():
            if keyword in clean_question:
                return intent
        return Intent.UNKNOWN