import time
from models import Context
from jsonl_sink import JsonlTraceSink 

class TraceBus:
    def __init__(self):
        self.sink = JsonlTraceSink()
        self.start_time = time.time()

    def publish(self, event_name: str, context: Context):
        # Calculate duration
        current_time = time.time()
        elapsed_ms = (current_time - self.start_time) * 1000
        self.start_time = current_time # Reset timer for next stage

        # 1. Type in the console (Old function)
        print("-" * 50)
        print(f"[EVENT]: {event_name} ({elapsed_ms:.2f}ms)")
        if context.query_terms and event_name == "QueryGenerated":
            print(f"   -> Terms: {context.query_terms}")
        if context.hits and event_name == "Retrieved":
            print(f"   -> Found Hits: {len(context.hits)}")
        if context.final_answer and event_name == "Answered":
            print(f"   -> FINAL ANSWER: {context.final_answer.text}")

        # 2. Save to file (New feature - PDF requirement)
        self.sink.log_event(event_name, context, elapsed_ms)
