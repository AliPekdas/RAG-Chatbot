import json
import time
import argparse
from pathlib import Path
from rag_orchestrator import RagOrchestrator
import chunker
import index_builder
from config_loader import load_config
from keyword_index import KeywordIndex
from trace_bus import TraceBus
from rag_orchestrator import RagOrchestrator
from intent_detector import RuleBasedIntentDetector
from query_writer import HeuristicQueryWriter
from retriever import KeywordRetriever
from reranker import BasicReranker
from answer_agent import TemplateAnswerAgent

class EvalHarness:
    def __init__(self, orchestrator: RagOrchestrator):
        self.orchestrator = orchestrator

    def run_evaluation(self, questions_file_path: str):
        print(f"[Eval] Starting evaluation using {questions_file_path}...")
        
        path = Path(questions_file_path)
        if not path.exists():
            print("ERROR: Questions file not found!")
            return

        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)

        total_questions = len(data)
        correct_retrieval = 0
        total_latency = 0.0

        print(f"\n{'ID':<5} {'Question':<40} {'Expected':<15} {'Found':<15} {'Time(ms)':<10} {'Result'}")
        print("-" * 100)

        for item in data:
            q_id = item.get("id", 0)
            question = item["question"]
            expected_doc = item["ground_truth_doc_id"] # Document ID that should be in the correct answer

            start = time.time()
            # The system is running
            # Note: TraceBus will continue to log here; it can be disabled in eval mode if desired.
            result = self.orchestrator.run(question)
            end = time.time()

            latency_ms = (end - start) * 1000
            total_latency += latency_ms

            # Accuracy Check (Accuracy / Coverage)
            # We consider it successful if it contains at least one citation expected in the document.
            found_docs = [c.split(":")[1] for c in result.citations if ":" in c] # Format Doc:X Chunk:Y
            
            is_success = any(expected_doc in citations for citations in result.citations)
            
            # A simple check: Does the docID appear in the citation string?
            # Example citation: “Doc:cse3063_syllabus.txt Chunk:5”
            hit_status = "FAIL"
            if is_success: 
                correct_retrieval += 1
                hit_status = "PASS"

            print(f"{q_id:<5} {question[:38]:<40} {expected_doc[:13]:<15} {str(found_docs)[:13]:<15} {latency_ms:.0f}ms      {hit_status}")

        # Reporting
        avg_latency = total_latency / total_questions if total_questions > 0 else 0
        accuracy = (correct_retrieval / total_questions) * 100
        
        print("\n=== EVALUATION REPORT ===")
        print(f"Total Questions: {total_questions}")
        print(f"Accuracy (Recall@k): {accuracy:.2f}%")
        print(f"Avg Latency: {avg_latency:.2f} ms")
        print("=========================")
