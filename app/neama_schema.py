# -*- coding: utf-8 -*-
from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field


class CausalNode(BaseModel):
    name: str
    parents: List[str] = []
    probability_table: Optional[Dict[str, float]] = None


class CausalInferenceRequest(BaseModel):
    target_hypothesis: str
    evidence: Dict[str, Any] = Field(default_factory=dict)
    interventions: Dict[str, Any] = Field(default_factory=dict)
    mathematical_dimensions: Optional[List[float]] = None


class CausalInferenceResponse(BaseModel):
    hypothesis: str
    causal_impact_score: float
    confidence_interval: List[float]
    counterfactual_outcome: str
    deductive_steps: List[str]


class MemoryIngestRequest(BaseModel):
    session_id: str
    content: str
    modality: str = "text"
    salience_score: float = Field(default=0.8, ge=0.0, le=1.0)
    metadata: Dict[str, Any] = Field(default_factory=dict)


class MemoryRecallRequest(BaseModel):
    session_id: str
    query: str
    top_k: int = Field(default=5, ge=1, le=50)


class MemoryItem(BaseModel):
    id: str
    session_id: str
    content: str
    modality: str
    salience: float
    decayed_weight: float
    timestamp: str


class MultimodalPayload(BaseModel):
    text_prompt: Optional[str] = None
    code_snippet: Optional[str] = None
    telemetry_data: Optional[Dict[str, Any]] = None
    numerical_matrix: Optional[List[List[float]]] = None


class MultimodalSynthesisResponse(BaseModel):
    fused_context: str
    harmonic_coherence: float
    analyzed_modalities: List[str]
    synthesized_insights: List[str]


class SecurityAuditRequest(BaseModel):
    payload: str
    signature: Optional[str] = None
    metadata: Dict[str, Any] = Field(default_factory=dict)


class SecurityAuditResponse(BaseModel):
    is_valid: bool
    quantum_resistant_hash: str
    bias_score: float
    is_biased: bool
    integrity_status: str
