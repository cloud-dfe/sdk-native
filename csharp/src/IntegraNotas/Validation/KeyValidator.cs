namespace IntegraNotas.Validation;

/// <summary>
/// Utilitário de validação de chave de acesso de documentos fiscais eletrônicos.
/// </summary>
public static class KeyValidator
{
    /// <summary>
    /// Valida e sanitiza a chave de acesso do documento fiscal eletrônico (DFe: 44 dígitos) ou NFSe (50 dígitos).
    /// </summary>
    /// <param name="chave">Chave de acesso a ser validada.</param>
    /// <returns>A chave contendo apenas dígitos numéricos.</returns>
    /// <throws cref="ArgumentException">Quando a chave não possuir 44 ou 50 dígitos.</throws>
    public static string CheckKey(string? chave)
    {
        if (string.IsNullOrWhiteSpace(chave))
        {
            throw new ArgumentException("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.", nameof(chave));
        }

        string cleanKey = new string(chave.Where(char.IsDigit).ToArray());
        int length = cleanKey.Length;

        if (length != 44 && length != 50)
        {
            throw new ArgumentException("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.", nameof(chave));
        }

        return cleanKey;
    }
}
