package br.com.integranotas.sdk;

import br.com.integranotas.sdk.validation.KeyValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class KeyValidatorTest {

    @Test
    void testCheckKey_ComChave44Digitos() {
        String chaveOriginal = "35230912345678000195550010000000011234567890";
        String resultado = KeyValidator.checkKey(chaveOriginal);

        assertEquals(chaveOriginal, resultado);
        assertEquals(44, resultado.length());
    }

    @Test
    void testCheckKey_ComChave50Digitos() {
        String chaveOriginal = "35230912345678000195550010000000011234567890123456";
        String resultado = KeyValidator.checkKey(chaveOriginal);

        assertEquals(chaveOriginal, resultado);
        assertEquals(50, resultado.length());
    }

    @Test
    void testCheckKey_ComPontuacoesEEspacos() {
        String chaveFormatada = "3523.0912.3456.7800.0195-5500.1000.0000.0112-3456.7890";
        String resultado = KeyValidator.checkKey(chaveFormatada);

        assertEquals(44, resultado.length());
        assertFalse(resultado.contains("."));
        assertFalse(resultado.contains("-"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "12345",
            "3523091234567800019555001000000001123456789",          // 43 dígitos
            "352309123456780001955500100000000112345678901",         // 45 dígitos
            "352309123456780001955500100000000112345678901234567"    // 51 dígitos
    })
    void testCheckKey_ComTamanhosInvalidos(String chaveInvalida) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                KeyValidator.checkKey(chaveInvalida)
        );
        assertTrue(ex.getMessage().contains("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe."));
    }

    @Test
    void testCheckKey_ComNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                KeyValidator.checkKey(null)
        );
        assertTrue(ex.getMessage().contains("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe."));
    }
}
