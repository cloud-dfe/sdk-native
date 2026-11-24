package integranotas

import (
	"bytes"
	"context"
	"crypto/tls"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

const (
	URIProduction = "https://api.integranotas.com.br/v"
	URITests       = "https://hom-api.integranotas.com.br/v"
)

// IntegraServices gerencia requisições HTTP para a API da IntegraNotas.
type IntegraServices struct {
	IsProduction bool
	Version      string
	BaseURI      string
	Debug        bool
	HTTPClient   *http.Client
}

// NewIntegraServices inicializa uma nova instância de IntegraServices.
func NewIntegraServices(isProduction bool, version string, debug bool, httpClient *http.Client) *IntegraServices {
	if strings.TrimSpace(version) == "" {
		version = "1"
	}

	baseURI := URITests
	if isProduction {
		baseURI = URIProduction
	}

	client := httpClient
	if client == nil {
		transport := &http.Transport{
			TLSClientConfig: &tls.Config{
				InsecureSkipVerify: !isProduction, // Em homologação permite certificados de teste
			},
			ResponseHeaderTimeout: 60 * time.Second,
		}
		client = &http.Client{
			Transport: transport,
			Timeout:   60 * time.Second,
		}
	}

	return &IntegraServices{
		IsProduction: isProduction,
		Version:      version,
		BaseURI:      baseURI,
		Debug:        debug,
		HTTPClient:   client,
	}
}

// SetVersion define a versão da API utilizada.
func (s *IntegraServices) SetVersion(v string) error {
	trimmed := strings.TrimSpace(v)
	if trimmed == "" {
		return errors.New("a versão da API não pode ser vazia")
	}
	s.Version = trimmed
	return nil
}

// Request executa uma requisição HTTP para a API da IntegraNotas.
func (s *IntegraServices) Request(
	ctx context.Context,
	method string,
	endpoint string,
	body any,
	headers map[string]string,
	version string,
) (any, error) {
	if strings.TrimSpace(version) != "" {
		if err := s.SetVersion(version); err != nil {
			return nil, err
		}
	}

	url := fmt.Sprintf("%s%s%s", s.BaseURI, s.Version, endpoint)

	reqHeaders := make(map[string]string)
	reqHeaders["Accept"] = "application/json"
	for k, v := range headers {
		reqHeaders[k] = v
	}

	var bodyReader io.Reader
	var bodyBytes []byte
	if body != nil {
		reqHeaders["Content-Type"] = "application/json"
		switch b := body.(type) {
		case []byte:
			bodyBytes = b
		case string:
			bodyBytes = []byte(b)
		default:
			var err error
			bodyBytes, err = json.Marshal(body)
			if err != nil {
				return nil, fmt.Errorf("falha ao serializar payload JSON: %w", err)
			}
		}
		bodyReader = bytes.NewReader(bodyBytes)
	}

	var debugInfo *DebugInfo
	if s.Debug {
		debugInfo = &DebugInfo{
			Method:  method,
			URL:     url,
			Body:    body,
			Headers: reqHeaders,
		}
	}

	if ctx == nil {
		ctx = context.Background()
	}

	req, err := http.NewRequestWithContext(ctx, method, url, bodyReader)
	if err != nil {
		return nil, &IntegraError{
			Message:  "Erro na Criação da Requisição",
			HTTPCode: 0,
			Err:      err.Error(),
			Debug:    debugInfo,
		}
	}

	for k, v := range reqHeaders {
		req.Header.Set(k, v)
	}

	resp, err := s.HTTPClient.Do(req)
	if err != nil {
		if errors.Is(ctx.Err(), context.DeadlineExceeded) || errors.Is(err, context.DeadlineExceeded) {
			return nil, &IntegraError{
				Message:  "Timeout na Requisição",
				HTTPCode: http.StatusRequestTimeout,
				Err:      "Tempo limite de conexão excedido (60s)",
				Debug:    debugInfo,
			}
		}
		return nil, &IntegraError{
			Message:  "Erro na Requisição HTTP",
			HTTPCode: 0,
			Err:      err.Error(),
			Debug:    debugInfo,
		}
	}
	defer resp.Body.Close()

	respBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, &IntegraError{
			Message:  "Erro ao ler corpo da resposta",
			HTTPCode: resp.StatusCode,
			Err:      err.Error(),
			Debug:    debugInfo,
		}
	}

	if len(strings.TrimSpace(string(respBytes))) == 0 {
		return nil, &IntegraError{
			Message:  "Resposta Vazia",
			HTTPCode: resp.StatusCode,
			Debug:    debugInfo,
		}
	}

	var parsed any
	if err := json.Unmarshal(respBytes, &parsed); err != nil {
		return nil, &IntegraError{
			Message:      "Resposta JSON Inválida",
			HTTPCode:     resp.StatusCode,
			ResponseBody: string(respBytes),
			JSONError:    err.Error(),
			Debug:        debugInfo,
		}
	}

	if resp.StatusCode >= 400 {
		return nil, &IntegraError{
			Message:      fmt.Sprintf("Erro na API (HTTP %d)", resp.StatusCode),
			HTTPCode:     resp.StatusCode,
			ResponseBody: parsed,
			Debug:        debugInfo,
		}
	}

	if s.Debug {
		if parsedMap, ok := parsed.(map[string]any); ok {
			parsedMap["debug"] = debugInfo
			return parsedMap, nil
		}
		return map[string]any{
			"data":  parsed,
			"debug": debugInfo,
		}, nil
	}

	return parsed, nil
}
