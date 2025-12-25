from intent import Intent

class PolicyRoute:
    """Kullanıcının niyetine (Intent) göre arama yöntemine karar verir."""
    
    def decide_strategy(self, intent: Intent) -> str:
        # Niyete göre strateji belirleme
        if intent == Intent.POLICY_FAQ:
            return "KEYWORD"
        elif intent == Intent.STAFF_LOOKUP:
            return "VECTOR"
        elif intent == Intent.COURSE:
            return "VECTOR"
        elif intent == Intent.REGISTRATION:
            return "HYBRID"
        else:
            return "KEYWORD"
