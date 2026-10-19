package integranotas

import (
	"errors"
	"regexp"
	"strings"
)

var nonDigitsRegex = regexp.MustCompile(`\D`)

// CheckKey valida se a chave de acesso informada é válida (44 ou 50 dígitos).
func CheckKey(chave string) (string, error) {
	if strings.TrimSpace(chave) == "" {
		return "", errors.New("a chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe")
	}

	cleanKey := nonDigitsRegex.ReplaceAllString(chave, "")
	length := len(cleanKey)

	if length != 44 && length != 50 {
		return "", errors.New("a chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe")
	}

	return cleanKey, nil
}
