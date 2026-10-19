from typing import Any, Dict, Optional


class IntegraException(Exception):
    """Exceção personalizada para erros originados na comunicação com a API da IntegraNotas."""

    def __init__(
        self,
        message: str,
        http_code: int = 0,
        response_body: Optional[Any] = None,
        debug_info: Optional[Dict[str, Any]] = None,
    ) -> None:
        super().__init__(message)
        self.message = message
        self.http_code = http_code
        self.response_body = response_body
        self.debug_info = debug_info

    def __str__(self) -> str:
        return self.message
