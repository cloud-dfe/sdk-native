import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { IntegraClass } from "../src/IntegraClass.js";

describe("IntegraClass", () => {
  const tokensInvalidos = ["", "   ", null, undefined];

  for (const token of tokensInvalidos) {
    it(`deve lançar erro quando o token for inválido: "${token}"`, () => {
      assert.throws(
        () => new IntegraClass(false, token as any),
        /Token é obrigatório\./
      );
    });
  }

  it("cria deve enviar POST para /servico com cabeçalho de autenticação", async () => {
    let capturedUrl = "";
    let capturedOptions: any = null;

    const mockFetch = async (url: any, options: any) => {
      capturedUrl = String(url);
      capturedOptions = options;
      return new Response(JSON.stringify({ id: 123 }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    };

    const client = new IntegraClass(false, "MEU_TOKEN", false, mockFetch as any);
    const payload = { emitente: "12345678000195" };
    const resposta = await client.cria(payload);

    assert.deepEqual(resposta, { id: 123 });
    assert.match(capturedUrl, /\/servico$/);
    assert.equal(capturedOptions.method, "POST");
    assert.equal(capturedOptions.headers["Authorization"], "MEU_TOKEN");
  });

  it("consulta deve enviar GET para /servico/{chave} com chave válida de 44 dígitos", async () => {
    const chave = "35230912345678000195550010000000011234567890";
    let capturedUrl = "";
    let capturedOptions: any = null;

    const mockFetch = async (url: any, options: any) => {
      capturedUrl = String(url);
      capturedOptions = options;
      return new Response(JSON.stringify({ status: "autorizada" }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    };

    const client = new IntegraClass(false, "MEU_TOKEN", false, mockFetch as any);
    const resposta = await client.consulta(chave);

    assert.deepEqual(resposta, { status: "autorizada" });
    assert.match(capturedUrl, new RegExp(`/servico/${chave}$`));
    assert.equal(capturedOptions.method, "GET");
    assert.equal(capturedOptions.headers["Authorization"], "MEU_TOKEN");
  });

  it("consulta deve extrair a chave a partir do payload se não informada como primeiro parâmetro", async () => {
    const chave = "35230912345678000195550010000000011234567890";
    let capturedUrl = "";

    const mockFetch = async (url: any) => {
      capturedUrl = String(url);
      return new Response(JSON.stringify({ status: "autorizada" }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    };

    const client = new IntegraClass(false, "MEU_TOKEN", false, mockFetch as any);
    await client.consulta("", { chave });

    assert.match(capturedUrl, new RegExp(`/servico/${chave}$`));
  });

  it("consulta deve lançar erro se a chave for inválida sem chamar a rede", async () => {
    let redeChamada = false;
    const mockFetch = async () => {
      redeChamada = true;
      return new Response("{}", { status: 200 });
    };

    const client = new IntegraClass(false, "MEU_TOKEN", false, mockFetch as any);

    await assert.rejects(
      async () => client.consulta("123invalida"),
      /A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe\./
    );

    assert.equal(redeChamada, false);
  });

  it("cancela deve enviar POST para /servico/cancela", async () => {
    let capturedUrl = "";
    let capturedOptions: any = null;

    const mockFetch = async (url: any, options: any) => {
      capturedUrl = String(url);
      capturedOptions = options;
      return new Response(JSON.stringify({ cancelado: true }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    };

    const client = new IntegraClass(false, "MEU_TOKEN", false, mockFetch as any);
    const payload = {
      chave: "35230912345678000195550010000000011234567890",
      justificativa: "Cancelamento por desacordo"
    };

    const resposta = await client.cancela(payload);

    assert.deepEqual(resposta, { cancelado: true });
    assert.match(capturedUrl, /\/servico\/cancela$/);
    assert.equal(capturedOptions.method, "POST");
    assert.equal(capturedOptions.headers["Authorization"], "MEU_TOKEN");
  });

  it("preview deve enviar POST para /servico/preview", async () => {
    let capturedUrl = "";
    let capturedOptions: any = null;

    const mockFetch = async (url: any, options: any) => {
      capturedUrl = String(url);
      capturedOptions = options;
      return new Response(JSON.stringify({ pdf_base64: "JVBERi0xLjQK..." }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    };

    const client = new IntegraClass(false, "MEU_TOKEN", false, mockFetch as any);
    const payload = { emitente: "12345678000195" };

    const resposta = await client.preview(payload);

    assert.deepEqual(resposta, { pdf_base64: "JVBERi0xLjQK..." });
    assert.match(capturedUrl, /\/servico\/preview$/);
    assert.equal(capturedOptions.method, "POST");
    assert.equal(capturedOptions.headers["Authorization"], "MEU_TOKEN");
  });
});
