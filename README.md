# IntegraNotas - SDKs Nativas de Integração

Repositório contendo implementações nativas e puras da SDK de integração com a API da [IntegraNotas](https://integranotas.com.br).

---

## 📦 SDKs Disponíveis

| Linguagem | Plataforma | Filosofia / Foco | Documentação |
| :--- | :--- | :--- | :--- |
| **PHP** | PHP 5.6+ / 7.x / 8.x | 100% Nativo, **Zero Composer**, compatível com ScriptCase e sistemas legados | [Ver Documentação PHP](./php/README.md) |
| **C#** | .NET 8 LTS | Idiomático, assíncrono (TAP), alta performance com `System.Text.Json` | [Ver Documentação C#](./csharp/README.md) |
| **TypeScript** | Node.js 18+ / Edge | 100% Nativo, **Zero Dependências**, baseado na Fetch API padrão | [Ver Documentação TypeScript](./typescript/README.md) |
| **Python** | Python 3.8+ | 100% Nativo, **Zero Pip**, baseado na biblioteca padrão (`urllib`) | [Ver Documentação Python](./python/README.md) |
| **Go (Golang)** | Go 1.22+ | 100% Nativo, **Zero Dependências**, baseado em `net/http` e `context` | [Ver Documentação Go](./golang/README.md) |
| **Java** | Java 17+ / 21 LTS | 100% Nativo, **Zero Dependências**, baseado em `java.net.http.HttpClient` | [Ver Documentação Java](./java/README.md) |
| **Rust** | Rust 1.70+ | Minimalista, síncrono e leve com `ureq` (sem Tokio ou runtime assíncrono) | [Ver Documentação Rust](./rust/README.md) |

---

## 📁 Pacotes para Download (Drop-in / Copiar e Colar)

Caso precise apenas do **código-fonte limpo de produção** para copiar e colar diretamente no projeto da sua empresa ou cliente (sem arquivos de teste, Docker ou scripts auxiliares), utilize os pacotes compactados disponíveis na pasta [`packages/`](./packages/README.md):

- [PHP (`integranotas-php.zip`)](./packages/integranotas-php.zip)
- [C# (`integranotas-csharp.zip`)](./packages/integranotas-csharp.zip)
- [TypeScript (`integranotas-typescript.zip`)](./packages/integranotas-typescript.zip)
- [Python (`integranotas-python.zip`)](./packages/integranotas-python.zip)
- [Go / Golang (`integranotas-golang.zip`)](./packages/integranotas-golang.zip)
- [Java (`integranotas-java.zip`)](./packages/integranotas-java.zip)
- [Rust (`integranotas-rust.zip`)](./packages/integranotas-rust.zip)

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
