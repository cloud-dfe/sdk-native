import urllib.request
from typing import Any, Dict, Optional

from .integra_services import IntegraServices
from .validation.key_validator import KeyValidator


class IntegraClass:
    """Fachada principal para comunicação com os serviços da API IntegraNotas em Python."""

    def __init__(
        self,
        is_production: bool = False,
        token: str = "",
        debug: bool = False,
        opener: Optional[urllib.request.OpenerDirector] = None,
    ) -> None:
        """Construtor da classe de serviço.

        :param is_production: Define se o ambiente é de produção (True) ou homologação (False).
        :param token: Token de acesso à API (Softhouse ou Emitente).
        :param debug: Ativa o modo de depuração para detalhar erros e requisições.
        :param opener: Instância customizada de OpenerDirector (opcional para testes ou mocks).
        :raises ValueError: Quando o token não for informado ou for vazio.
        """
        if not token or not str(token).strip():
            raise ValueError("Token é obrigatório.")

        self.token = str(token).strip()
        self.services = IntegraServices(
            is_production=is_production,
            version="1",
            debug=debug,
            opener=opener,
        )

    def _get_headers(self) -> Dict[str, str]:
        """Obtém os cabeçalhos padrão de autorização para a requisição."""
        return {
            "Authorization": self.token,
            "Accept": "application/json",
        }

    @staticmethod
    def check_key(chave: Optional[str]) -> str:
        """Valida a chave de acesso do documento fiscal eletrônico (44 dígitos para DFe ou 50 dígitos para NFSe).

        :param chave: Chave a ser validada.
        :return: Chave limpa e validada.
        :raises ValueError: Quando a chave for inválida.
        """
        return KeyValidator.check_key(chave)

    def cria(self, payload: Dict[str, Any]) -> Any:
        """Envia o payload de emissão/criação do documento fiscal para a API.

        :param payload: Dicionário contendo os dados do documento fiscal.
        :return: Resposta da API deserializada.
        """
        return self.services.request(
            method="POST",
            endpoint="/servico",
            body=payload,
            headers=self._get_headers(),
        )

    def consulta(
        self,
        chave: str = "",
        payload: Optional[Dict[str, Any]] = None,
    ) -> Any:
        """Consulta o status de um documento fiscal a partir da chave de acesso.

        :param chave: Chave de acesso de 44 ou 50 dígitos (opcional se informada no payload).
        :param payload: Dicionário opcional contendo filtros ou a própria chave.
        :return: Resposta da API com os dados do documento.
        """
        target_key = chave
        if not target_key and payload and isinstance(payload, dict) and "chave" in payload:
            target_key = str(payload.get("chave", ""))

        clean_key = KeyValidator.check_key(target_key)

        return self.services.request(
            method="GET",
            endpoint=f"/servico/{clean_key}",
            body=payload,
            headers=self._get_headers(),
        )

    def cancela(self, payload: Dict[str, Any]) -> Any:
        """Envia uma solicitação de cancelamento de documento fiscal.

        :param payload: Dicionário contendo a chave e justificativa do cancelamento.
        :return: Resposta da API sobre o cancelamento.
        """
        return self.services.request(
            method="POST",
            endpoint="/servico/cancela",
            body=payload,
            headers=self._get_headers(),
        )

    def preview(self, payload: Dict[str, Any]) -> Any:
        """Gera a pré-visualização do documento fiscal antes da emissão definitiva.

        :param payload: Dicionário contendo os dados do documento para pré-visualização.
        :return: Resposta da API com os dados da pré-visualização (PDF em Base64, etc.).
        """
        return self.services.request(
            method="POST",
            endpoint="/servico/preview",
            body=payload,
            headers=self._get_headers(),
        )
