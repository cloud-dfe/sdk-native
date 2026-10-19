# IntegraNotas - SDK TypeScript Nativa

SDK nativa em TypeScript puro para integração com a API da [IntegraNotas](https://integranotas.com.br).

Desenvolvida com foco em **zero dependências de produção**, alta performance, tipagem estrita e compatibilidade total com ecossistemas modernos (Node.js 18+, Bun, Deno e Edge runtimes).

---

## ⚡ Destaques e Filosofia

- **Zero Dependências em Produção:** Utiliza a API nativa `fetch` (sem bibliotecas de terceiros como Axios no bundle final).
- **TypeScript Idiomático:** Tipos estritos, IntelliSense completo e assincronismo via `async/await` (`Promise`).
- **Validação Antecipada:** Validação rigorosa de tokens e chaves fiscais (44 dígitos para DFe/NFe/CTe ou 50 dígitos para NFSe) antes de qualquer chamada HTTP.
- **Suíte de Testes Nativa:** Testes unitários com mocks de requisição utilizando o test runner nativo do Node.js.

---

## 📁 Estrutura de Arquivos

```text
typescript/
├── src/
│   ├── IntegraClass.ts        # Fachada principal de integração com a API
│   ├── IntegraServices.ts     # Cliente HTTP baseado em fetch com tratamento de erros
│   ├── IntegraException.ts    # Erro customizado com status HTTP e payload detalhado
│   ├── validation/
│   │   └── KeyValidator.ts    # Validador de formato de chaves fiscais
│   └── index.ts               # Ponto de exportação da biblioteca
├── tests/                     # Suíte de testes unitários
│   ├── KeyValidator.test.ts
│   ├── IntegraServices.test.ts
│   └── IntegraClass.test.ts
├── examples/                  # Exemplos práticos de uso
│   ├── cria.ts                # Emissão de documento fiscal
│   ├── consulta.ts            # Consulta por chave
│   ├── cancela.ts             # Cancelamento de documento fiscal
│   └── preview.ts             # Pré-visualização de documento fiscal
├── package.json
├── tsconfig.json
└── README.md
```

---

## 💻 Como Utilizar no Código

### 1. Inicialização

```typescript
import { IntegraClass } from "./src/index.js";

const isProduction = false; // false = Homologação / Testes; true = Produção
const token = "SEU_TOKEN_AQUI"; // Token emitente ou softhouse
const debug = true; // Se true, inclui detalhes da requisição caso ocorra erro

const servico = new IntegraClass(isProduction, token, debug);
```

### 2. Emissão / Criação (`cria`)

```typescript
const payload = {
  natureza_operacao: "Venda de mercadoria"
  // Consulte a documentação dos campos em: https://integranotas.com.br/doc
};

try {
  const resposta = await servico.cria(payload);
  console.log(resposta);
} catch (error) {
  console.error("Erro na emissão:", error);
}
```

### 3. Consulta de Documento Fiscal (`consulta`)

Valida automaticamente se a chave possui **44 dígitos** (DFe/NFe/CTe) ou **50 dígitos** (NFSe):

```typescript
const chave = "35230912345678000195550010000000011234567890";

try {
  const resposta = await servico.consulta(chave);
  console.log(resposta);
} catch (error) {
  console.error("Erro na consulta:", error);
}
```

### 4. Cancelamento (`cancela`)

```typescript
const payload = {
  chave: "35230912345678000195550010000000011234567890",
  justificativa: "Cancelamento por desacordo comercial"
};

try {
  const resposta = await servico.cancela(payload);
  console.log(resposta);
} catch (error) {
  console.error("Erro no cancelamento:", error);
}
```

### 5. Pré-visualização (`preview`)

```typescript
const payload = {
  // Payload do documento para geração prévia de PDF/DANFE
};

try {
  const resposta = await servico.preview(payload);
  console.log(resposta);
} catch (error) {
  console.error("Erro no preview:", error);
}
```

---

## 🛡️ Tratamento de Exceções

A SDK disponibiliza a classe `IntegraException` para capturar falhas de rede, timeouts e respostas de erro da API:

```typescript
import { IntegraClass, IntegraException } from "./src/index.js";

try {
  const servico = new IntegraClass(isProduction, token, debug);
  const resposta = await servico.cria(payload);
} catch (error) {
  if (error instanceof IntegraException) {
    console.error(`Erro HTTP ${error.httpCode}: ${error.message}`);
    console.error("Detalhes do erro:", error.responseBody);
    if (error.debugInfo) {
      console.error("Debug:", error.debugInfo);
    }
  } else {
    console.error("Erro de validação ou inesperado:", error.message);
  }
}
```

---

## 🐳 Testes e Execução com Docker

O ambiente de desenvolvimento e testes está isolado na pasta `.docker-dev-typescript/`.

### Executar a Suíte de Testes no Container:

```bash
docker compose -f .docker-dev-typescript/docker-compose.yml run --rm sdk-typescript
```

### Executar os Exemplos no Container:

```bash
# Exemplo de Criação:
docker compose -f .docker-dev-typescript/docker-compose.yml run --rm sdk-typescript npx tsx examples/cria.ts

# Exemplo de Consulta:
docker compose -f .docker-dev-typescript/docker-compose.yml run --rm sdk-typescript npx tsx examples/consulta.ts

# Exemplo de Cancelamento:
docker compose -f .docker-dev-typescript/docker-compose.yml run --rm sdk-typescript npx tsx examples/cancela.ts

# Exemplo de Preview:
docker compose -f .docker-dev-typescript/docker-compose.yml run --rm sdk-typescript npx tsx examples/preview.ts
```

### Executar Localmente (Sem Docker):

```bash
cd typescript
npm install
npm test
```
