import unittest

from integranotas.validation.key_validator import KeyValidator


class TestKeyValidator(unittest.TestCase):
    def test_check_key_com_chave_44_digitos_deve_retornar_chave_limpa(self):
        chave_original = "35230912345678000195550010000000011234567890"
        resultado = KeyValidator.check_key(chave_original)
        self.assertEqual(resultado, chave_original)
        self.assertEqual(len(resultado), 44)

    def test_check_key_com_chave_50_digitos_deve_retornar_chave_limpa(self):
        chave_original = "35230912345678000195550010000000011234567890123456"
        resultado = KeyValidator.check_key(chave_original)
        self.assertEqual(resultado, chave_original)
        self.assertEqual(len(resultado), 50)

    def test_check_key_com_pontuacoes_e_espacos_deve_remover_nao_numericos(self):
        chave_formatada = "3523.0912.3456.7800.0195-5500.1000.0000.0112-3456.7890"
        resultado = KeyValidator.check_key(chave_formatada)
        self.assertEqual(len(resultado), 44)
        self.assertNotIn(".", resultado)
        self.assertNotIn("-", resultado)

    def test_check_key_com_tamanhos_invalidos_deve_lancar_value_error(self):
        chaves_invalidas = [
            "",
            None,
            "12345",
            "3523091234567800019555001000000001123456789",  # 43 dígitos
            "352309123456780001955500100000000112345678901",  # 45 dígitos
            "352309123456780001955500100000000112345678901234567",  # 51 dígitos
        ]

        for chave in chaves_invalidas:
            with self.subTest(chave=chave):
                with self.assertRaises(ValueError) as ctx:
                    KeyValidator.check_key(chave)  # type: ignore
                self.assertIn("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.", str(ctx.exception))


if __name__ == "__main__":
    unittest.main()
