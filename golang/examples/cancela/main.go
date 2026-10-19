package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"

	"github.com/integranotas/sdk-go"
)

func main() {
	isProduction := false
	token := "SEU_TOKEN_AQUI"
	debug := true

	servico, err := integranotas.NewIntegraClass(isProduction, token, debug)
	if err != nil {
		log.Fatalf("Erro ao inicializar SDK: %v", err)
	}

	payload := map[string]any{
		"chave":         "35230912345678000195550010000000011234567890",
		"justificativa": "Cancelamento por desacordo comercial",
	}

	resposta, err := servico.Cancela(context.Background(), payload)
	if err != nil {
		if integraErr, ok := err.(*integranotas.IntegraError); ok {
			log.Fatalf("Erro na API (HTTP %d): %s", integraErr.HTTPCode, integraErr.Error())
		}
		log.Fatalf("Erro inesperado: %v", err)
	}

	b, _ := json.MarshalIndent(resposta, "", "  ")
	fmt.Printf("Resposta do cancelamento:\n%s\n", string(b))
}
