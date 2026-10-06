# IntegraNotas - SDK C# (.NET 8)

SDK nativa em C# para integração com a API da [IntegraNotas](https://integranotas.com.br).

---

## 🚀 Estrutura do Projeto

- **`src/IntegraNotas/`**: Biblioteca de classes (.NET 8) com a fachada `IntegraClass`, serviços HTTP `IntegraServices`, validações e exceções personalizadas.
- **`tests/IntegraNotas.Tests/`**: Suíte de testes unitários xUnit com mocks de requisições HTTP e validações de regras fiscais.
- **`examples/IntegraNotas.Examples/`**: Exemplos executáveis equivalentes aos scripts do PHP (`cria`, `consulta`, `cancela`, `preview`).

---

## 🛠️ Como Utilizar no Código

### 1. Inicialização

```csharp
using IntegraNotas;

bool isProduction = false; // false = Homologação / Testes; true = Produção
string token = "SEU_TOKEN_AQUI";
bool debug = true; // Exibe detalhes completos da requisição em caso de erro

using var servico = new IntegraClass(isProduction, token, debug);
```

### 2. Emissão / Criação (`cria`)

```csharp
var payload = new Dictionary<string, object>
{
    ["natureza_operacao"] = "Venda de mercadoria",
    // Consulte https://integranotas.com.br/doc para os campos do payload
};

string resposta = await servico.CriaAsync(payload);
Console.WriteLine(resposta);
```

### 3. Consulta de Documento Fiscal (`consulta`)

```csharp
string chave = "35230912345678000195550010000000011234567890"; // 44 ou 50 dígitos
string resposta = await servico.ConsultaAsync(chave);
Console.WriteLine(resposta);
```

### 4. Cancelamento (`cancela`)

```csharp
var payload = new Dictionary<string, object>
{
    ["chave"] = "35230912345678000195550010000000011234567890",
    ["justificativa"] = "Cancelamento por desacordo comercial"
};

string resposta = await servico.CancelaAsync(payload);
Console.WriteLine(resposta);
```

### 5. Pré-visualização (`preview`)

```csharp
var payload = new Dictionary<string, object>();
string resposta = await servico.PreviewAsync(payload);
Console.WriteLine(resposta);
```

---

## 🐳 Executando com Docker

O ambiente de desenvolvimento e testes está configurado na pasta isolada `.docker-dev-csharp/`.

### Executar a suíte de testes unitários:

```bash
docker compose -f .docker-dev-csharp/docker-compose.yml run --rm sdk-csharp
```

### Executar os exemplos:

```bash
# Exemplo de Criação/Emissão:
docker compose -f .docker-dev-csharp/docker-compose.yml run --rm sdk-csharp dotnet run --project examples/IntegraNotas.Examples -- cria

# Exemplo de Consulta por Chave:
docker compose -f .docker-dev-csharp/docker-compose.yml run --rm sdk-csharp dotnet run --project examples/IntegraNotas.Examples -- consulta

# Exemplo de Cancelamento:
docker compose -f .docker-dev-csharp/docker-compose.yml run --rm sdk-csharp dotnet run --project examples/IntegraNotas.Examples -- cancela

# Exemplo de Preview:
docker compose -f .docker-dev-csharp/docker-compose.yml run --rm sdk-csharp dotnet run --project examples/IntegraNotas.Examples -- preview
```
