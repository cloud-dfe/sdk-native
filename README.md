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

---

## 📚 Documentação da API

Para detalhes sobre estrutura de payloads, códigos de serviço e autenticação, consulte a documentação oficial da IntegraNotas:
- [Documentação Oficial IntegraNotas](https://integranotas.com.br/doc)
