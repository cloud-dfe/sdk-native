import { IntegraClass, IntegraException } from "../src/index.js";

const isProduction = false; // false = Homologação / Testes; true = Produção
const token = "SEU_TOKEN_AQUI"; // Token emitente ou softhouse
const debug = true; // Se true, inclui detalhes da requisição caso ocorra erro

async function main() {
  try {
    const servico = new IntegraClass(isProduction, token, debug);

    // Monte o objeto com os dados da nota fiscal
    // Consulte a documentação dos campos em: https://integranotas.com.br/doc
    const payload = {
      natureza_operacao: "Venda de mercadoria"
      // ... demais campos da nota
    };

    const resp = await servico.cria(payload);
    console.log("Resposta da emissão:", JSON.stringify(resp, null, 2));
  } catch (error: any) {
    if (error instanceof IntegraException) {
      console.error(`Erro na API (HTTP ${error.httpCode}):`, error.message);
    } else {
      console.error("Erro inesperado:", error.message || error);
    }
  }
}

main();
