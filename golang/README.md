# IntegraNotas - SDK Go / Golang Nativa (Zero Dependências)

SDK nativa em Go puro para integração com a API da [IntegraNotas](https://integranotas.com.br).

Desenvolvida com foco em **zero dependências externas**, utilizando estritamente os pacotes da biblioteca padrão do Go (`net/http`, `encoding/json`, `context`, `time`).

---

## ⚡ Destaques e Filosofia

- **Zero Dependências Externas:** Nenhum pacote de terceiros no `go.mod`.
- **Go Idiomático:** Suporte a `context.Context` em todas as operações, structs fortemente tipadas e tipos de erro customizados.
- **Validação Antecipada:** Validação rigorosa de tokens e chaves fiscais (44 dígitos para DFe/NFe/CTe ou 50 dígitos para NFSe) antes de qualquer chamada HTTP.
- **Testes com Servidor Mock Nativo:** Testes unitários com o pacote padrão `net/http/httptest`.

---

## 📁 Estrutura de Arquivos

```text
golang/
├── go.mod                     # Módulo Go puro (zero dependências)
├── integranotas.go            # Fachada IntegraClass (Cria, Consulta, Cancela, Preview)
├── services.go                # Cliente HTTP nativo com net/http
├── validator.go               # Validação de formato de chave fiscal (CheckKey)
├── errors.go                  # Tipo de erro customizado IntegraError
├── integranotas_test.go       # Testes unitários de IntegraClass
├── services_test.go           # Testes unitários de IntegraServices com httptest
├── validator_test.go          # Testes unitários de CheckKey
├── examples/                  # Exemplos práticos executáveis
│   ├── cria/main.go           # Emissão de documento fiscal
│   ├── consulta/main.go       # Consulta por chave
│   ├── cancela/main.go        # Cancelamento de documento
│   └── preview/main.go        # Pré-visualização de documento
└── README.md                  # Documentação completa em português
```

---

## 💻 Como Utilizar no Código

### 1. Inicialização

```go
package main

import (
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
}
```

### 2. Emissão / Criação (`Cria`)

```go
payload := map[string]any{
    "natureza_operacao": "Venda de mercadoria",
    // Consulte https://integranotas.com.br/doc para os campos do payload
}

resposta, err := servico.Cria(ctx, payload)
if err != nil {
    log.Fatalf("Erro na emissão: %v", err)
}
fmt.Println(resposta)
```

### 3. Consulta de Documento Fiscal (`Consulta`)

Valida automaticamente se a chave possui **44 dígitos** (DFe/NFe/CTe) ou **50 dígitos** (NFSe):

```go
chave := "35230912345678000195550010000000011234567890"

resposta, err := servico.Consulta(ctx, chave, nil)
if err != nil {
    log.Fatalf("Erro na consulta: %v", err)
}
fmt.Println(resposta)
```

### 4. Cancelamento (`Cancela`)

```go
payload := map[string]any{
    "chave":         "35230912345678000195550010000000011234567890",
    "justificativa": "Cancelamento por desacordo comercial",
}

resposta, err := servico.Cancela(ctx, payload)
if err != nil {
    log.Fatalf("Erro no cancelamento: %v", err)
}
fmt.Println(resposta)
```

### 5. Pré-visualização (`Preview`)

```go
payload := map[string]any{
    // Payload do documento para geração prévia de PDF/DANFE
}

resposta, err := servico.Preview(ctx, payload)
if err != nil {
    log.Fatalf("Erro no preview: %v", err)
}
fmt.Println(resposta)
```

---

## 🛡️ Tratamento de Exceções e Erros

A SDK disponibiliza o tipo `*integranotas.IntegraError`:

```go
resposta, err := servico.Cria(ctx, payload)
if err != nil {
    if integraErr, ok := err.(*integranotas.IntegraError); ok {
        fmt.Printf("Erro na API (HTTP %d): %s\n", integraErr.HTTPCode, integraErr.Message)
        if integraErr.Debug != nil {
            fmt.Printf("Debug Info: %+v\n", integraErr.Debug)
        }
    } else {
        fmt.Printf("Erro de validação ou inesperado: %v\n", err)
    }
}
```

---

## 🐳 Testes e Execução com Docker

O ambiente de testes está configurado na pasta isolada `.docker-dev-golang/`.

### Executar a Suíte de Testes no Container:

```bash
docker compose -f .docker-dev-golang/docker-compose.yml run --rm sdk-golang
```

### Executar os Exemplos no Container:

```bash
# Exemplo de Criação:
docker compose -f .docker-dev-golang/docker-compose.yml run --rm sdk-golang go run examples/cria/main.go

# Exemplo de Consulta:
docker compose -f .docker-dev-golang/docker-compose.yml run --rm sdk-golang go run examples/consulta/main.go

# Exemplo de Cancelamento:
docker compose -f .docker-dev-golang/docker-compose.yml run --rm sdk-golang go run examples/cancela/main.go

# Exemplo de Preview:
docker compose -f .docker-dev-golang/docker-compose.yml run --rm sdk-golang go run examples/preview/main.go
```

### Executar Localmente (Sem Docker):

```bash
cd golang
go test -v ./...
go run examples/cria/main.go
```
