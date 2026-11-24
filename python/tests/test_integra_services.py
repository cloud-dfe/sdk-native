import io
import json
import unittest
import urllib.error
from unittest.mock import MagicMock, patch

from integranotas.exceptions import IntegraException
from integranotas.integra_services import IntegraServices


class TestIntegraServices(unittest.TestCase):
    def test_base_uri_homologacao_por_padrao(self):
        services = IntegraServices(is_production=False)
        self.assertEqual(services.base_uri, "https://hom-api.integranotas.com.br/v")
        self.assertFalse(services.is_production)
        self.assertEqual(services.version, "1")

    def test_base_uri_producao_quando_is_production_for_true(self):
        services = IntegraServices(is_production=True)
        self.assertEqual(services.base_uri, "https://api.integranotas.com.br/v")
        self.assertTrue(services.is_production)

    def test_alterar_versao_da_api(self):
        services = IntegraServices()
        services.set_version("2")
        self.assertEqual(services.version, "2")

    def test_lancar_erro_ao_definir_versao_vazia(self):
        services = IntegraServices()
        with self.assertRaises(ValueError) as ctx:
            services.set_version("")
        self.assertIn("A versão da API não pode ser vazia.", str(ctx.exception))

    @patch("urllib.request.urlopen")
    def test_requisicao_com_sucesso(self, mock_urlopen):
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = json.dumps({"status": "ok", "id": 123}).encode("utf-8")
        mock_urlopen.return_value = mock_resp

        services = IntegraServices(is_production=False)
        resultado = services.request(
            method="POST",
            endpoint="/servico",
            body={"teste": True},
            headers={"Authorization": "token123"},
        )

        self.assertEqual(resultado, {"status": "ok", "id": 123})
        req = mock_urlopen.call_args[0][0]
        self.assertEqual(req.full_url, "https://hom-api.integranotas.com.br/v1/servico")
        self.assertEqual(req.get_method(), "POST")
        self.assertEqual(req.headers.get("Authorization"), "token123")
        self.assertEqual(req.headers.get("Content-type"), "application/json")
        self.assertEqual(req.data, json.dumps({"teste": True}).encode("utf-8"))

    @patch("urllib.request.urlopen")
    def test_wrapper_debug_quando_ativo(self, mock_urlopen):
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = json.dumps({"status": "sucesso"}).encode("utf-8")
        mock_urlopen.return_value = mock_resp

        services = IntegraServices(is_production=False, debug=True)
        resultado = services.request("POST", "/servico", body={"nome": "nota"})

        self.assertIn("debug", resultado)
        self.assertEqual(resultado["status"], "sucesso")
        self.assertEqual(resultado["debug"]["method"], "POST")
        self.assertEqual(resultado["debug"]["url"], "https://hom-api.integranotas.com.br/v1/servico")

    @patch("urllib.request.urlopen")
    def test_resposta_vazia_lanca_integra_exception(self, mock_urlopen):
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = b""
        mock_urlopen.return_value = mock_resp

        services = IntegraServices(is_production=False)
        with self.assertRaises(IntegraException) as ctx:
            services.request("GET", "/servico/123")

        self.assertIn("Resposta Vazia", str(ctx.exception))

    @patch("urllib.request.urlopen")
    def test_resposta_json_invalida_lanca_integra_exception(self, mock_urlopen):
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = b"resposta nao json"
        mock_urlopen.return_value = mock_resp

        services = IntegraServices(is_production=False)
        with self.assertRaises(IntegraException) as ctx:
            services.request("GET", "/servico/123")

        self.assertIn("Resposta JSON Inválida", str(ctx.exception))

    @patch("urllib.request.urlopen")
    def test_erro_http_lanca_integra_exception(self, mock_urlopen):
        error_body = json.dumps({"error": "Não autorizado"}).encode("utf-8")
        http_error = urllib.error.HTTPError(
            url="https://hom-api.integranotas.com.br/v1/servico",
            code=401,
            msg="Unauthorized",
            hdrs={},
            fp=io.BytesIO(error_body),
        )
        mock_urlopen.side_effect = http_error

        services = IntegraServices(is_production=False)
        with self.assertRaises(IntegraException) as ctx:
            services.request("GET", "/servico/123")

        self.assertEqual(ctx.exception.http_code, 401)
        self.assertIn("Erro na API (HTTP 401)", str(ctx.exception))

    @patch("urllib.request.urlopen")
    def test_falha_de_rede_lanca_integra_exception(self, mock_urlopen):
        mock_urlopen.side_effect = urllib.error.URLError(reason="Falha de conexão DNS")

        services = IntegraServices(is_production=False)
        with self.assertRaises(IntegraException) as ctx:
            services.request("GET", "/servico/123")

        self.assertIn("Erro na Requisição HTTP", str(ctx.exception))
        self.assertIn("Falha de conexão DNS", str(ctx.exception))


if __name__ == "__main__":
    unittest.main()
