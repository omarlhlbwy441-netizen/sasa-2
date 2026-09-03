# -*- coding: utf-8 -*-
from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from app.core.exceptions import global_exception_handler
from app.controllers.auth_controller import router as auth_router
from app.controllers.neama_controller import router as neama_router
from app.core.config import settings

app = FastAPI(
    title=f"{settings.PROJECT_NAME} & Neama AI (Gen 20)",
    version="20.0.0",
    description="Sovereign Cognitive Architecture with Enterprise Auth & Multimodal Reasoning"
)

app.add_exception_handler(RequestValidationError, global_exception_handler)
app.add_exception_handler(Exception, global_exception_handler)

app.include_router(auth_router)
app.include_router(neama_router)


@app.get("/health", tags=["Health Check"])
async def health_check():
    return {
        "status": "healthy",
        "service": settings.PROJECT_NAME,
        "neama_cognitive_core": "Generation 20 Active"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
