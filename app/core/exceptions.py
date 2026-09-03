# -*- coding: utf-8 -*-
import logging
from fastapi import Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from sqlalchemy.exc import SQLAlchemyError
import jwt

logger = logging.getLogger("enterprise.exceptions")


async def global_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    if isinstance(exc, RequestValidationError):
        errors = [
            {"field": ".".join(map(str, err["loc"])), "message": err["msg"]}
            for err in exc.errors()
        ]
        return JSONResponse(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            content={"success": False, "error_code": "VALIDATION_ERROR", "details": errors},
        )

    if isinstance(exc, jwt.PyJWTError):
        return JSONResponse(
            status_code=status.HTTP_401_UNAUTHORIZED,
            content={"success": False, "error_code": "AUTH_TOKEN_INVALID", "message": "رمز التوثيق غير صالح أو منتهي الصلاحية."},
        )

    if isinstance(exc, SQLAlchemyError):
        logger.critical(f"Database error on {request.url.path}: {str(exc)}", exc_info=True)
        return JSONResponse(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            content={"success": False, "error_code": "DATABASE_ERROR", "message": "حدث خطأ غير متوقع في معالجة البيانات."},
        )

    logger.error(f"Unhandled system error: {str(exc)}", exc_info=True)
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={"success": False, "error_code": "INTERNAL_SERVER_ERROR", "message": "حدث خطأ داخلي في النظام."},
    )
