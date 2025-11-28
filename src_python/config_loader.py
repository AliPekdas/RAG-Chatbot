import yaml
import sys
from pathlib import Path

CONFIG_PATH = Path("config.yaml")


def load_config():
    if not CONFIG_PATH.exists():
        print(f"CRITICAL ERROR: '{CONFIG_PATH}' not found!")
        sys.exit(1)

    try:
        with open(CONFIG_PATH, 'r', encoding='utf-8') as f:
            return yaml.safe_load(f)
    except Exception as e:
        print(f"CRITICAL ERROR: Could not parse config.yaml. {e}")
        sys.exit(1)