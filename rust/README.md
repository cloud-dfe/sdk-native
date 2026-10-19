# IntegraNotas - SDK Rust Nativa

SDK em Rust puro para integração com a API da [IntegraNotas](https://integranotas.com.br).

Desenvolvida com foco em **simplicidade, performance e baixo consumo de recursos**, utilizando o cliente HTTP minimalista e síncrono `ureq` com `serde_json`, **sem a necessidade de runtimes assíncronos pesados** (como Tokio).

---

## ⚡ Destaques e Filosofia

- **Zero Async Runtime:** Síncrona, direta e extremamente rápida para compilar e executar.
- **Rust Idiomático:** Tratamento rigoroso de erros com `Result<T, IntegraError>` (sem `panic!`).
- **Validação Antecipada:** Validação prévia de chaves fiscais (44 dígitos para DFe/NFe/CTe ou 50 dígitos para NFSe).
- **Testes com Servidor Mock Nativo:** Testes unitários com `std::net::TcpListener` da biblioteca padrão do Rust.

---

## 📁 Estrutura de Arquivos

```text
rust/
├── Cargo.toml                     # Manifesto do crate
├── src/
│   ├── lib.rs                     # Ponto de exportação pública da biblioteca
│   ├── client.rs                  # Fachada principal IntegraClass
│   ├── services.rs                # Cliente HTTP nativo baseado em ureq
│   ├── validator.rs               # Validação de formato de chaves fiscais
│   └── error.rs                   # Estrutura de erro customizada IntegraError
├── tests/                         # Suíte de testes unitários
│   ├── validator_test.rs          # Testes de chave fiscal
│   ├── services_test.rs           # Testes de HTTP e modo debug
│   └── client_test.rs             # Testes de integração da fachada
├── examples/                      # Exemplos executáveis com cargo run --example
│   ├── cria.rs                    # Emissão de documento fiscal
│   ├── consulta.rs                # Consulta por chave
│   ├── cancela.rs                 # Cancelamento de documento
│   └── preview.rs                 # Pré-visualização de documento
└── README.md                      # Documentação completa em português
```

---

## 💻 Como Utilizar no Código

### 1. Inicialização

```rust
use integranotas_sdk::IntegraClass;

let is_production = false; // false = Homologação / Testes; true = Produção
let token = "SEU_TOKEN_AQUI"; // Token emitente ou softhouse
let debug = true; // Se true, inclui detalhes da requisição caso ocorra erro

let servico = IntegraClass::new(is_production, token, debug)?;
```

### 2. Emissão / Criação (`cria`)

```rust
use serde_json::json;

let payload = json!({
    "natureza_operacao": "Venda de mercadoria"
    // Consulte https://integranotas.com.br/doc para os campos do payload
});

let resposta = servico.cria(&payload)?;
println!("Resposta: {}", resposta);
```

### 3. Consulta de Documento Fiscal (`consulta`)

Valida automaticamente se a chave possui **44 dígitos** (DFe/NFe/CTe) ou **50 dígitos** (NFSe):

```rust
let chave = "35230912345678000195550010000000011234567890";

let resposta = servico.consulta(Some(chave), None)?;
println!("Resposta: {}", resposta);
```

### 4. Cancelamento (`cancela`)

```rust
use serde_json::json;

let payload = json!({
    "chave": "35230912345678000195550010000000011234567890",
    "justificativa": "Cancelamento por desacordo comercial"
});

let resposta = servico.cancela(&payload)?;
println!("Resposta: {}", resposta);
```

### 5. Pré-visualização (`preview`)

```rust
use serde_json::json;

let payload = json!({
    // Payload do documento para geração prévia de PDF/DANFE
});

let resposta = servico.preview(&payload)?;
println!("Resposta: {}", resposta);
```

---

## 🛡️ Tratamento de Exceções e Erros

A SDK retorna erros detalhados através da estrutura `IntegraError`:

```rust
match servico.cria(&payload) {
    Ok(resposta) => println!("Sucesso: {}", resposta),
    Err(e) => {
        eprintln!("Erro HTTP {}: {}", e.http_code, e.message);
        if let Some(resp) = e.response {
            eprintln!("Resposta da API: {}", resp);
        }
        if let Some(debug) = e.debug {
            eprintln!("Debug: {:?}", debug);
        }
    }
}
```

---

## 🐳 Testes e Execução com Docker

O ambiente de testes está configurado na pasta isolada `.docker-dev-rust/`.

### Executar a Suíte de Testes no Container:

```bash
docker compose -f .docker-dev-rust/docker-compose.yml run --rm sdk-rust
```

### Executar os Exemplos no Container:

```bash
# Exemplo de Criação:
docker compose -f .docker-dev-rust/docker-compose.yml run --rm sdk-rust cargo run --example cria

# Exemplo de Consulta:
docker compose -f .docker-dev-rust/docker-compose.yml run --rm sdk-rust cargo run --example consulta

# Exemplo de Cancelamento:
docker compose -f .docker-dev-rust/docker-compose.yml run --rm sdk-rust cargo run --example cancela

# Exemplo de Preview:
docker compose -f .docker-dev-rust/docker-compose.yml run --rm sdk-rust cargo run --example preview
```

### Executar Localmente (Sem Docker):

```bash
cd rust
cargo test
cargo run --example cria
```
