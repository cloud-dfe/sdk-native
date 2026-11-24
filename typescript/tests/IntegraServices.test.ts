import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { IntegraServices } from "../src/IntegraServices.js";
import { IntegraException } from "../src/IntegraException.js";

describe("IntegraServices", () => {
  it("deve definir baseUri de homologação por padrão", () => {
    const services = new IntegraServices(false);
    assert.equal(services.baseUri, "https://hom-api.integranotas.com.br/v");
    assert.equal(services.isProduction, false);
    assert.equal(services.version, "1");
  });

  it("deve definir baseUri de produção quando isProduction for true", () => {
    const services = new IntegraServices(true);
    assert.equal(services.baseUri, "https://api.integranotas.com.br/v");
    assert.equal(services.isProduction, true);
  });

  it("deve permitir alterar versão da API", () => {
    const services = new IntegraServices();
    services.setVersion("2");
    assert.equal(services.version, "2");
  });

  it("deve lançar erro ao definir versão vazia", () => {
    const services = new IntegraServices();
    assert.throws(() => services.setVersion(""), /A versão da API não pode ser vazia\./);
  });

  it("deve executar requisição com sucesso e retornar payload parseado", async () => {
    let capturedUrl = "";
    let capturedOptions: any = null;

    const mockFetch = async (url: any, options: any) => {
      capturedUrl = String(url);
      capturedOptions = options;
      return new Response(JSON.stringify({ status: "ok", id: 123 }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    };

    const services = new IntegraServices(false, "1", false, mockFetch as any);
    const resultado = await services.request("POST", "/servico", { teste: true }, { Authorization: "token123" });

    assert.deepEqual(resultado, { status: "ok", id: 123 });
    assert.equal(capturedUrl, "https://hom-api.integranotas.com.br/v1/servico");
    assert.equal(capturedOptions.method, "POST");
    assert.equal(capturedOptions.headers["Authorization"], "token123");
    assert.equal(capturedOptions.headers["Content-Type"], "application/json");
    assert.equal(capturedOptions.body, JSON.stringify({ teste: true }));
  });

  it("deve retornar wrapper de depuração quando debug estiver ativado", async () => {
    const mockFetch = async () => {
      return new Response(JSON.stringify({ status: "sucesso" }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    };

    const services = new IntegraServices(false, "1", true, mockFetch as any);
    const resultado: any = await services.request("POST", "/servico", { nome: "nota" });

    assert.ok(resultado.debug);
    assert.equal(resultado.debug.method, "POST");
    assert.equal(resultado.debug.url, "https://hom-api.integranotas.com.br/v1/servico");
    assert.equal(resultado.status, "sucesso");
  });

  it("deve lançar IntegraException quando resposta for vazia", async () => {
    const mockFetch = async () => new Response("", { status: 200 });

    const services = new IntegraServices(false, "1", false, mockFetch as any);

    await assert.rejects(
      async () => services.request("GET", "/servico/123"),
      (err: any) => {
        assert.ok(err instanceof IntegraException);
        assert.match(err.message, /Resposta Vazia/);
        return true;
      }
    );
  });

  it("deve lançar IntegraException quando resposta for JSON inválido", async () => {
    const mockFetch = async () => new Response("erro fatal não json", { status: 200 });

    const services = new IntegraServices(false, "1", false, mockFetch as any);

    await assert.rejects(
      async () => services.request("GET", "/servico/123"),
      (err: any) => {
        assert.ok(err instanceof IntegraException);
        assert.match(err.message, /Resposta JSON Inválida/);
        return true;
      }
    );
  });

  it("deve lançar IntegraException em erro HTTP (status >= 400)", async () => {
    const mockFetch = async () =>
      new Response(JSON.stringify({ error: "Não autorizado" }), {
        status: 401,
        headers: { "Content-Type": "application/json" }
      });

    const services = new IntegraServices(false, "1", false, mockFetch as any);

    await assert.rejects(
      async () => services.request("GET", "/servico/123"),
      (err: any) => {
        assert.ok(err instanceof IntegraException);
        assert.equal(err.httpCode, 401);
        assert.match(err.message, /Erro na API \(HTTP 401\)/);
        return true;
      }
    );
  });

  it("deve capturar falha de rede e lançar IntegraException", async () => {
    const mockFetch = async () => {
      throw new Error("Falha de conexão DNS");
    };

    const services = new IntegraServices(false, "1", false, mockFetch as any);

    await assert.rejects(
      async () => services.request("GET", "/servico/123"),
      (err: any) => {
        assert.ok(err instanceof IntegraException);
        assert.match(err.message, /Erro na Requisição HTTP/);
        assert.match(err.message, /Falha de conexão DNS/);
        return true;
      }
    );
  });
});
