package br.com.integranotas.sdk.validation;

/**
 * Validador de chaves de acesso para documentos fiscais eletrônicos.
 */
public final class KeyValidator {

    private KeyValidator() {
        // Utilitário estático
    }

    /**
     * Valida a chave de acesso do documento fiscal eletrônico (DFe) ou nota fiscal de serviço eletrônica (NFSe).
     *
     * @param chave Chave a ser validada (44 dígitos para DFe/NFe/CTe ou 50 dígitos para NFSe).
     * @return Chave limpa contendo apenas dígitos numéricos.
     * @throws IllegalArgumentException Quando a chave não contiver 44 dígitos para DFe ou 50 dígitos para NFSe.
     */
    public static String checkKey(String chave) {
        if (chave == null || chave.trim().isEmpty()) {
            throw new IllegalArgumentException("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
        }

        String cleanKey = chave.replaceAll("\\D", "");
        int length = cleanKey.length();

        if (length != 44 && length != 50) {
            throw new IllegalArgumentException("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.");
        }

        return cleanKey;
    }
}
