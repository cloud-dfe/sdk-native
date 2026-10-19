package integranotas

import (
	"context"
	"errors"
	"net/http"
	"strings"
)

// IntegraClass é a fachada principal para comunicação com a API da IntegraNotas em Go.
type IntegraClass struct {
	Services *IntegraServices
	Token    string
}

// NewIntegraClass inicializa uma nova instância de IntegraClass.
func NewIntegraClass(isProduction bool, token string, debug bool) (*IntegraClass, error) {
	return NewIntegraClassWithClient(isProduction, token, debug, nil)
}

// NewIntegraClassWithClient permite injetar um http.Client customizado (útil para testes).
func NewIntegraClassWithClient(isProduction bool, token string, debug bool, httpClient *http.Client) (*IntegraClass, error) {
	trimmedToken := strings.TrimSpace(token)
	if trimmedToken == "" {
		return nil, errors.New("token é obrigatório")
	}

	services := NewIntegraServices(isProduction, "1", debug, httpClient)
	return &IntegraClass{
		Services: services,
		Token:    trimmedToken,
	}, nil
}

func (c *IntegraClass) getHeaders() map[string]string {
	return map[string]string{
		"Authorization": c.Token,
		"Accept":        "application/json",
	}
}

// Cria envia o payload de emissão do documento fiscal para a API.
func (c *IntegraClass) Cria(ctx context.Context, payload any) (any, error) {
	return c.Services.Request(ctx, http.MethodPost, "/servico", payload, c.getHeaders(), "")
}

// Consulta obtém o status de um documento fiscal a partir da chave de acesso.
func (c *IntegraClass) Consulta(ctx context.Context, chave string, payload any) (any, error) {
	targetKey := chave
	if strings.TrimSpace(targetKey) == "" && payload != nil {
		if m, ok := payload.(map[string]any); ok {
			if k, exists := m["chave"].(string); exists {
				targetKey = k
			}
		} else if m, ok := payload.(map[string]string); ok {
			if k, exists := m["chave"]; exists {
				targetKey = k
			}
		}
	}

	cleanKey, err := CheckKey(targetKey)
	if err != nil {
		return nil, err
	}

	return c.Services.Request(ctx, http.MethodGet, "/servico/"+cleanKey, payload, c.getHeaders(), "")
}

// Cancela envia uma solicitação de cancelamento de documento fiscal.
func (c *IntegraClass) Cancela(ctx context.Context, payload any) (any, error) {
	return c.Services.Request(ctx, http.MethodPost, "/servico/cancela", payload, c.getHeaders(), "")
}

// Preview gera a pré-visualização do documento antes da emissão definitiva.
func (c *IntegraClass) Preview(ctx context.Context, payload any) (any, error) {
	return c.Services.Request(ctx, http.MethodPost, "/servico/preview", payload, c.getHeaders(), "")
}
