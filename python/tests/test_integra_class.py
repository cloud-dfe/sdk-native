import json
import unittest
from unittest.mock import MagicMock, patch

from integranotas.integra_class import IntegraClass


class TestIntegraClass(unittest.TestCase):
    def test_construtor_sem_token_deve_lancar_value_error(self):
        tokens_invalidos = ["", "   ", None]
        for token in tokens_invalidos:
            with self.subTest(token=token):
                with self.assertRaises(ValueError) as ctx:
                    IntegraClass(token=token)  # type: ignore
                self.assertIn("Token é obrigatório.", str(ctx.exception))

    @patch("urllib.request.urlopen")
    def test_cria_deve_enviar_post_para_servico_com_autorizacao(self, mock_urlopen):
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = json.dumps({"id": 123}).encode("utf-8")
        mock_urlopen.return_value = mock_resp

        client = IntegraClass(is_production=False, token="MEU_TOKEN")
        resposta = client.cria({"emitente": "12345678000195"})

        self.assertEqual(resposta, {"id": 123})
        req = mock_urlopen.call_args[0][0]
        self.assertTrue(req.full_url.endswith("/servico"))
        self.assertEqual(req.get_method(), "POST")
        self.assertEqual(req.headers.get("Authorization"), "MEU_TOKEN")

    @patch("urllib.request.urlopen")
    def test_consulta_com_chave_valida_deve_consultar_endpoint_get(self, mock_urlopen):
        chave = "35230912345678000195550010000000011234567890"
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = json.dumps({"status": "autorizada"}).encode("utf-8")
        mock_urlopen.return_value = mock_resp

        client = IntegraClass(is_production=False, token="MEU_TOKEN")
        resposta = client.consulta(chave)

        self.assertEqual(resposta, {"status": "autorizada"})
        req = mock_urlopen.call_args[0][0]
        self.assertTrue(req.full_url.endswith(f"/servico/{chave}"))
        self.assertEqual(req.get_method(), "GET")
        self.assertEqual(req.headers.get("Authorization"), "MEU_TOKEN")

    @patch("urllib.request.urlopen")
    def test_consulta_extrai_chave_do_payload(self, mock_urlopen):
        chave = "35230912345678000195550010000000011234567890"
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = json.dumps({"status": "autorizada"}).encode("utf-8")
        mock_urlopen.return_value = mock_resp

        client = IntegraClass(is_production=False, token="MEU_TOKEN")
        client.consulta(payload={"chave": chave})

        req = mock_urlopen.call_args[0][0]
        self.assertTrue(req.full_url.endswith(f"/servico/{chave}"))

    @patch("urllib.request.urlopen")
    def test_consulta_com_chave_invalida_lanca_value_error_sem_chamar_rede(self, mock_urlopen):
        client = IntegraClass(is_production=False, token="MEU_TOKEN")

        with self.assertRaises(ValueError) as ctx:
            client.consulta("chave_invalida_curta")

        self.assertIn("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.", str(ctx.exception))
        mock_urlopen.assert_not_called()

    @patch("urllib.request.urlopen")
    def test_cancela_deve_enviar_post_para_servico_cancela(self, mock_urlopen):
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = json.dumps({"cancelado": True}).encode("utf-8")
        mock_urlopen.return_value = mock_resp

        client = IntegraClass(is_production=False, token="MEU_TOKEN")
        payload = {
            "chave": "35230912345678000195550010000000011234567890",
            "justificativa": "Cancelamento por desacordo",
        }
        resposta = client.cancela(payload)

        self.assertEqual(resposta, {"cancelado": True})
        req = mock_urlopen.call_args[0][0]
        self.assertTrue(req.full_url.endswith("/servico/cancela"))
        self.assertEqual(req.get_method(), "POST")
        self.assertEqual(req.headers.get("Authorization"), "MEU_TOKEN")

    @patch("urllib.request.urlopen")
    def test_preview_deve_enviar_post_para_servico_preview(self, mock_urlopen):
        mock_resp = MagicMock()
        mock_resp.getcode.return_value = 200
        mock_resp.read.return_value = json.dumps({"pdf_base64": "JVBERi0xLjQK..."}).encode("utf-8")
        mock_urlopen.return_value = mock_resp

        client = IntegraClass(is_production=False, token="MEU_TOKEN")
        resposta = client.preview({"emitente": "12345678000195"})

        self.assertEqual(resposta, {"pdf_base64": "JVBERi0xLjQK..."})
        req = mock_urlopen.call_args[0][0]
        self.assertTrue(req.full_url.endswith("/servico/preview"))
        self.assertEqual(req.get_method(), "POST")
        self.assertEqual(req.headers.get("Authorization"), "MEU_TOKEN")


if __name__ == "__main__":
    unittest.main()
