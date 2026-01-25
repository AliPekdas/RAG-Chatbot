import sys
import argparse
from pathlib import Path

# Importers
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
from answer_agent import TemplateAnswerAgent, LlmAnswerAgent
from eval_harness import EvalHarness

def ensure_data_exists(config):
    """Checks if data exists based on config paths."""
    index_file = Path(config['data']['indexFile'])

    if not index_file.exists():
        print("[System]: Data files missing. Generating automatically...")
        try:
            # We call the run functions directly
            chunker.run_chunker()
            index_builder.run_index_builder()
            print("[System]: Data prepared.\n")
        except Exception as e:
            print(f"CRITICAL ERROR: Could not generate data. {e}")
            sys.exit(1)


def run_scenario(orchestrator: RagOrchestrator, question: str):
    print("=" * 50)
    print(f"QUESTION: {question}")
    try:
        result = orchestrator.run(question)
        print("\n>>> RESULT OUTPUT <<<")
        print(f"📝 Answer: {result.text}")
        print(f"📚 Source: {result.citations}")
    except Exception as e:
        print(f"ERROR: {e}")
    print("\n")


def main():
    parser = argparse.ArgumentParser(description="MiniRAG System CLI")
    
    # Argument Group: The user must use either --q or --batch.
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--q", help="Sisteme tek bir soru sorar.")
    group.add_argument("--batch", help="JSON dosyasındaki sorularla toplu değerlendirme yapar.")
    
    # Optional configuration parameter
    parser.add_argument("--config", default="config.yaml", help="Konfigürasyon dosyası.")

    args = parser.parse_args()

    print("==================================================")
    print("                    RAG SYSTEM")
    print("==================================================")

    # 1. Load Configuration
    config = load_config()
    print(f"[System]: Loaded config for '{config['pipeline']['name']}'")

    # 2. Make Sure the Data Is Ready
    ensure_data_exists(config)
    print("[System]: Initializing components...")

    # 3. Start Components (Dependency Injection)
    if 'llm' in config['parameters']:
        print(f"[System]: using {config['parameters']['llm']['provider']} LLM Agent.")
        selected_agent = LlmAnswerAgent(config)
    else:
        print("[System]: Using TemplateAnswerAgent (Simple Mode).")
        selected_agent = TemplateAnswerAgent(config)

    orchestrator = RagOrchestrator(
        intent_detector=RuleBasedIntentDetector(),
        query_writer=HeuristicQueryWriter(),
        retriever=KeywordRetriever(config),
        reranker=BasicReranker(config),
        answer_agent=selected_agent,  # <--- Pass the selected agent variable here
        trace_bus=TraceBus(),
        keyword_index=KeywordIndex(config)
    )

    # 4. Run According to Mode Selection
    if args.q:
        # --- Single Question Mode ---
        run_scenario(orchestrator, args.q.strip())
    
    elif args.batch:
        # --- Batch Evaluation Mode (Your Task) ---
        print(f"[Mode]: Batch Evaluation running with {args.batch}...")
        
        # We start the EvalHarness class and run the test.
        harness = EvalHarness(orchestrator)
        harness.run_evaluation(args.batch)

if __name__ == "__main__":
    main()

