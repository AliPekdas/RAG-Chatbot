from models import Context


class TraceBus:
    """Converted from TraceBus.java"""

    def publish(self, event_name: str, context: Context):
        print("-" * 50)
        print(f"[EVENT]: {event_name}")

        if context.query_terms and event_name == "QueryGenerated":
            print(f"   -> Terms: {context.query_terms}")

        if context.hits and event_name == "Retrieved":
            print(f"   -> Found Hits: {len(context.hits)}")

        if context.final_answer and event_name == "Answered":
            print(f"   -> FINAL ANSWER: {context.final_answer.text}")
            print(f"   -> CITATIONS: {context.final_answer.citations}")