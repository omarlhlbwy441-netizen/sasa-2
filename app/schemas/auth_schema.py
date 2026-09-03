# -*- coding: utf-8 -*-
import re
from typing import Optional, List
from pydantic import BaseModel, EmailStr, field_validator, ConfigDict


class UserRegisterRequest(BaseModel):
    email: EmailStr
    username: str
    password: str
    full_name: Optional[str] = None
    role: Optional[str] = "user"

    @field_validator("password")
    @classmethod
    def validate_strong_password(cls, v: str) -> str:
        if len(v) < 8:
            raise ValueError("يجب ألا تقل كلمة المرور عن 8 خانات.")
        if not re.search(r"[A-Z]", v):
            raise ValueError("يجب أن تحتوي كلمة المرور على حرف كبير واحد على الأقل (A-Z).")
        if not re.search(r"[a-z]", v):
            raise ValueError("يجب أن تحتوي كلمة المرور على حرف صغير واحد على الأقل (a-z).")
        if not re.search(r"\d", v):
            raise ValueError("يجب أن تحتوي كلمة المرور على رقم واحد على الأقل (0-9).")
        if not re.search(r"[!@#$%^&*(),.?\":{}|<>]", v):
            raise ValueError("يجب أن تحتوي كلمة المرور على رمز خاص واحد على الأقل.")
        return v

    @field_validator("username")
    @classmethod
    def validate_username(cls, v: str) -> str:
        cleaned = v.strip()
        if len(cleaned) < 3 or len(cleaned) > 50:
            raise ValueError("اسم المستخدم يجب أن يكون بين 3 و 50 حرفاً.")
        if not re.match(r"^[a-zA-Z0-9_-]+$", cleaned):
            raise ValueError("اسم المستخدم يمكن أن يحتوي فقط على أحرف وأرقام وشرطة (- أو _).")
        return cleaned


class UserLoginRequest(BaseModel):
    username_or_email: str
    password: str


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    expires_in: int


class UserResponse(BaseModel):
    id: str
    email: EmailStr
    username: str
    full_name: Optional[str] = None
    role: str
    roles: List[str] = []
    is_active: bool

    model_config = ConfigDict(from_attributes=True)
