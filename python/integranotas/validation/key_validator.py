import re
from typing import Optional


class KeyValidator:
    """Validador de chaves de acesso para documentos fiscais eletrônicos."""

    @staticmethod
    def check_key(chave: Optional[str]) -> str:
        """Valida se a chave de acesso informada é válida (44 ou 50 dígitos).
        
        :param chave: Chave a ser validada.
        :return: Chave limpa contendo apenas dígitos numéricos.
        :raises ValueError: Quando a chave não for válida.
        """
        if not chave or not isinstance(chave, str):
            raise ValueError("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.")

        clean_key = re.sub(r"\D", "", chave)
        length = len(clean_key)

        if length not in (44, 50):
            raise ValueError("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.")

        return clean_key
