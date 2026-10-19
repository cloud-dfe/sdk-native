import json
import os
import sys

# Garante que o pacote integranotas seja importado do diretório local
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from integranotas import IntegraClass, IntegraException

is_production = False  # False = Homologação / Testes; True = Produção
token = "SEU_TOKEN_AQUI"  # Token emitente ou softhouse
debug = True  # Se True, inclui detalhes da requisição caso ocorra erro


def main():
    try:
        servico = IntegraClass(is_production=is_production, token=token, debug=debug)

        # Monte o dicionário com os dados da nota fiscal
        # Consulte a documentação dos campos em: https://integranotas.com.br/doc
        payload = {
            "natureza_operacao": "Venda de mercadoria",
            # ... demais campos da nota
        }

        resp = servico.cria(payload)
        print("Resposta da emissão:", json.dumps(resp, indent=2, ensure_ascii=False))

    except IntegraException as e:
        print(f"Erro na API (HTTP {e.http_code}): {e.message}")
        if e.debug_info:
            print("Debug:", json.dumps(e.debug_info, indent=2, ensure_ascii=False))
    except Exception as e:
        print(f"Erro inesperado: {e}")


if __name__ == "__main__":
    main()
