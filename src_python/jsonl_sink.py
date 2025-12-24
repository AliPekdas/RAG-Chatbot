import json
import time
from pathlib import Path
from datetime import datetime
from models import Context

class JsonlTraceSink:
    def __init__(self):
        # Create the log folder
        log_dir = Path("logs")
        log_dir.mkdir(parents=True, exist_ok=True)
        
        # File name: run-YYYYMMDD-HHMMSS.jsonl
        timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
        self.log_file = log_dir / f"run-{timestamp}.jsonl"
        print(f"[System]: Logging to {self.log_file}")

    def log_event(self, stage: str, context: Context, execution_time_ms: float = 0.0):
        # Data structure to be recorded
        log_entry = {
            "timestamp": datetime.now().isoformat(),
            "stage": stage,
            "executionTimeMs": execution_time_ms,
            "question": context.original_question,
            "intent": str(context.intent) if context.intent else "N/A"
        }

        # Additional details depending on the stage
        if stage == "QueryGenerated":
            log_entry["terms"] = context.query_terms
        elif stage == "Retrieved":
            log_entry["hitCount"] = len(context.hits)
            log_entry["topHit"] = str(context.hits[0]) if context.hits else "None"
        elif stage == "Answered" and context.final_answer:
            log_entry["answer"] = context.final_answer.text
            log_entry["citations"] = context.final_answer.citations

        # Add to file (Append mode)
        with open(self.log_file, "a", encoding="utf-8") as f:
            f.write(json.dumps(log_entry, ensure_ascii=False) + "\n")
