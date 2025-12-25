import unittest
from unittest.mock import MagicMock
from retriever import KeywordRetriever
from keyword_index import IndexEntry


class TestKeywordRetriever(unittest.TestCase):
    def setUp(self):
        # Mock configuration
        self.config = {'parameters': {'retriever': {'maxResults': 2}}}
        self.retriever = KeywordRetriever(self.config)

        # Mock KeywordIndex
        self.mock_index = MagicMock()

    def test_score_aggregation(self):
        """Should sum term frequencies for multiple query terms."""
        # Setup: term 'python' appears in doc1 (TF: 2), 'code' appears in doc1 (TF: 3)
        self.mock_index.get_entries.side_effect = lambda term: {
            "python": [IndexEntry("doc1.txt", 1, 2)],
            "code": [IndexEntry("doc1.txt", 1, 3)]
        }.get(term, [])

        self.mock_index.get_chunk_text.return_value = "python code sample"

        hits = self.retriever.retrieve(["python", "code"], self.mock_index)

        self.assertEqual(len(hits), 1)
        # Score should be 2 + 3 = 5.0
        self.assertEqual(hits[0].score, 5.0)

    def test_max_results_limit(self):
        """Should return no more than maxResults defined in config."""
        # Setup 3 different hits
        self.mock_index.get_entries.return_value = [
            IndexEntry("d1", 1, 1), IndexEntry("d2", 1, 1), IndexEntry("d3", 1, 1)
        ]

        hits = self.retriever.retrieve(["test"], self.mock_index)
        self.assertEqual(len(hits), 2)  # Limited by config maxResults


if __name__ == '__main__':
    unittest.main()