class VectorRetriever:
    def __init__(self, vector_index):
        self.vector_index = vector_index

    def retrieve(self, query: str):
        results = self.vector_index.search(query)
        return [chunk for chunk, score in results]
