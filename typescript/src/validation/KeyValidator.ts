export class KeyValidator {
  /**
   * Valida a chave de acesso do documento fiscal eletrônico (DFe) ou nota fiscal de serviço eletrônica (NFSe).
   *
   * @param chave Chave a ser validada (44 dígitos para DFe/NFe/CTe ou 50 dígitos para NFSe).
   * @returns Chave limpa apenas com dígitos numéricos.
   * @throws Error Quando a chave não contiver 44 dígitos para DFe ou 50 dígitos para NFSe.
   */
  public static checkKey(chave: string | null | undefined): string {
    if (!chave || typeof chave !== "string") {
      throw new Error("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
    }

    const cleanKey = chave.replace(/\D/g, "");
    const length = cleanKey.length;

    if (length !== 44 && length !== 50) {
      throw new Error("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
    }

    return cleanKey;
  }
}
