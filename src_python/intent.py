from enum import Enum, auto

class Intent(Enum):
    """Converted from Intent.java"""
    REGISTRATION = auto()
    STAFF_LOOKUP = auto()
    POLICY_FAQ = auto()
    COURSE = auto()
    UNKNOWN = auto()