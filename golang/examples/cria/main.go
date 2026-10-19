package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"

	"github.com/integranotas/sdk-go"
)

func main() {
	isProduction := false    // false = Homologação / Testes; true = Produção
	token := "SEU_TOKEN_AQUI" // Token emitente ou softhouse
	debug := true            // Se true, inclui detalhes da requisição caso ocorra erro

	servico, err := integranotas.NewIntegraClass(isProduction, token, debug)
	if err != nil {
		log.Fatalf("Erro ao inicializar SDK: %v", err)
	}

	// Monte o payload da nota fiscal
	// Consulte a documentação dos campos em: https://integranotas.com.br/doc
	payload := map[string]any{
		"natureza_operacao": "Venda de mercadoria",
		// ... demais campos da nota
	}

	resposta, err := servico.Cria(context.Background(), payload)
	if err != nil {
		if integraErr, ok := err.(*integranotas.IntegraError); ok {
			log.Fatalf("Erro na API (HTTP %d): %s", integraErr.HTTPCode, integraErr.Error())
		}
		log.Fatalf("Erro inesperado: %v", err)
	}

	b, _ := json.MarshalIndent(resposta, "", "  ")
	fmt.Printf("Resposta da emissão:\n%s\n", string(b))
}
