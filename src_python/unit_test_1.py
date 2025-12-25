import unittest
from intent_detector import RuleBasedIntentDetector
from intent import Intent


class TestRuleBasedIntentDetector(unittest.TestCase):
    def setUp(self):
        self.detector = RuleBasedIntentDetector()

    def test_intent_detection_logic(self):
        """Should detect the correct intent based on predefined keyword rules."""
        # Test STAFF_LOOKUP intent
        self.assertEqual(self.detector.detect("Hocanın ofis nerede?"), Intent.STAFF_LOOKUP)

        # Test POLICY_FAQ intent
        self.assertEqual(self.detector.detect("Staj yönetmeliği nedir?"), Intent.POLICY_FAQ)

        # Test REGISTRATION intent
        self.assertEqual(self.detector.detect("Ders seçimi ne zaman?"), Intent.REGISTRATION)

    def test_case_insensitivity_and_punctuation(self):
        """Should ignore case and special characters during detection."""
        self.assertEqual(self.detector.detect("AKTS!!!"), Intent.COURSE)
        self.assertEqual(self.detector.detect("müfredat"), Intent.COURSE)

    def test_unknown_intent(self):
        """Should return Intent.UNKNOWN if no keywords match."""
        self.assertEqual(self.detector.detect("Bugün hava nasıl?"), Intent.UNKNOWN)


if __name__ == '__main__':
    unittest.main()