from embedding_provider import EmbeddingProvider

class StubEmbeddingProvider(EmbeddingProvider):
    def embed(self, text: str) -> list[float]:
        words = text.lower().split()

        dim1 = sum(ord(c) for word in words for c in word) % 500
        dim2 = len(words) * 10.0
        dim3 = sum(1 for c in text if c.isupper()) * 5.0

        raw_vector = [float(dim1), float(dim2), float(dim3)]

        norm = (sum(x ** 2 for x in raw_vector)) ** 0.5
        return [x / norm for x in raw_vector] if norm > 0 else [0.0, 0.0, 0.0]
