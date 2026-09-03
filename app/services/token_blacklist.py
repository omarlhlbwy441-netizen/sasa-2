# -*- coding: utf-8 -*-
from typing import Set


class TokenBlacklistService:
    def __init__(self):
        self._memory_blacklist: Set[str] = set()

    async def revoke_token(self, token_jti: str, exp_timestamp: int = 0) -> None:
        self._memory_blacklist.add(token_jti)

    async def is_revoked(self, token_jti: str) -> bool:
        return token_jti in self._memory_blacklist


token_blacklist_service = TokenBlacklistService()
