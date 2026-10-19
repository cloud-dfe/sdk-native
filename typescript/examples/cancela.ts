import { IntegraClass, IntegraException } from "../src/index.js";

const isProduction = false;
const token = "SEU_TOKEN_AQUI";
const debug = true;

async function main() {
  try {
    const servico = new IntegraClass(isProduction, token, debug);

    const payload = {
      chave: "35230912345678000195550010000000011234567890",
      justificativa: "Cancelamento por desacordo comercial"
    };

    const resp = await servico.cancela(payload);
    console.log("Resposta do cancelamento:", JSON.stringify(resp, null, 2));
  } catch (error: any) {
    if (error instanceof IntegraException) {
      console.error(`Erro na API (HTTP ${error.httpCode}):`, error.message);
    } else {
      console.error("Erro inesperado:", error.message || error);
    }
  }
}

main();
