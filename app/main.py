# -*- coding: utf-8 -*-
from fastapi import FastAPI
from fastapi.exceptions import RequestValidationError
from app.core.exceptions import global_exception_handler
from app.controllers.auth_controller import router as auth_router
from app.core.config import settings

app = FastAPI(title=settings.PROJECT_NAME, version="1.0.0")

app.add_exception_handler(RequestValidationError, global_exception_handler)
app.add_exception_handler(Exception, global_exception_handler)

app.include_router(auth_router)


@app.get("/health", tags=["Health Check"])
async def health_check():
    return {"status": "healthy", "service": settings.PROJECT_NAME}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
