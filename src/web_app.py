import streamlit as st
import time
from config_loader import load_config
from rag_orchestrator import RagOrchestrator
from intent_detector import RuleBasedIntentDetector
from query_writer import HeuristicQueryWriter
from retriever import KeywordRetriever
from reranker import BasicReranker
from answer_agent import TemplateAnswerAgent, LlmAnswerAgent
from trace_bus import TraceBus
from keyword_index import KeywordIndex
import chunker
import index_builder
from pathlib import Path

# --- Page Config ---
st.set_page_config(page_title="RAG Chatbot", page_icon="🤖")
st.title("🤖 MiniRAG University Helpdesk ")


# --- Initialization (Cached to run only once) ---
@st.cache_resource
def init_system():
    # 1. Load Config
    config = load_config()

    # 2. Ensure Data Exists
    index_file = Path(config['data']['indexFile'])
    if not index_file.exists():
        with st.spinner("Veri tabanı oluşturuluyor..."):
            chunker.run_chunker()
            index_builder.run_index_builder()

    # 3. Select Agent
    if 'llm' in config['parameters']:
        agent = LlmAnswerAgent(config)
        agent_type = f"GenAI ({config['parameters']['llm']['provider']})"
    else:
        agent = TemplateAnswerAgent(config)
        agent_type = "Template (Simple)"

    # 4. Build Orchestrator
    orchestrator = RagOrchestrator(
        intent_detector=RuleBasedIntentDetector(),
        query_writer=HeuristicQueryWriter(),
        retriever=KeywordRetriever(config),
        reranker=BasicReranker(config),
        answer_agent=agent,
        trace_bus=TraceBus(),
        keyword_index=KeywordIndex(config)
    )

    return orchestrator, agent_type


# Load the system
try:
    orchestrator, agent_type = init_system()
    st.success(f"Sistem Hazır! Mod: {agent_type}")
except Exception as e:
    st.error(f"Sistem başlatılamadı: {e}")
    st.stop()

# --- Chat Interface ---
if "messages" not in st.session_state:
    st.session_state.messages = []

# Display chat history
for message in st.session_state.messages:
    with st.chat_message(message["role"]):
        st.markdown(message["content"])

# User Input
if prompt := st.chat_input("Sorunuzu buraya yazın..."):
    # 1. Show user message
    st.session_state.messages.append({"role": "user", "content": prompt})
    with st.chat_message("user"):
        st.markdown(prompt)

    # 2. Generate Answer
    with st.chat_message("assistant"):
        message_placeholder = st.empty()
        with st.spinner("Düşünüyor..."):
            try:
                # Run the RAG pipeline
                result = orchestrator.run(prompt)

                # Format output with sources
                full_response = result.text
                if result.citations:
                    full_response += "\n\n**Kaynaklar:**\n" + "\n".join([f"- {c}" for c in result.citations])

                message_placeholder.markdown(full_response)

                # Add to history
                st.session_state.messages.append({"role": "assistant", "content": full_response})

            except Exception as e:
                st.error(f"Hata oluştu: {e}")