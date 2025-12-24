from abc import ABC, abstractmethod

class VectorIndex(ABC):
    @abstractmethod
    def add_chunk(self, chunk):
        pass

    @abstractmethod
    def search(self, query: str, k: int = 5):
        pass
