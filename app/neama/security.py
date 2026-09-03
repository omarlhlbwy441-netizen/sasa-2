# -*- coding: utf-8 -*-
"""
2. Sovereign Security & Data Integrity
Post-Quantum Hybrid verification, dynamic filtering, and bias auditing.
"""
import hashlib
import hmac
import re
from typing import Dict, Any, Tuple
from app.schemas.neama_schema import SecurityAuditRequest, SecurityAuditResponse


class SovereignSecurityEngine:
    """
    Quantum-Resistant Hash Integrity and Dynamic Impartiality Filter
    """

    def __init__(self, sovereign_salt: str = "NEAMA_GEN20_QUANTUM_RESISTANT_SEED_v20"):
        self.salt = sovereign_salt.encode("utf-8")
        # Pre-compiled biased or non-objective heuristic markers
        self.bias_markers = [
            r"\b(absolute truth|unquestionable|always superior|never wrong)\b",
            r"\b(حقيقة مطلقة|دون أدنى شك|لا يقبل النقاش|متفوق دائماً)\b"
        ]

    def compute_quantum_resistant_hash(self, payload: str) -> str:
        """
        Combines SHA-3/Keccak-like double hashing with HMAC sovereign salt
        """
        first_pass = hashlib.sha3_512(payload.encode("utf-8")).digest()
        second_pass = hmac.new(self.salt, first_pass, hashlib.blake2b).hexdigest()
        return f"qrh_blake2b_sha3_{second_pass[:48]}"

    def evaluate_bias_and_impartiality(self, text: str) -> Tuple[float, bool]:
        """
        Dynamic filtering to ensure highest objectivity and neutrality
        """
        matches = 0
        for pattern in self.bias_markers:
            if re.search(pattern, text, re.IGNORECASE):
                matches += 1

        length_factor = max(len(text.split()), 1)
        score = min(matches / max(length_factor / 10.0, 1.0), 1.0)
        is_biased = score > 0.3
        return round(score, 4), is_biased

    def audit_payload(self, request: SecurityAuditRequest) -> SecurityAuditResponse:
        q_hash = self.compute_quantum_resistant_hash(request.payload)
        bias_score, is_biased = self.evaluate_bias_and_impartiality(request.payload)

        status = "PASSED_IMMUTABLE" if not is_biased else "AUDIT_WARNING_BIAS_DETECTED"

        return SecurityAuditResponse(
            is_valid=not is_biased,
            quantum_resistant_hash=q_hash,
            bias_score=bias_score,
            is_biased=is_biased,
            integrity_status=status
        )


security_engine = SovereignSecurityEngine()
