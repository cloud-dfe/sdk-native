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

	// Chave do documento fiscal (44 ou 50 dígitos)
	// Dica: Para captura contínua de notas, recomenda-se a utilização de Webhooks
	// em vez de consultas repetitivas (polling) via API.
	chave := "35230912345678000195550010000000011234567890"

	resposta, err := servico.Consulta(context.Background(), chave, nil)
	if err != nil {
		if integraErr, ok := err.(*integranotas.IntegraError); ok {
			log.Fatalf("Erro na API (HTTP %d): %s", integraErr.HTTPCode, integraErr.Error())
		}
		log.Fatalf("Erro inesperado: %v", err)
	}

	b, _ := json.MarshalIndent(resposta, "", "  ")
	fmt.Printf("Resposta da consulta:\n%s\n", string(b))
}
