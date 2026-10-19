export class KeyValidator {
  /**
   * Valida se a chave de acesso informada é válida (44 ou 50 dígitos).
   *
   * @param chave Chave a ser validada.
   * @returns Chave limpa apenas com dígitos numéricos.
   * @throws Error Quando a chave não for válida.
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
