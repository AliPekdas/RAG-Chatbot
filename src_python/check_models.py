# check_models.py
import google.generativeai as genai
import os

# 1. Setup API Key (or paste it directly here for testing)
api_key = os.environ.get("GEMINI_API_KEY")
if not api_key:
    api_key = input("Enter your Google API Key: ").strip()

genai.configure(api_key=api_key)

print("Checking available models...")
try:
    for m in genai.list_models():
        if 'generateContent' in m.supported_generation_methods:
            print(f"- {m.name}")
except Exception as e:
    print(f"Error: {e}")