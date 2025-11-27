import json
from pathlib import Path
from collections import defaultdict
from typing import List, Dict, NamedTuple


class IndexEntry(NamedTuple):
    doc_id: str
    chunk_id: int
    term_frequency: int


class KeywordIndex:
    def __init__(self, config):
        # Load Paths from Config
        self.corpus_path = Path(config['data']['corpusFile'])
        self.index_path = Path(config['data']['indexFile'])

        self.index_map: Dict[str, List[IndexEntry]] = defaultdict(list)
        self.chunk_store: Dict[str, str] = {}

        self.load_corpus()
        self.load_index()

    def get_entries(self, term: str) -> List[IndexEntry]:
        return self.index_map.get(term.lower(), [])

    def get_chunk_text(self, doc_id: str, chunk_id: int) -> str:
        key = f"{doc_id}_{chunk_id}"
        return self.chunk_store.get(key, f"ERROR: Content not found (Key: {key})")

    def load_corpus(self):
        if not self.corpus_path.exists():
            print(f"WARNING: {self.corpus_path} not found.")
            return
        try:
            with open(self.corpus_path, 'r', encoding='utf-8') as f:
                chunks = json.load(f)
            for chunk in chunks:
                key = f"{chunk['docId']}_{chunk['chunkId']}"
                self.chunk_store[key] = chunk['text']
            print(f"KeywordIndex: Corpus loaded ({len(self.chunk_store)} chunks).")
        except Exception as e:
            print(f"ERROR: IO Error in load_corpus: {e}")

    def load_index(self):
        if not self.index_path.exists():
            print(f"WARNING: {self.index_path} not found.")
            return
        try:
            with open(self.index_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            for term, entries in data.items():
                self.index_map[term] = [IndexEntry(e['docId'], e['chunkId'], e['tf']) for e in entries]
            print(f"KeywordIndex: Index loaded ({len(self.index_map)} terms).")
        except Exception as e:
            print(f"ERROR: IO Error in load_index: {e}")