package integranotas

import (
	"context"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestIntegraClass_ValidacaoToken(t *testing.T) {
	tokensInvalidos := []string{"", "   "}
	for _, tok := range tokensInvalidos {
		t.Run("token_"+tok, func(t *testing.T) {
			_, err := NewIntegraClass(false, tok, false)
			if err == nil {
				t.Fatalf("esperava erro para token inválido: %q", tok)
			}
			if !strings.Contains(err.Error(), "token é obrigatório") {
				t.Errorf("mensagem inesperada: %v", err)
			}
		})
	}
}

func TestIntegraClass_Cria(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != "POST" || !strings.HasSuffix(r.URL.Path, "/servico") {
			http.Error(w, "rota incorreta", http.StatusNotFound)
			return
		}
		if r.Header.Get("Authorization") != "MEU_TOKEN" {
			http.Error(w, "não autorizado", http.StatusUnauthorized)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		w.Write([]byte(`{"id":123}`))
	}))
	defer server.Close()

	client, err := NewIntegraClassWithClient(false, "MEU_TOKEN", false, server.Client())
	if err != nil {
		t.Fatalf("erro ao instanciar IntegraClass: %v", err)
	}
	client.Services.BaseURI = server.URL + "/v"

	resp, err := client.Cria(context.Background(), map[string]any{"natureza_operacao": "venda"})
	if err != nil {
		t.Fatalf("esperava sucesso, obteve erro: %v", err)
	}

	m := resp.(map[string]any)
	if m["id"].(float64) != 123 {
		t.Errorf("resposta inesperada: %v", m)
	}
}

func TestIntegraClass_Consulta(t *testing.T) {
	chave := "35230912345678000195550010000000011234567890"

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != "GET" || !strings.HasSuffix(r.URL.Path, "/servico/"+chave) {
			http.Error(w, "rota incorreta", http.StatusNotFound)
			return
		}
		if r.Header.Get("Authorization") != "MEU_TOKEN" {
			http.Error(w, "não autorizado", http.StatusUnauthorized)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		w.Write([]byte(`{"status":"autorizada"}`))
	}))
	defer server.Close()

	client, err := NewIntegraClassWithClient(false, "MEU_TOKEN", false, server.Client())
	if err != nil {
		t.Fatalf("erro ao instanciar: %v", err)
	}
	client.Services.BaseURI = server.URL + "/v"

	// Consulta passando chave direta
	resp, err := client.Consulta(context.Background(), chave, nil)
	if err != nil {
		t.Fatalf("esperava sucesso na consulta: %v", err)
	}
	m := resp.(map[string]any)
	if m["status"] != "autorizada" {
		t.Errorf("resposta inesperada: %v", m)
	}

	// Consulta extraindo chave do payload
	resp2, err := client.Consulta(context.Background(), "", map[string]any{"chave": chave})
	if err != nil {
		t.Fatalf("esperava sucesso extraindo do payload: %v", err)
	}
	m2 := resp2.(map[string]any)
	if m2["status"] != "autorizada" {
		t.Errorf("resposta inesperada: %v", m2)
	}
}

func TestIntegraClass_ConsultaChaveInvalida(t *testing.T) {
	called := false
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		called = true
		w.Write([]byte(`{}`))
	}))
	defer server.Close()

	client, _ := NewIntegraClassWithClient(false, "MEU_TOKEN", false, server.Client())
	client.Services.BaseURI = server.URL + "/v"

	_, err := client.Consulta(context.Background(), "chave_curta", nil)
	if err == nil {
		t.Fatalf("esperava erro de validação de chave")
	}
	if called {
		t.Errorf("requisição HTTP não deveria ter sido executada para chave inválida")
	}
}

func TestIntegraClass_Cancela(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != "POST" || !strings.HasSuffix(r.URL.Path, "/servico/cancela") {
			http.Error(w, "rota incorreta", http.StatusNotFound)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		w.Write([]byte(`{"cancelado":true}`))
	}))
	defer server.Close()

	client, _ := NewIntegraClassWithClient(false, "MEU_TOKEN", false, server.Client())
	client.Services.BaseURI = server.URL + "/v"

	resp, err := client.Cancela(context.Background(), map[string]string{
		"chave":         "35230912345678000195550010000000011234567890",
		"justificativa": "Cancelamento por desacordo",
	})
	if err != nil {
		t.Fatalf("esperava sucesso, obteve: %v", err)
	}

	m := resp.(map[string]any)
	if m["cancelado"] != true {
		t.Errorf("esperava cancelado: true, obteve: %v", m)
	}
}

func TestIntegraClass_Preview(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != "POST" || !strings.HasSuffix(r.URL.Path, "/servico/preview") {
			http.Error(w, "rota incorreta", http.StatusNotFound)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		w.Write([]byte(`{"pdf_base64":"JVBERi0xLjQK..."}`))
	}))
	defer server.Close()

	client, _ := NewIntegraClassWithClient(false, "MEU_TOKEN", false, server.Client())
	client.Services.BaseURI = server.URL + "/v"

	resp, err := client.Preview(context.Background(), map[string]any{"natureza_operacao": "venda"})
	if err != nil {
		t.Fatalf("esperava sucesso no preview: %v", err)
	}

	m := resp.(map[string]any)
	if m["pdf_base64"] != "JVBERi0xLjQK..." {
		t.Errorf("resposta inesperada: %v", m)
	}
}
