# IntegraNotas - SDK Python Nativa (Zero Dependências)

SDK nativa em Python puro para integração com a API da [IntegraNotas](https://integranotas.com.br).

Desenvolvida com foco em **zero dependências externas (Zero Pip em runtime)**, utilizando estritamente os módulos da biblioteca padrão do Python (`urllib.request`, `json`, `ssl`).

---

## ⚡ Destaques e Filosofia

- **Zero Dependências:** Não requer `requests`, `urllib3` nem qualquer pacote via `pip` em produção.
- **Python Puro (Standard Library):** Utiliza apenas módulos nativos da linguagem (`urllib`, `json`, `re`).
- **Compatibilidade Ampla:** Funciona perfeitamente em Python 3.8, 3.9, 3.10, 3.11, 3.12+.
- **Validação Antecipada:** Validação rigorosa de tokens e chaves de acesso (44 dígitos para DFe/NFe/CTe ou 50 dígitos para NFSe) antes de qualquer requisição.
- **Suíte de Testes com Mocks Nativos:** Testes unitários com o framework nativo `unittest`, sem necessidade de bibliotecas de terceiros.

---

## 📁 Estrutura de Arquivos

```text
python/
├── integranotas/              # Pacote principal da SDK
│   ├── __init__.py            # Exportação pública (IntegraClass, IntegraServices, etc.)
│   ├── integra_class.py       # Fachada principal de integração com a API
│   ├── integra_services.py    # Cliente HTTP nativo com urllib e tratamento de erros
│   ├── exceptions.py          # Classe personalizada de exceção (IntegraException)
│   └── validation/
│       ├── __init__.py
│       └── key_validator.py   # Validação de formato de chaves fiscais
├── tests/                     # Suíte de testes unitários nativa (unittest)
│   ├── __init__.py
│   ├── test_key_validator.py
│   ├── test_integra_services.py
│   └── test_integra_class.py
├── examples/                  # Exemplos de uso prontos para execução
│   ├── cria.py                # Emissão de documento fiscal
│   ├── consulta.py            # Consulta por chave
│   ├── cancela.py             # Cancelamento de documento fiscal
│   └── preview.py             # Pré-visualização de documento
├── pyproject.toml             # Metadados de empacotamento padrão PEP 517/621
└── README.md                  # Documentação completa em português
```

---

## 💻 Como Utilizar no Código

### 1. Inicialização

```python
from integranotas import IntegraClass

is_production = False  # False = Homologação / Testes; True = Produção
token = "SEU_TOKEN_AQUI"  # Token emitente ou softhouse
debug = True  # Se True, inclui detalhes da requisição caso ocorra erro

servico = IntegraClass(is_production=is_production, token=token, debug=debug)
```

### 2. Emissão / Criação (`cria`)

```python
payload = {
    "natureza_operacao": "Venda de mercadoria",
    # Consulte https://integranotas.com.br/doc para os campos do payload
}

try:
    resposta = servico.cria(payload)
    print(resposta)
except Exception as e:
    print(f"Erro na emissão: {e}")
```

### 3. Consulta de Documento Fiscal (`consulta`)

Valida automaticamente se a chave possui **44 dígitos** (DFe/NFe/CTe) ou **50 dígitos** (NFSe):

```python
chave = "35230912345678000195550010000000011234567890"

try:
    resposta = servico.consulta(chave)
    print(resposta)
except Exception as e:
    print(f"Erro na consulta: {e}")
```

### 4. Cancelamento (`cancela`)

```python
payload = {
    "chave": "35230912345678000195550010000000011234567890",
    "justificativa": "Cancelamento por desacordo comercial",
}

try:
    resposta = servico.cancela(payload)
    print(resposta)
except Exception as e:
    print(f"Erro no cancelamento: {e}")
```

### 5. Pré-visualização (`preview`)

```python
payload = {
    # Payload do documento para geração prévia de PDF/DANFE
}

try:
    resposta = servico.preview(payload)
    print(resposta)
except Exception as e:
    print(f"Erro no preview: {e}")
```

---

## 🛡️ Tratamento de Exceções

A classe `IntegraException` permite capturar erros de rede, respostas inválidas ou status HTTP $\ge 400$:

```python
from integranotas import IntegraClass, IntegraException

try:
    servico = IntegraClass(is_production=is_production, token=token, debug=debug)
    resposta = servico.cria(payload)
except IntegraException as e:
    print(f"Erro da API (HTTP {e.http_code}): {e.message}")
    if e.debug_info:
        print("Dados de depuração:", e.debug_info)
except ValueError as e:
    print(f"Erro de validação: {e}")
except Exception as e:
    print(f"Erro inesperado: {e}")
```

---

## 🐳 Testes e Execução com Docker

O ambiente de desenvolvimento e testes está configurado na pasta isolada `.docker-dev-python/`.

### Executar a Suíte de Testes no Container:

```bash
docker compose -f .docker-dev-python/docker-compose.yml run --rm sdk-python
```

### Executar os Exemplos no Container:

```bash
# Exemplo de Criação:
docker compose -f .docker-dev-python/docker-compose.yml run --rm sdk-python python python/examples/cria.py

# Exemplo de Consulta:
docker compose -f .docker-dev-python/docker-compose.yml run --rm sdk-python python python/examples/consulta.py

# Exemplo de Cancelamento:
docker compose -f .docker-dev-python/docker-compose.yml run --rm sdk-python python python/examples/cancela.py

# Exemplo de Preview:
docker compose -f .docker-dev-python/docker-compose.yml run --rm sdk-python python python/examples/preview.py
```

### Executar Localmente (Sem Docker):

```bash
python -m unittest discover -s python/tests
```
