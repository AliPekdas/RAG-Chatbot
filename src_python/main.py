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
from answer_agent import TemplateAnswerAgent


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
    parser = argparse.ArgumentParser()
    parser.add_argument("--q", required=True, help="Your question")
    args = parser.parse_args()
    cli_question = args.q.strip()

    print("==================================================")
    print("   RAG SYSTEM")
    print("==================================================")

    # 1. Load Config
    config = load_config()
    print(f"[System]: Loaded config for '{config['pipeline']['name']}'")

    # 2. Ensure Data
    ensure_data_exists(config)
    print("[System]: Initializing components...")

    # 3. Initialize Components with Config injection
    # IntentDetector and QueryWriter don't use config params in this version, so no arg passed
    orchestrator = RagOrchestrator(
        RuleBasedIntentDetector(),
        HeuristicQueryWriter(),
        KeywordRetriever(config),
        BasicReranker(config),
        TemplateAnswerAgent(config),
        TraceBus(),
        KeywordIndex(config)
    )

    run_scenario(orchestrator, cli_question)


if __name__ == "__main__":
    main()