import os
from abc import ABC, abstractmethod
from typing import List
from models import Hit, Answer

# Try imports for both providers
try:
    from openai import OpenAI
except ImportError:
    OpenAI = None

try:
    import google.generativeai as genai
except ImportError:
    genai = None


class AnswerAgent(ABC):
    @abstractmethod
    def answer(self, question: str, terms: List[str], ranked_hits: List[Hit]) -> Answer:
        pass


# --- Template Agent (Backup) ---
class TemplateAnswerAgent(AnswerAgent):
    def __init__(self, config):
        self.max_length = config['parameters']['answer']['maxLength']

    def answer(self, question: str, terms: List[str], ranked_hits: List[Hit]) -> Answer:
        if not ranked_hits:
            return Answer("Üzgünüm, veritabanında bilgi yok.", [])

        best_hit = ranked_hits[0]
        content = best_hit.body[:self.max_length] + "..."
        return Answer(f"Cevabınız: {content}", [f"Doc:{best_hit.doc_id}"])


# --- NEW: Universal LLM Agent (Supports OpenAI & Gemini) ---
class LlmAnswerAgent(AnswerAgent):
    def __init__(self, config):
        self.provider = config['parameters']['llm']['provider']
        self.api_key = config['parameters']['llm']['apiKey']
        self.model_name = config['parameters']['llm']['model']
        self.max_tokens = config['parameters']['llm'].get('maxTokens', 300)

        if self.provider == "openai":
            if OpenAI is None: raise ImportError("Run 'pip install openai'")
            self.client = OpenAI(api_key=self.api_key)

        elif self.provider == "gemini":
            if genai is None: raise ImportError("Run 'pip install google-generativeai'")
            genai.configure(api_key=self.api_key)
            self.model = genai.GenerativeModel(self.model_name)

    def answer(self, question: str, terms: List[str], ranked_hits: List[Hit]) -> Answer:
        if not ranked_hits:
            return Answer("Üzgünüm, context içinde bilgi bulunamadı.", [])

        # 1. Prepare Context
        context_text = "\n\n".join(
            [f"--- Document (ID: {h.doc_id}) ---\n{h.body}" for h in ranked_hits[:3]]
        )
        citations = [f"Doc:{h.doc_id} Chunk:{h.chunk_id}" for h in ranked_hits[:3]]

        # 2. Construct Prompt
        system_prompt = (
            "You are a helpful assistant. "
            "Answer the question based ONLY on the provided context below. "
            "If the answer is not in the context, say 'I don't know'."
        )
        full_prompt = f"{system_prompt}\n\nCONTEXT:\n{context_text}\n\nQUESTION: {question}"

        answer_text = "Error generating response."

        # 3. Call API based on Provider
        try:
            if self.provider == "openai":
                response = self.client.chat.completions.create(
                    model=self.model_name,
                    messages=[{"role": "user", "content": full_prompt}],
                    max_tokens=self.max_tokens
                )
                answer_text = response.choices[0].message.content.strip()

            elif self.provider == "gemini":
                # Gemini doesn't use a separate "system" role in the simplest API,
                # so we just combine everything into the prompt.
                response = self.model.generate_content(full_prompt)
                answer_text = response.text.strip()

        except Exception as e:
            answer_text = f"LLM Error ({self.provider}): {str(e)}"

        return Answer(answer_text, citations)
