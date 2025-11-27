import json
import re
from pathlib import Path
from dataclasses import dataclass
from typing import List
from config_loader import load_config


@dataclass
class Chunk:
    chunk_id: int
    doc_id: str
    text: str

    def to_dict(self):
        return {"chunkId": self.chunk_id, "docId": self.doc_id, "text": self.text}


def create_chunks(content: str, doc_id: str, start_id: int, window_size: int, overlap: int) -> List[Chunk]:
    chunks = []
    words = content.split()
    i = 0
    current_id = start_id

    while i < len(words):
        end = min(i + window_size, len(words))
        chunk_words = words[i:end]
        text = " ".join(chunk_words).strip()

        if len(text) > 10:
            chunks.append(Chunk(current_id, doc_id, text))
            current_id += 1

        if end == len(words):
            break
        i += (window_size - overlap)

    return chunks


def run_chunker():
    print("[Chunker] Starting...")
    config = load_config()

    # Load Paths from Config
    input_dir = Path(config['data']['sourceDirectory'])
    output_file = Path(config['data']['corpusFile'])
    output_dir = output_file.parent

    # Load Parameters from Config
    window_size = config['parameters']['chunking']['windowSize']
    overlap = config['parameters']['chunking']['overlap']

    if not output_dir.exists():
        output_dir.mkdir(parents=True, exist_ok=True)

    if not input_dir.exists():
        input_dir.mkdir(parents=True, exist_ok=True)
        print(f"ERROR: '{input_dir}' is empty! Please add .txt files.")
        return

    txt_files = list(input_dir.glob("*.txt"))
    if not txt_files:
        print(f"ERROR: No .txt files found in '{input_dir}'.")
        return

    json_chunks = []
    global_chunk_id = 0

    for file_path in txt_files:
        print(f"   -> Processing: {file_path.name}")
        try:
            content = file_path.read_text(encoding='utf-8')
            content = content.lower().strip().replace("\n", " ").replace("\r", " ")
            content = re.sub(r'\s+', ' ', content)

            file_chunks = create_chunks(content, file_path.name, global_chunk_id, window_size, overlap)
            for chunk in file_chunks:
                json_chunks.append(chunk.to_dict())
                global_chunk_id += 1
        except Exception as e:
            print(f"Error reading {file_path.name}: {e}")

    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(json_chunks, f, ensure_ascii=False, indent=2)
        print(f"[Chunker] Completed. Total Chunks: {global_chunk_id}")
    except Exception as e:
        print(f"[Chunker] Error writing output: {e}")


if __name__ == "__main__":
    run_chunker()