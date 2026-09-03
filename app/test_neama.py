# -*- coding: utf-8 -*-
import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app


@pytest.mark.asyncio
async def test_neama_status():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        res = await ac.get("/api/v1/neama/status")
        assert res.status_code == 200
        data = res.json()
        assert data["generation"] == "20.0 Sovereign"
        assert "1_deep_cognitive_reasoning" in data["pillars"]


@pytest.mark.asyncio
async def test_neama_causal_reasoning():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        payload = {
            "target_hypothesis": "تحسين كفاءة خوارزميات الاستدلال",
            "evidence": {"complexity": "O(N log N)", "samples": 1000},
            "interventions": {"apply_quantum_modeling": True},
            "mathematical_dimensions": [1.5, 2.3, 0.8]
        }
        res = await ac.post("/api/v1/neama/reason", json=payload)
        assert res.status_code == 200
        data = res.json()
        assert data["causal_impact_score"] > 0.5
        assert len(data["deductive_steps"]) > 0


@pytest.mark.asyncio
async def test_neama_memory_lifecycle():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Ingest
        ingest_payload = {
            "session_id": "session_architect_1",
            "content": "خطة العمل الخاصة بالجيل العشرين تركز على المنطق السببي والتشفير الكمي",
            "salience_score": 0.95
        }
        res_ingest = await ac.post("/api/v1/neama/memory/ingest", json=ingest_payload)
        assert res_ingest.status_code == 201

        # 2. Recall
        recall_payload = {
            "session_id": "session_architect_1",
            "query": "المنطق السببي",
            "top_k": 3
        }
        res_recall = await ac.post("/api/v1/neama/memory/recall", json=recall_payload)
        assert res_recall.status_code == 200
        memories = res_recall.json()
        assert len(memories) >= 1
        assert "المنطق السببي" in memories[0]["content"]


@pytest.mark.asyncio
async def test_neama_security_audit():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        res = await ac.post("/api/v1/neama/security/audit", json={
            "payload": "تقرير موضوعي دقيق حول النماذج العصبية المتقدمة"
        })
        assert res.status_code == 200
        data = res.json()
        assert data["is_valid"] is True
        assert data["quantum_resistant_hash"].startswith("qrh_")
