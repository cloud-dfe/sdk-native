import json
import socket
import ssl
import urllib.error
import urllib.request
from typing import Any, Dict, Optional

from .exceptions import IntegraException


class IntegraServices:
    """Responsável pela execução de requisições HTTP para a API IntegraNotas usando a biblioteca padrão do Python."""

    URI_PRODUCTION = "https://api.integranotas.com.br/v"
    URI_TESTS = "https://hom-api.integranotas.com.br/v"

    def __init__(
        self,
        is_production: bool = False,
        version: str = "1",
        debug: bool = False,
        opener: Optional[urllib.request.OpenerDirector] = None,
    ) -> None:
        self.is_production = bool(is_production)
        self.version = str(version)
        self.debug = bool(debug)
        self.base_uri = self.URI_PRODUCTION if self.is_production else self.URI_TESTS
        self.opener = opener

    def set_version(self, version: str) -> None:
        """Define a versão da API utilizada nas requisições.

        :param version: Versão da API (ex: "1").
        :raises ValueError: Quando a versão for vazia.
        """
        if not version or not str(version).strip():
            raise ValueError("A versão da API não pode ser vazia.")
        self.version = str(version)

    def request(
        self,
        method: str,
        endpoint: str,
        body: Optional[Any] = None,
        headers: Optional[Dict[str, str]] = None,
        version: Optional[str] = None,
    ) -> Any:
        """Executa uma requisição HTTP para a API da IntegraNotas.

        :param method: Método HTTP (GET, POST, etc.)
        :param endpoint: Caminho relativo do endpoint (ex: "/servico")
        :param body: Dados a serem enviados no corpo da requisição
        :param headers: Cabeçalhos adicionais da requisição
        :param version: Versão alternativa da API (opcional)
        :return: Resposta deserializada como dicionário/lista (ou encapsulada com debug se ativo)
        :raises IntegraException: Quando ocorrer erro de rede, timeout, resposta vazia ou erro HTTP
        """
        if version:
            self.set_version(version)

        url = f"{self.base_uri}{self.version}{endpoint}"

        request_headers: Dict[str, str] = {
            "Accept": "application/json",
        }
        if headers:
            request_headers.update(headers)

        data_bytes: Optional[bytes] = None
        if body is not None:
            if isinstance(body, (dict, list)):
                data_bytes = json.dumps(body).encode("utf-8")
            elif isinstance(body, str):
                data_bytes = body.encode("utf-8")
            elif isinstance(body, bytes):
                data_bytes = body
            request_headers["Content-Type"] = "application/json"

        debug_details: Optional[Dict[str, Any]] = None
        if self.debug:
            debug_details = {
                "method": method.upper(),
                "url": url,
                "body": body,
                "headers": request_headers,
            }

        req = urllib.request.Request(
            url=url,
            data=data_bytes,
            headers=request_headers,
            method=method.upper(),
        )

        ssl_context = ssl.create_default_context()
        if not self.is_production:
            ssl_context.check_hostname = False
            ssl_context.verify_mode = ssl.CERT_NONE

        http_code = 0
        response_text = ""

        try:
            if self.opener is not None:
                resp = self.opener.open(req, timeout=60)
            else:
                resp = urllib.request.urlopen(req, timeout=60, context=ssl_context)

            http_code = resp.getcode()
            response_text = resp.read().decode("utf-8")

        except urllib.error.HTTPError as err:
            http_code = err.code
            try:
                raw_body = err.read().decode("utf-8")
                parsed_error = json.loads(raw_body)
            except Exception:
                parsed_error = raw_body if "raw_body" in locals() else str(err)

            error_payload: Dict[str, Any] = {
                "message": f"Erro na API (HTTP {http_code})",
                "response": parsed_error,
                "http_code": http_code,
            }
            if debug_details:
                error_payload["debug"] = debug_details

            raise IntegraException(
                message=json.dumps(error_payload, ensure_ascii=False),
                http_code=http_code,
                response_body=parsed_error,
                debug_info=debug_details,
            ) from err

        except (urllib.error.URLError, socket.timeout) as err:
            is_timeout = isinstance(err, socket.timeout) or (
                isinstance(err, urllib.error.URLError)
                and isinstance(err.reason, socket.timeout)
            )

            if is_timeout:
                error_payload = {
                    "message": "Timeout na Requisição",
                    "error": "Tempo limite de conexão excedido (60s)",
                    "http_code": 408,
                }
                if debug_details:
                    error_payload["debug"] = debug_details
                raise IntegraException(
                    message=json.dumps(error_payload, ensure_ascii=False),
                    http_code=408,
                    response_body=None,
                    debug_info=debug_details,
                ) from err

            error_msg = str(getattr(err, "reason", err))
            error_payload = {
                "message": "Erro na Requisição HTTP",
                "error": error_msg,
                "http_code": http_code,
            }
            if debug_details:
                error_payload["debug"] = debug_details

            raise IntegraException(
                message=json.dumps(error_payload, ensure_ascii=False),
                http_code=http_code,
                response_body=None,
                debug_info=debug_details,
            ) from err

        except Exception as err:
            error_payload = {
                "message": "Erro na Requisição HTTP",
                "error": str(err),
                "http_code": http_code,
            }
            if debug_details:
                error_payload["debug"] = debug_details
            raise IntegraException(
                message=json.dumps(error_payload, ensure_ascii=False),
                http_code=http_code,
                response_body=None,
                debug_info=debug_details,
            ) from err

        if not response_text or not response_text.strip():
            error_payload = {
                "message": "Resposta Vazia",
                "http_code": http_code,
            }
            if debug_details:
                error_payload["debug"] = debug_details
            raise IntegraException(
                message=json.dumps(error_payload, ensure_ascii=False),
                http_code=http_code,
                response_body=response_text,
                debug_info=debug_details,
            )

        try:
            parsed_data = json.loads(response_text)
        except Exception as err:
            error_payload = {
                "message": "Resposta JSON Inválida",
                "response": response_text,
                "http_code": http_code,
                "json_error": str(err),
            }
            if debug_details:
                error_payload["debug"] = debug_details
            raise IntegraException(
                message=json.dumps(error_payload, ensure_ascii=False),
                http_code=http_code,
                response_body=response_text,
                debug_info=debug_details,
            ) from err

        if self.debug:
            if isinstance(parsed_data, dict):
                parsed_data["debug"] = debug_details
                return parsed_data
            return {
                "data": parsed_data,
                "debug": debug_details,
            }

        return parsed_data
