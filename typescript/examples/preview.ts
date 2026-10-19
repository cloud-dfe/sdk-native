import { IntegraClass, IntegraException } from "../src/index.js";

const isProduction = false;
const token = "SEU_TOKEN_AQUI";
const debug = true;

async function main() {
  try {
    const servico = new IntegraClass(isProduction, token, debug);

    // Payload do documento para geração prévia de PDF/DANFE
    const payload = {
      natureza_operacao: "Venda de mercadoria"
      // ... demais dados do documento
    };

    const resp = await servico.preview(payload);
    console.log("Resposta da pré-visualização:", JSON.stringify(resp, null, 2));
  } catch (error: any) {
    if (error instanceof IntegraException) {
      console.error(`Erro na API (HTTP ${error.httpCode}):`, error.message);
    } else {
      console.error("Erro inesperado:", error.message || error);
    }
  }
}

main();
