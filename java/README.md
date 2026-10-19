# IntegraNotas - SDK Java Nativa (Zero Dependências)

SDK nativa em Java puro para integração com a API da [IntegraNotas](https://integranotas.com.br).

Desenvolvida com foco em **zero dependências externas em runtime**, utilizando estritamente a API padrão `java.net.http.HttpClient` nativa do JDK (Java 17+ / 21 LTS).

---

## ⚡ Destaques e Filosofia

- **Zero Dependências em Runtime:** Não requer bibliotecas de terceiros (sem Apache HttpClient, OkHttp, Jackson ou Gson obrigatórios).
- **Java Moderno (LTS):** Desenvolvida e compatível com **Java 17** e **Java 21**.
- **Cliente HTTP Nativo:** Utiliza o moderno `java.net.http.HttpClient` com pool de conexões e suporte a timeouts.
- **Validação Antecipada:** Validação rigorosa de tokens e chaves fiscais (44 ou 50 dígitos) antes de qualquer chamada HTTP.
- **Testes com Servidor Mock Nativo:** Testes unitários com o servidor embutido do JDK (`com.sun.net.httpserver.HttpServer`) e JUnit 5 no escopo de testes.

---

## 📁 Estrutura de Arquivos

```text
java/
├── pom.xml                                       # Configuração do projeto Maven
├── src/
│   ├── main/java/br/com/integranotas/
│   │   ├── sdk/
│   │   │   ├── IntegraClass.java                 # Fachada principal da API
│   │   │   ├── IntegraServices.java              # Cliente HTTP nativo (HttpClient)
│   │   │   ├── IntegraException.java             # Exceção customizada da SDK
│   │   │   ├── util/
│   │   │   │   └── JsonUtils.java                # Utilitário nativo de JSON
│   │   │   └── validation/
│   │   │       └── KeyValidator.java             # Validação de formato de chave fiscal
│   │   └── examples/                             # Exemplos executáveis
│   │       ├── CriaExample.java                  # Emissão de documento fiscal
│   │       ├── ConsultaExample.java              # Consulta por chave
│   │       ├── CancelaExample.java               # Cancelamento de documento
│   │       └── PreviewExample.java               # Pré-visualização de documento
│   └── test/java/br/com/integranotas/sdk/
│       ├── KeyValidatorTest.java
│       ├── IntegraServicesTest.java
│       └── IntegraClassTest.java
└── README.md                                     # Documentação técnica completa
```

---

## 💻 Como Utilizar no Código

### 1. Inicialização

```java
import br.com.integranotas.sdk.IntegraClass;

boolean isProduction = false; // false = Homologação / Testes; true = Produção
String token = "SEU_TOKEN_AQUI"; // Token emitente ou softhouse
boolean debug = true; // Se true, inclui detalhes da requisição caso ocorra erro

IntegraClass servico = new IntegraClass(isProduction, token, debug);
```

### 2. Emissão / Criação (`cria`)

Aceita `Map<String, Object>` ou `String` formatada em JSON:

```java
Map<String, Object> payload = new LinkedHashMap<>();
payload.put("natureza_operacao", "Venda de mercadoria");
// Consulte https://integranotas.com.br/doc para os campos do payload

try {
    String resposta = servico.cria(payload);
    System.out.println(resposta);
} catch (Exception e) {
    System.err.println("Erro na emissão: " + e.getMessage());
}
```

### 3. Consulta de Documento Fiscal (`consulta`)

Valida automaticamente se a chave possui formato válido (44 ou 50 dígitos):

```java
String chave = "35230912345678000195550010000000011234567890";

try {
    String resposta = servico.consulta(chave, null);
    System.out.println(resposta);
} catch (Exception e) {
    System.err.println("Erro na consulta: " + e.getMessage());
}
```

> [!TIP]
> **Captura Contínua de Notas:** Para captura contínua de notas, recomenda-se a utilização de **Webhooks** em vez de consultas repetitivas (*polling*) via API.

### 4. Cancelamento (`cancela`)

```java
Map<String, Object> payload = new LinkedHashMap<>();
payload.put("chave", "35230912345678000195550010000000011234567890");
payload.put("justificativa", "Cancelamento por desacordo comercial");

try {
    String resposta = servico.cancela(payload);
    System.out.println(resposta);
} catch (Exception e) {
    System.err.println("Erro no cancelamento: " + e.getMessage());
}
```

### 5. Pré-visualização (`preview`)

```java
Map<String, Object> payload = new LinkedHashMap<>();
// Payload do documento para geração prévia de PDF/DANFE

try {
    String resposta = servico.preview(payload);
    System.out.println(resposta);
} catch (Exception e) {
    System.err.println("Erro no preview: " + e.getMessage());
}
```

---

## 🛡️ Tratamento de Exceções

A SDK lança `IntegraException` com status HTTP e corpo da resposta:

```java
import br.com.integranotas.sdk.IntegraClass;
import br.com.integranotas.sdk.IntegraException;

try {
    IntegraClass servico = new IntegraClass(isProduction, token, debug);
    String resposta = servico.cria(payload);
} catch (IntegraException e) {
    System.err.printf("Erro na API (HTTP %d): %s%n", e.getHttpCode(), e.getMessage());
    if (e.getDebugInfo() != null) {
        System.err.println("Debug: " + e.getDebugInfo());
    }
} catch (IllegalArgumentException e) {
    System.err.println("Erro de validação: " + e.getMessage());
}
```

---

## 🐳 Testes e Execução com Docker

O ambiente de testes está configurado na pasta isolada `.docker-dev-java/`.

### Executar a Suíte de Testes no Container:

```bash
docker compose -f .docker-dev-java/docker-compose.yml run --rm sdk-java
```

### Executar os Exemplos no Container:

```bash
# Exemplo de Criação:
docker compose -f .docker-dev-java/docker-compose.yml run --rm sdk-java mvn exec:java -Dexec.mainClass="br.com.integranotas.examples.CriaExample"

# Exemplo de Consulta:
docker compose -f .docker-dev-java/docker-compose.yml run --rm sdk-java mvn exec:java -Dexec.mainClass="br.com.integranotas.examples.ConsultaExample"

# Exemplo de Cancelamento:
docker compose -f .docker-dev-java/docker-compose.yml run --rm sdk-java mvn exec:java -Dexec.mainClass="br.com.integranotas.examples.CancelaExample"

# Exemplo de Preview:
docker compose -f .docker-dev-java/docker-compose.yml run --rm sdk-java mvn exec:java -Dexec.mainClass="br.com.integranotas.examples.PreviewExample"
```

### Executar Localmente (Sem Docker):

```bash
cd java
mvn test
```
