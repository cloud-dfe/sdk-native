package integranotas

import (
	"encoding/json"
)

// DebugInfo armazena os detalhes da requisição para depuração.
type DebugInfo struct {
	Method  string            `json:"method"`
	URL     string            `json:"url"`
	Body    any               `json:"body,omitempty"`
	Headers map[string]string `json:"headers,omitempty"`
}

// IntegraError representa um erro ocorrido na integração com a API da IntegraNotas.
type IntegraError struct {
	Message      string     `json:"message"`
	HTTPCode     int        `json:"http_code"`
	ResponseBody any        `json:"response,omitempty"`
	Debug        *DebugInfo `json:"debug,omitempty"`
	JSONError    string     `json:"json_error,omitempty"`
	Err          string     `json:"error,omitempty"`
}

// Error implementa a interface padrão error do Go serializando os dados em JSON.
func (e *IntegraError) Error() string {
	b, err := json.Marshal(e)
	if err != nil {
		return e.Message
	}
	return string(b)
}
