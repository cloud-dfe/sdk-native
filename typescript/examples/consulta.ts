import { IntegraClass, IntegraException } from "../src/index.js";

const isProduction = false;
const token = "SEU_TOKEN_AQUI";
const debug = true;

async function main() {
  try {
    const servico = new IntegraClass(isProduction, token, debug);

    // Chave do documento fiscal (44 ou 50 dígitos)
    // Dica: Para captura contínua de notas, recomenda-se a utilização de Webhooks
    // em vez de consultas repetitivas (polling) via API.
    const chave = "35230912345678000195550010000000011234567890";

    const resp = await servico.consulta(chave);
    console.log("Resposta da consulta:", JSON.stringify(resp, null, 2));
  } catch (error: any) {
    if (error instanceof IntegraException) {
      console.error(`Erro na API (HTTP ${error.httpCode}):`, error.message);
    } else {
      console.error("Erro inesperado:", error.message || error);
    }
  }
}

main();
