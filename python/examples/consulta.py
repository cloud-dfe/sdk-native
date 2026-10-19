import json
import os
import sys

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from integranotas import IntegraClass, IntegraException

is_production = False
token = "SEU_TOKEN_AQUI"
debug = True


def main():
    try:
        servico = IntegraClass(is_production=is_production, token=token, debug=debug)

        # Chave de 44 dígitos (DFe/NFe/CTe) ou 50 dígitos (NFSe)
        chave = "35230912345678000195550010000000011234567890"

        resp = servico.consulta(chave)
        print("Resposta da consulta:", json.dumps(resp, indent=2, ensure_ascii=False))

    except IntegraException as e:
        print(f"Erro na API (HTTP {e.http_code}): {e.message}")
        if e.debug_info:
            print("Debug:", json.dumps(e.debug_info, indent=2, ensure_ascii=False))
    except Exception as e:
        print(f"Erro inesperado: {e}")


if __name__ == "__main__":
    main()
