# -*- coding: utf-8 -*-
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.repositories.user_repository import UserRepository
from app.schemas.auth_schema import UserRegisterRequest, UserLoginRequest
from app.core.security import get_password_hash, verify_password, create_access_token, create_refresh_token
from app.models.user import UserModel


class AuthService:
    def __init__(self, db: AsyncSession):
        self.user_repo = UserRepository(db)

    async def register_user(self, payload: UserRegisterRequest) -> UserModel:
        if await self.user_repo.get_by_email(payload.email):
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="البريد الإلكتروني مستخدم بالفعل.")
        if await self.user_repo.get_by_username(payload.username):
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="اسم المستخدم مستخدم بالفعل.")

        hashed_pwd = get_password_hash(payload.password)
        user_data = {
            "email": payload.email,
            "username": payload.username,
            "hashed_password": hashed_pwd,
            "full_name": payload.full_name,
            "role": payload.role or "user"
        }
        return await self.user_repo.create(user_data)

    async def authenticate_user(self, payload: UserLoginRequest) -> dict:
        user = await self.user_repo.get_by_username(payload.username_or_email)
        if not user:
            user = await self.user_repo.get_by_email(payload.username_or_email)
        
        if not user or not verify_password(payload.password, user.hashed_password):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="بيانات الاعتماد غير صحيحة.")
        
        if not user.is_active:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="الحساب معطل.")

        token_data = {"sub": str(user.id), "role": user.role}
        access_token = create_access_token(token_data)
        refresh_token = create_refresh_token(token_data)

        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "bearer",
            "expires_in": 900
        }
