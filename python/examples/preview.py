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

        # Payload do documento para geração prévia de PDF/DANFE
        payload = {
            "natureza_operacao": "Venda de mercadoria",
            # ... demais campos
        }

        resp = servico.preview(payload)
        print("Resposta da pré-visualização:", json.dumps(resp, indent=2, ensure_ascii=False))

    except IntegraException as e:
        print(f"Erro na API (HTTP {e.http_code}): {e.message}")
        if e.debug_info:
            print("Debug:", json.dumps(e.debug_info, indent=2, ensure_ascii=False))
    except Exception as e:
        print(f"Erro inesperado: {e}")


if __name__ == "__main__":
    main()
