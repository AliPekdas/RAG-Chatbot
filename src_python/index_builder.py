import json
import re
from pathlib import Path
from collections import OrderedDict
from config_loader import load_config

STOP_WORDS = {
    "ve", "ile", "bir", "bu", "şu", "o", "de", "da", "ki", "için",
    "gibi", "ne", "mi", "mı", "mu", "mü", "ama", "fakat", "lakin"
}


def run_index_builder():
    print("[IndexBuilder] Running...")
    config = load_config()

    # Load Paths from Config
    input_file = Path(config['data']['corpusFile'])
    output_file = Path(config['data']['indexFile'])

    if not input_file.exists():
        print(f"ERROR: {input_file} not found! Run Chunker first.")
        return

    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            chunks = json.load(f)

        index = {}
        for chunk in chunks:
            chunk_id = chunk['chunkId']
            doc_id = chunk['docId']
            text = chunk['text']
            terms = re.findall(r'\w+', text)

            for term in terms:
                if len(term) < 2 or term in STOP_WORDS: continue

                if term not in index: index[term] = []
                postings = index[term]

                existing = next((p for p in postings if p['chunkId'] == chunk_id), None)
                if existing:
                    existing['tf'] += 1
                else:
                    postings.append({"docId": doc_id, "chunkId": chunk_id, "tf": 1})

        sorted_index = OrderedDict(sorted(index.items()))

        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(sorted_index, f, ensure_ascii=False, indent=2)

        print(f"Indexing completed! Total terms: {len(index)}")

    except Exception as e:
        print(f"ERROR: {e}")


if __name__ == "__main__":
    run_index_builder()