# IntegraNotas - SDK PHP Nativa (Zero Composer)

SDK nativa em PHP puro para integração direta com a API da [IntegraNotas](https://integranotas.com.br).

Desenvolvida especificamente para desenvolvedores e plataformas que **não utilizam Composer**, como **ScriptCase**, sistemas legados, scripts cron e bibliotecas internas de ERPs.

---

## ⚡ Destaques e Filosofia

- **Zero Dependências:** Não requer Composer nem bibliotecas de terceiros (`vendor/`).
- **PHP Puro (Vanilla):** Utiliza apenas as extensões padrão `ext-curl` e `ext-json` (presentes por padrão em quase todas as hospedagens e servidores PHP).
- **Compatibilidade Ampla:** Funciona perfeitamente em PHP 5.6, 7.x e 8.x.
- **Fácil Inclusão:** Basta um `require_once` para começar a usar.

---

## 📁 Estrutura de Arquivos

```text
php/
├── IntegraClass.php       # Fachada principal de integração com a API
├── IntegraServices.php    # Cliente HTTP com cURL e tratamento de erros
├── examples/              # Exemplos de uso prontos para execução
│   ├── cria.php           # Emissão de documento fiscal
│   ├── consulta.php       # Consulta de documento fiscal por chave
│   ├── cancela.php        # Cancelamento de documento fiscal
│   └── preview.php        # Pré-visualização antes da emissão
└── tests/                 # Suíte de testes unitários nativa (sem Composer)
    ├── Assert.php         # Utilitário leve de asserções
    ├── KeyValidationTest.php
    ├── IntegraClassTest.php
    ├── IntegraServicesTest.php
    └── run_tests.php      # Runner executável de testes
```

---

## 🛠️ Como Integrar no ScriptCase

O ScriptCase permite utilizar a SDK facilmente através de Bibliotecas do sistema:

### Opção A: Como Biblioteca Externa (Recomendado)
1. No menu superior do ScriptCase, acesse: **Ferramentas** $\rightarrow$ **Bibliotecas Externas**.
2. Crie uma nova biblioteca (ex: `integranotas-sdk`) no nível de Projeto ou Público.
3. Faça o upload dos arquivos `IntegraClass.php` e `IntegraServices.php`.
4. Na aplicação do ScriptCase (ex: Formulário, Controle, Blank ou Grid):
   - Acesse **Programação** $\rightarrow$ **Bibliotecas**.
   - Marque a biblioteca `integranotas-sdk` como ativa.
   - Utilize a macro para carregar no evento desejado:
     ```php
     sc_include_library("prj", "integranotas-sdk", "IntegraClass.php", true, true);
     ```

### Opção B: Inclusão Direta (Caminho Físico / Blank)
Em aplicações do tipo *Blank* ou eventos com caminho absoluto:
```php
require_once "../_lib/libraries/integranotas/IntegraClass.php";

$servico = new IntegraClass(false, "{meu_token}", true);
$resposta = $servico->cria($payload);
```

---

## 💻 Como Utilizar no Código (PHP Puro)

### 1. Inicialização

```php
require_once __DIR__ . '/IntegraClass.php';

$isProduction = false; // false = Homologação / Testes; true = Produção
$token = "SEU_TOKEN_AQUI"; // Token emitente ou softhouse
$debug = true;  // Se true, inclui detalhes da requisição caso ocorra erro

$servico = new IntegraClass($isProduction, $token, $debug);
```

### 2. Emissão / Criação (`cria`)

```php
// Monte o array associativo com os dados da nota fiscal
// Consulte a documentação dos campos em: https://integranotas.com.br/doc
$payload = array(
    "natureza_operacao" => "Venda de mercadoria",
    // ... demais campos da nota
);

try {
    $resp = $servico->cria($payload);
    echo json_encode($resp);
} catch (\Exception $e) {
    echo "Erro: " . $e->getMessage();
}
```

### 3. Consulta por Chave (`consulta`)

Valida automaticamente se a chave possui formato válido (44 ou 50 dígitos):

```php
$chave = "35230912345678000195550010000000011234567890";

try {
    $resp = $servico->consulta($chave);
    echo json_encode($resp);
} catch (\Exception $e) {
    echo "Erro: " . $e->getMessage();
}
```

> [!TIP]
> **Captura Contínua de Notas:** Para captura contínua de notas, recomenda-se a utilização de **Webhooks** em vez de consultas repetitivas (*polling*) via API.

### 4. Cancelamento (`cancela`)

```php
$payload = array(
    "chave" => "35230912345678000195550010000000011234567890",
    "justificativa" => "Cancelamento por desacordo comercial"
);

try {
    $resp = $servico->cancela($payload);
    echo json_encode($resp);
} catch (\Exception $e) {
    echo "Erro: " . $e->getMessage();
}
```

### 5. Pré-visualização (`preview`)

```php
$payload = array(
    // Payload do documento para geração prévia de PDF/DANFE
);

try {
    $resp = $servico->preview($payload);
    echo json_encode($resp);
} catch (\Exception $e) {
    echo "Erro: " . $e->getMessage();
}
```

---

## 🛡️ Tratamento de Exceções

A SDK separa os erros de validação local dos erros da API:

```php
try {
    $servico = new IntegraClass($isProduction, $token, $debug);
    $resp = $servico->cria($payload);
} catch (\InvalidArgumentException $e) {
    // Disparado caso o token não seja informado
    echo "Erro de configuração: " . $e->getMessage();
} catch (\RuntimeException $e) {
    // Disparado em erros de HTTP, cURL ou JSON inválido retornado pela API
    // Se $debug estiver ativo, a mensagem conterá o JSON completo de debug
    echo "Erro na comunicação: " . $e->getMessage();
} catch (\Exception $e) {
    // Erros gerais (como validação de chave fiscal)
    echo "Erro: " . $e->getMessage();
}
```

---

## 🐳 Testes e Execução com Docker

O ambiente de desenvolvimento e testes para PHP está isolado na pasta `.docker-dev-php/`.

### Executar a Suíte de Testes Nativos no Container:

```bash
docker compose -f .docker-dev-php/docker-compose.yml run --rm sdk-php
```

### Executar os Exemplos no Container:

```bash
# Exemplo de Criação:
docker compose -f .docker-dev-php/docker-compose.yml run --rm sdk-php php php/examples/cria.php

# Exemplo de Consulta:
docker compose -f .docker-dev-php/docker-compose.yml run --rm sdk-php php php/examples/consulta.php

# Exemplo de Cancelamento:
docker compose -f .docker-dev-php/docker-compose.yml run --rm sdk-php php php/examples/cancela.php

# Exemplo de Preview:
docker compose -f .docker-dev-php/docker-compose.yml run --rm sdk-php php php/examples/preview.php
```

### Executar Localmente (Sem Docker):

```bash
php php/tests/run_tests.php
```
