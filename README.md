# IntegraNotas - SDKs Nativas de Integração

Repositório contendo implementações nativas e puras da SDK de integração com a API da [IntegraNotas](https://integranotas.com.br).

---

## 📦 SDKs Disponíveis

| Linguagem | Plataforma | Filosofia / Foco | Documentação | Download Direto (.zip) |
| :--- | :--- | :--- | :--- | :--- |
| **PHP** | PHP 5.6+ / 7.x / 8.x | 100% Nativo, **Zero Composer**, compatível com ScriptCase e sistemas legados | [Ver Documentação PHP](./php/README.md) | [📥 Baixar PHP (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-php.zip) |
| **C#** | .NET 8 LTS | Idiomático, assíncrono (TAP), alta performance com `System.Text.Json` | [Ver Documentação C#](./csharp/README.md) | [📥 Baixar C# (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-csharp.zip) |
| **TypeScript** | Node.js 18+ / Edge | 100% Nativo, **Zero Dependências**, baseado na Fetch API padrão | [Ver Documentação TypeScript](./typescript/README.md) | [📥 Baixar TypeScript (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-typescript.zip) |
| **Python** | Python 3.8+ | 100% Nativo, **Zero Pip**, baseado na biblioteca padrão (`urllib`) | [Ver Documentação Python](./python/README.md) | [📥 Baixar Python (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-python.zip) |
| **Go (Golang)** | Go 1.22+ | 100% Nativo, **Zero Dependências**, baseado em `net/http` e `context` | [Ver Documentação Go](./golang/README.md) | [📥 Baixar Go (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-golang.zip) |
| **Java** | Java 17+ / 21 LTS | 100% Nativo, **Zero Dependências**, baseado em `java.net.http.HttpClient` | [Ver Documentação Java](./java/README.md) | [📥 Baixar Java (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-java.zip) |
| **Rust** | Rust 1.70+ | Minimalista, síncrono e leve com `ureq` (sem Tokio ou runtime assíncrono) | [Ver Documentação Rust](./rust/README.md) | [📥 Baixar Rust (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-rust.zip) |

---

## 📁 Pacotes para Download Direto (Drop-in / Copiar e Colar)

Caso precise apenas do **código-fonte limpo de produção** para copiar e colar diretamente no projeto da sua empresa ou cliente (sem arquivos de teste, Docker ou scripts auxiliares), clique nos links abaixo para download imediato:

- [📥 **Download do Pacote PHP** (`integranotas-php.zip`)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-php.zip)
- [📥 **Download do Pacote C#** (`integranotas-csharp.zip`)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-csharp.zip)
- [📥 **Download do Pacote TypeScript** (`integranotas-typescript.zip`)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-typescript.zip)
- [📥 **Download do Pacote Python** (`integranotas-python.zip`)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-python.zip)
- [📥 **Download do Pacote Go / Golang** (`integranotas-golang.zip`)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-golang.zip)
- [📥 **Download do Pacote Java** (`integranotas-java.zip`)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-java.zip)
- [📥 **Download do Pacote Rust** (`integranotas-rust.zip`)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-rust.zip)

> Para detalhes do conteúdo de cada pacote consulte [`packages/README.md`](./packages/README.md).  
> Para regenerar ou atualizar todos os pacotes: `python3 scripts/build_packages.py`

---

## 🚀 Execução Rápida com Docker

Cada linguagem possui seu ambiente de desenvolvimento e testes completamente isolado em pastas `.docker-dev-<linguagem>/`:

### 🐘 Testes PHP:
```bash
docker compose -f .docker-dev-php/docker-compose.yml run --rm sdk-php
```

### 🔷 Testes C# (.NET 8):
```bash
docker compose -f .docker-dev-csharp/docker-compose.yml run --rm sdk-csharp
```

### 🟨 Testes TypeScript:
```bash
docker compose -f .docker-dev-typescript/docker-compose.yml run --rm sdk-typescript
```

### 🐍 Testes Python:
```bash
docker compose -f .docker-dev-python/docker-compose.yml run --rm sdk-python
```

### 🐹 Testes Go (Golang):
```bash
docker compose -f .docker-dev-golang/docker-compose.yml run --rm sdk-golang
```

### ☕ Testes Java:
```bash
docker compose -f .docker-dev-java/docker-compose.yml run --rm sdk-java
```

### 🦀 Testes Rust:
```bash
docker compose -f .docker-dev-rust/docker-compose.yml run --rm sdk-rust
```

---

## 📚 Documentação da API

Para detalhes sobre estrutura de payloads, códigos de serviço e autenticação, consulte a documentação oficial da IntegraNotas:
- [Documentação Oficial IntegraNotas](https://integranotas.com.br/doc)
