# -*- coding: utf-8 -*-
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from app.models.user import UserModel


class UserRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_by_email(self, email: str) -> UserModel | None:
        result = await self.db.execute(select(UserModel).where(UserModel.email == email))
        return result.scalars().first()

    async def get_by_username(self, username: str) -> UserModel | None:
        result = await self.db.execute(select(UserModel).where(UserModel.username == username))
        return result.scalars().first()

    async def get_by_id(self, user_id: str) -> UserModel | None:
        result = await self.db.execute(select(UserModel).where(UserModel.id == str(user_id)))
        return result.scalars().first()

    async def create(self, user_data: dict) -> UserModel:
        user = UserModel(**user_data)
        self.db.add(user)
        await self.db.flush()
        await self.db.refresh(user)
        return user
