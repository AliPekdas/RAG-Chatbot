from vector_index import VectorIndex

class InMemoryVectorIndex(VectorIndex):
    def __init__(self, embedding_provider):
        self.embedding_provider = embedding_provider
        self.entries = []

    def add_chunk(self, chunk):
        vector = self.embedding_provider.embed(chunk.text)
        self.entries.append((chunk, vector))

    def search(self, query: str, k: int = 5):
        query_vector = self.embedding_provider.embed(query)
        scored_results = []

        for chunk, vector in self.entries:
            score = sum(a * b for a, b in zip(query_vector, vector))
            scored_results.append((chunk, score))

        scored_results.sort(
            key=lambda x: (-x[1], x[0].doc_id, x[0].chunk_id)
        )

        return scored_results[:k]
