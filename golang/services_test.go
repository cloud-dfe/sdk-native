package integranotas

import (
	"context"
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"
)

func TestIntegraServices_BaseURIs(t *testing.T) {
	hom := NewIntegraServices(false, "1", false, nil)
	if hom.BaseURI != URITests {
		t.Errorf("esperava baseURI de homologação %s, obteve %s", URITests, hom.BaseURI)
	}

	prod := NewIntegraServices(true, "1", false, nil)
	if prod.BaseURI != URIProduction {
		t.Errorf("esperava baseURI de produção %s, obteve %s", URIProduction, prod.BaseURI)
	}
}

func TestIntegraServices_SetVersion(t *testing.T) {
	s := NewIntegraServices(false, "1", false, nil)
	if err := s.SetVersion("2"); err != nil {
		t.Fatalf("erro ao definir versão válida: %v", err)
	}
	if s.Version != "2" {
		t.Errorf("esperava versão 2, obteve %s", s.Version)
	}

	if err := s.SetVersion("   "); err == nil {
		t.Errorf("esperava erro ao definir versão vazia")
	}
}

func TestIntegraServices_RequestSucesso(t *testing.T) {
	var capturedMethod, capturedAuth, capturedContentType, capturedBody string

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		capturedMethod = r.Method
		capturedAuth = r.Header.Get("Authorization")
		capturedContentType = r.Header.Get("Content-Type")

		b, _ := io.ReadAll(r.Body)
		capturedBody = string(b)

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"status":"sucesso","id":123}`))
	}))
	defer server.Close()

	s := NewIntegraServices(false, "1", false, server.Client())
	s.BaseURI = server.URL + "/v"

	payload := map[string]any{"teste": true}
	headers := map[string]string{"Authorization": "TOKEN123"}

	resp, err := s.Request(context.Background(), "POST", "/servico", payload, headers, "")
	if err != nil {
		t.Fatalf("esperava sucesso, obteve erro: %v", err)
	}

	m, ok := resp.(map[string]any)
	if !ok {
		t.Fatalf("esperava map[string]any, obteve: %T", resp)
	}
	if m["status"] != "sucesso" || m["id"].(float64) != 123 {
		t.Errorf("resposta inesperada: %v", m)
	}

	if capturedMethod != "POST" {
		t.Errorf("esperava POST, obteve %s", capturedMethod)
	}
	if capturedAuth != "TOKEN123" {
		t.Errorf("esperava TOKEN123, obteve %s", capturedAuth)
	}
	if capturedContentType != "application/json" {
		t.Errorf("esperava application/json, obteve %s", capturedContentType)
	}
	if !strings.Contains(capturedBody, `"teste":true`) {
		t.Errorf("payload body inesperado: %s", capturedBody)
	}
}

func TestIntegraServices_ModoDebug(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(`{"ok":true}`))
	}))
	defer server.Close()

	s := NewIntegraServices(false, "1", true, server.Client())
	s.BaseURI = server.URL + "/v"

	resp, err := s.Request(context.Background(), "POST", "/servico", map[string]string{"k": "v"}, nil, "")
	if err != nil {
		t.Fatalf("esperava sucesso, obteve erro: %v", err)
	}

	debugWrapper, ok := resp.(map[string]any)
	if !ok {
		t.Fatalf("esperava map[string]any no modo debug, obteve %T", resp)
	}

	if debugWrapper["debug"] == nil || debugWrapper["ok"] != true {
		t.Errorf("esperava campos debug e ok na raiz, obteve: %v", debugWrapper)
	}
}

func TestIntegraServices_RespostaVazia(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("   "))
	}))
	defer server.Close()

	s := NewIntegraServices(false, "1", false, server.Client())
	s.BaseURI = server.URL + "/v"

	_, err := s.Request(context.Background(), "GET", "/servico/123", nil, nil, "")
	if err == nil {
		t.Fatalf("esperava erro para resposta vazia")
	}

	integraErr, ok := err.(*IntegraError)
	if !ok {
		t.Fatalf("esperava *IntegraError, obteve %T", err)
	}
	if integraErr.Message != "Resposta Vazia" {
		t.Errorf("mensagem de erro inesperada: %s", integraErr.Message)
	}
}

func TestIntegraServices_JSONInvalido(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("não é um json válido"))
	}))
	defer server.Close()

	s := NewIntegraServices(false, "1", false, server.Client())
	s.BaseURI = server.URL + "/v"

	_, err := s.Request(context.Background(), "GET", "/servico/123", nil, nil, "")
	if err == nil {
		t.Fatalf("esperava erro para JSON inválido")
	}

	integraErr, ok := err.(*IntegraError)
	if !ok {
		t.Fatalf("esperava *IntegraError, obteve %T", err)
	}
	if integraErr.Message != "Resposta JSON Inválida" {
		t.Errorf("mensagem de erro inesperada: %s", integraErr.Message)
	}
}

func TestIntegraServices_ErroHTTP(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusUnauthorized)
		w.Write([]byte(`{"erro":"não autorizado"}`))
	}))
	defer server.Close()

	s := NewIntegraServices(false, "1", false, server.Client())
	s.BaseURI = server.URL + "/v"

	_, err := s.Request(context.Background(), "GET", "/servico/123", nil, nil, "")
	if err == nil {
		t.Fatalf("esperava erro para status 401")
	}

	integraErr, ok := err.(*IntegraError)
	if !ok {
		t.Fatalf("esperava *IntegraError, obteve %T", err)
	}
	if integraErr.HTTPCode != 401 {
		t.Errorf("esperava HTTP 401, obteve %d", integraErr.HTTPCode)
	}
}

func TestIntegraServices_TimeoutContexto(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		time.Sleep(100 * time.Millisecond)
		w.Write([]byte(`{}`))
	}))
	defer server.Close()

	s := NewIntegraServices(false, "1", false, server.Client())
	s.BaseURI = server.URL + "/v"

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Millisecond)
	defer cancel()

	_, err := s.Request(ctx, "GET", "/servico/123", nil, nil, "")
	if err == nil {
		t.Fatalf("esperava erro de timeout")
	}

	integraErr, ok := err.(*IntegraError)
	if !ok {
		t.Fatalf("esperava *IntegraError, obteve %T", err)
	}
	if integraErr.HTTPCode != http.StatusRequestTimeout {
		t.Errorf("esperava código HTTP 408, obteve %d", integraErr.HTTPCode)
	}
}
