# IntegraNotas - Pacotes de Distribuição (Drop-in SDKs)

Esta pasta reúne os arquivos compactados (`.zip`) contendo **exclusivamente o código-fonte de produção** de cada SDK.

Estes pacotes são ideais para desenvolvedores, empresas e clientes que desejam apenas **copiar e colar** os arquivos diretamente na árvore do seu projeto, sem carregar testes, containers Docker ou ferramentas de desenvolvimento.

---

## 📦 Pacotes Disponíveis

| Linguagem | Arquivo | Conteúdo Principal | Como Usar | Download Direto |
| :--- | :--- | :--- | :--- | :--- |
| **PHP** | `integranotas-php.zip` | `IntegraClass.php`, `IntegraServices.php` | Copie para a pasta de bibliotecas do seu projeto (ou ScriptCase) e use `require_once` | [📥 Baixar (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-php.zip) |
| **C#** | `integranotas-csharp.zip` | `IntegraClass.cs`, `IntegraServices.cs`, `Validation/` | Adicione os arquivos ao seu projeto .NET ou importe como referência de projeto | [📥 Baixar (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-csharp.zip) |
| **TypeScript** | `integranotas-typescript.zip` | `src/*.ts`, `package.json`, `tsconfig.json` | Copie a pasta `src/` para o seu projeto TypeScript ou importe via `index.ts` | [📥 Baixar (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-typescript.zip) |
| **Python** | `integranotas-python.zip` | `integranotas/*.py`, `pyproject.toml` | Copie o pacote `integranotas/` diretamente para a raiz do seu projeto Python | [📥 Baixar (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-python.zip) |
| **Go (Golang)** | `integranotas-golang.zip` | `integranotas.go`, `services.go`, `validator.go`, `errors.go` | Copie os arquivos para um pacote local ou importe o módulo | [📥 Baixar (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-golang.zip) |
| **Java** | `integranotas-java.zip` | `src/main/java/br/com/integranotas/sdk/*.java`, `pom.xml` | Copie os fontes para o seu `src/main/java` ou use o `pom.xml` como submódulo Maven | [📥 Baixar (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-java.zip) |
| **Rust** | `integranotas-rust.zip` | `src/*.rs`, `Cargo.toml` | Copie o diretório `src/` ou importe como path dependency no seu `Cargo.toml` | [📥 Baixar (.zip)](https://github.com/cloud-dfe/sdk-native/raw/master/packages/integranotas-rust.zip) |

---

## 🔄 Como Regenerar os Pacotes

Os pacotes `.zip` são gerados automaticamente a partir do código-fonte original do repositório (garantindo que não haja duplicação manual de código).

Para atualizar todos os arquivos `.zip` após realizar alterações em qualquer SDK, execute:

```bash
python3 scripts/build_packages.py
```
