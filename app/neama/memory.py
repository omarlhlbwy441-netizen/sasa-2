# -*- coding: utf-8 -*-
"""
4. Contextual Awareness & Long-term Memory
Adaptive episodic and semantic memory matrix with decay, salience weighting, and consolidation.
"""
import uuid
import math
import time
from typing import List, Dict, Any, Optional
from datetime import datetime, timezone
from app.schemas.neama_schema import MemoryIngestRequest, MemoryRecallRequest, MemoryItem


class AdaptiveMemoryEntry:
    def __init__(self, session_id: str, content: str, modality: str, salience: float, metadata: Dict[str, Any]):
        self.id = str(uuid.uuid4())
        self.session_id = session_id
        self.content = content
        self.modality = modality
        self.salience = salience
        self.metadata = metadata
        self.created_at = time.time()
        self.access_count = 0
        self.last_accessed = self.created_at

    def get_decayed_weight(self, half_life_seconds: float = 3600.0 * 24) -> float:
        # Exponential memory retention curve (Ebbinghaus-inspired adaptive decay)
        delta_t = time.time() - self.last_accessed
        retention = math.exp(-delta_t / half_life_seconds)
        # Salience reinforces memory against decay
        effective_weight = (self.salience * 0.7) + (retention * 0.2) + (min(self.access_count, 10) * 0.01)
        return round(min(effective_weight, 1.0), 4)

    def touch(self):
        self.access_count += 1
        self.last_accessed = time.time()


class ContextualAdaptiveMemoryMatrix:
    """
    Sovereign Long-Term Adaptive Memory Matrix for Neama Gen 20
    """

    def __init__(self):
        self._memory_store: List[AdaptiveMemoryEntry] = []

    def ingest(self, request: MemoryIngestRequest) -> MemoryItem:
        entry = AdaptiveMemoryEntry(
            session_id=request.session_id,
            content=request.content,
            modality=request.modality,
            salience=request.salience_score,
            metadata=request.metadata
        )
        self._memory_store.append(entry)

        return MemoryItem(
            id=entry.id,
            session_id=entry.session_id,
            content=entry.content,
            modality=entry.modality,
            salience=entry.salience,
            decayed_weight=entry.get_decayed_weight(),
            timestamp=datetime.fromtimestamp(entry.created_at, timezone.utc).isoformat()
        )

    def recall(self, request: MemoryRecallRequest) -> List[MemoryItem]:
        query_words = set(request.query.lower().split())
        candidates = []

        for entry in self._memory_store:
            if entry.session_id == request.session_id or request.session_id == "*":
                # Compute lexical overlap + semantic salience + decayed retention
                content_words = set(entry.content.lower().split())
                overlap = len(query_words.intersection(content_words)) / max(len(query_words), 1)
                decayed_weight = entry.get_decayed_weight()
                relevance = (overlap * 0.6) + (decayed_weight * 0.4)

                candidates.append((relevance, entry))

        # Sort descending by relevance
        candidates.sort(key=lambda x: x[0], reverse=True)
        top_entries = candidates[:request.top_k]

        results = []
        for _, entry in top_entries:
            entry.touch()
            results.append(
                MemoryItem(
                    id=entry.id,
                    session_id=entry.session_id,
                    content=entry.content,
                    modality=entry.modality,
                    salience=entry.salience,
                    decayed_weight=entry.get_decayed_weight(),
                    timestamp=datetime.fromtimestamp(entry.created_at, timezone.utc).isoformat()
                )
            )

        return results

    def consolidate_and_prune(self, threshold: float = 0.05) -> int:
        """
        Consolidates long-term memory and prunes zero-salience noise
        """
        initial_len = len(self._memory_store)
        self._memory_store = [e for e in self._memory_store if e.get_decayed_weight() >= threshold]
        return initial_len - len(self._memory_store)


memory_matrix = ContextualAdaptiveMemoryMatrix()
