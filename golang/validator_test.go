package integranotas

import (
	"strings"
	"testing"
)

func TestCheckKey_ComChave44Digitos(t *testing.T) {
	chaveOriginal := "35230912345678000195550010000000011234567890"
	resultado, err := CheckKey(chaveOriginal)
	if err != nil {
		t.Fatalf("esperava sucesso, obteve erro: %v", err)
	}
	if resultado != chaveOriginal {
		t.Errorf("esperava %s, obteve %s", chaveOriginal, resultado)
	}
	if len(resultado) != 44 {
		t.Errorf("esperava tamanho 44, obteve %d", len(resultado))
	}
}

func TestCheckKey_ComChave50Digitos(t *testing.T) {
	chaveOriginal := "35230912345678000195550010000000011234567890123456"
	resultado, err := CheckKey(chaveOriginal)
	if err != nil {
		t.Fatalf("esperava sucesso, obteve erro: %v", err)
	}
	if resultado != chaveOriginal {
		t.Errorf("esperava %s, obteve %s", chaveOriginal, resultado)
	}
	if len(resultado) != 50 {
		t.Errorf("esperava tamanho 50, obteve %d", len(resultado))
	}
}

func TestCheckKey_ComPontuacoesEEspacos(t *testing.T) {
	chaveFormatada := "3523.0912.3456.7800.0195-5500.1000.0000.0112-3456.7890"
	resultado, err := CheckKey(chaveFormatada)
	if err != nil {
		t.Fatalf("esperava sucesso, obteve erro: %v", err)
	}
	if len(resultado) != 44 {
		t.Errorf("esperava tamanho 44, obteve %d", len(resultado))
	}
	if strings.Contains(resultado, ".") || strings.Contains(resultado, "-") {
		t.Errorf("resultado não deve conter pontuações: %s", resultado)
	}
}

func TestCheckKey_ComTamanhosInvalidos(t *testing.T) {
	chavesInvalidas := []string{
		"",
		"   ",
		"12345",
		"3523091234567800019555001000000001123456789",          // 43 dígitos
		"352309123456780001955500100000000112345678901",         // 45 dígitos
		"352309123456780001955500100000000112345678901234567",   // 51 dígitos
	}

	for _, chave := range chavesInvalidas {
		t.Run("chave_"+chave, func(t *testing.T) {
			_, err := CheckKey(chave)
			if err == nil {
				t.Errorf("esperava erro para chave inválida: %q", chave)
			}
			if !strings.Contains(err.Error(), "a chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe") {
				t.Errorf("mensagem de erro inesperada: %v", err)
			}
		})
	}
}
