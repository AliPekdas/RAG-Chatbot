from models import Context, Answer
# Correctly import the classes from their specific files
from intent_detector import IntentDetector
from query_writer import QueryWriter
from retriever import Retriever
from reranker import Reranker
from answer_agent import AnswerAgent
from trace_bus import TraceBus
from keyword_index import KeywordIndex


class RagOrchestrator:
    """Converted from RagOrchestrator.java"""

    def __init__(self,
                 intent_detector: IntentDetector,
                 query_writer: QueryWriter,
                 retriever: Retriever,
                 reranker: Reranker,
                 answer_agent: AnswerAgent,
                 trace_bus: TraceBus,
                 keyword_index: KeywordIndex):
        self.intent_detector = intent_detector
        self.query_writer = query_writer
        self.retriever = retriever
        self.reranker = reranker
        self.answer_agent = answer_agent
        self.trace_bus = trace_bus
        self.keyword_index = keyword_index

    def run(self, question: str) -> Answer:
        print(f"\n>>> Pipeline Starting: {question}")

        context = Context(original_question=question)

        # 1. Intent Detection
        context.intent = self.intent_detector.detect(context.original_question)
        self.trace_bus.publish("IntentDetected", context)

        # 2. Query Generation
        context.query_terms = self.query_writer.write(context.original_question, context.intent)
        self.trace_bus.publish("QueryGenerated", context)

        # 3. Retrieval
        context.hits = self.retriever.retrieve(context.query_terms, self.keyword_index)
        self.trace_bus.publish("Retrieved", context)

        # 4. Reranking
        context.hits = self.reranker.rerank(context.hits, context.query_terms)
        self.trace_bus.publish("Reranked", context)

        # 5. Answer Generation
        context.final_answer = self.answer_agent.answer(
            context.original_question,
            context.query_terms,
            context.hits
        )
        self.trace_bus.publish("Answered", context)

        return context.final_answer