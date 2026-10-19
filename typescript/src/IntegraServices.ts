import { DebugInfo, IntegraErrorPayload, IntegraException } from "./IntegraException.js";

/**
 * Responsável pela execução de requisições HTTP para a API IntegraNotas.
 * Utiliza a API nativa fetch (Node.js 18+ / navegadores / Edge).
 */
export class IntegraServices {
  public static readonly URI_PRODUCTION = "https://api.integranotas.com.br/v";
  public static readonly URI_TESTS = "https://hom-api.integranotas.com.br/v";

  public readonly isProduction: boolean;
  public version: string;
  public readonly baseUri: string;
  public readonly debug: boolean;
  private readonly customFetch?: typeof fetch;

  constructor(
    isProduction: boolean = false,
    version: string = "1",
    debug: boolean = false,
    customFetch?: typeof fetch
  ) {
    this.isProduction = isProduction;
    this.version = version;
    this.debug = debug;
    this.baseUri = isProduction ? IntegraServices.URI_PRODUCTION : IntegraServices.URI_TESTS;
    this.customFetch = customFetch;
  }

  /**
   * Define a versão da API utilizada nas requisições.
   * @param version Versão da API (ex: "1").
   */
  public setVersion(version: string): void {
    if (!version || version.trim() === "") {
      throw new Error("A versão da API não pode ser vazia.");
    }
    this.version = version;
  }

  /**
   * Executa uma requisição HTTP para a API da IntegraNotas.
   *
   * @param method Método HTTP (GET, POST, etc.)
   * @param endpoint Caminho relativo do endpoint (ex: "/servico")
   * @param body Dados a serem enviados no corpo da requisição
   * @param headers Cabeçalhos adicionais da requisição
   * @param version Versão alternativa da API (opcional)
   * @returns Dados da resposta parseados como JSON (ou encapsulados com debug quando ativo)
   */
  public async request<T = any>(
    method: string,
    endpoint: string,
    body?: unknown,
    headers?: Record<string, string>,
    version?: string
  ): Promise<T> {
    if (version) {
      this.setVersion(version);
    }

    const url = `${this.baseUri}${this.version}${endpoint}`;
    const fetchFn = this.customFetch ?? globalThis.fetch;

    const requestHeaders: Record<string, string> = {
      Accept: "application/json",
      ...(headers ?? {})
    };

    let serializedBody: string | undefined = undefined;
    if (body !== undefined && body !== null) {
      if (typeof body === "string") {
        serializedBody = body;
      } else {
        serializedBody = JSON.stringify(body);
      }
      requestHeaders["Content-Type"] = "application/json";
    }

    const debugDetails: DebugInfo | null = this.debug
      ? {
          method,
          url,
          body: body ?? null,
          headers: requestHeaders
        }
      : null;

    let response: Response;
    let responseText: string = "";
    let httpCode = 0;

    const abortController = new AbortController();
    const timeoutId = setTimeout(() => abortController.abort(), 60000);

    try {
      response = await fetchFn(url, {
        method,
        headers: requestHeaders,
        body: serializedBody,
        signal: abortController.signal
      });

      httpCode = response.status;
      responseText = await response.text();
    } catch (err: any) {
      if (err.name === "AbortError") {
        const errorPayload: IntegraErrorPayload = {
          message: "Timeout na Requisição",
          error: "Tempo limite de conexão excedido (60s)",
          http_code: 408,
          ...(debugDetails ? { debug: debugDetails } : {})
        };
        throw new IntegraException(
          JSON.stringify(errorPayload),
          408,
          null,
          debugDetails
        );
      }

      const errorPayload: IntegraErrorPayload = {
        message: "Erro na Requisição HTTP",
        error: err?.message || String(err),
        http_code: httpCode,
        ...(debugDetails ? { debug: debugDetails } : {})
      };
      throw new IntegraException(
        JSON.stringify(errorPayload),
        httpCode,
        null,
        debugDetails
      );
    } finally {
      clearTimeout(timeoutId);
    }

    // Validação de resposta vazia
    if (!responseText || responseText.trim() === "") {
      const errorPayload: IntegraErrorPayload = {
        message: "Resposta Vazia",
        http_code: httpCode,
        ...(debugDetails ? { debug: debugDetails } : {})
      };
      throw new IntegraException(
        JSON.stringify(errorPayload),
        httpCode,
        responseText,
        debugDetails
      );
    }

    // Validação de JSON válido
    let parsedData: any;
    try {
      parsedData = JSON.parse(responseText);
    } catch (err: any) {
      const errorPayload: IntegraErrorPayload = {
        message: "Resposta JSON Inválida",
        response: responseText,
        http_code: httpCode,
        json_error: err?.message || String(err),
        ...(debugDetails ? { debug: debugDetails } : {})
      };
      throw new IntegraException(
        JSON.stringify(errorPayload),
        httpCode,
        responseText,
        debugDetails
      );
    }

    // Erros HTTP (>= 400)
    if (!response.ok) {
      const errorPayload: IntegraErrorPayload = {
        message: `Erro na API (HTTP ${httpCode})`,
        response: parsedData,
        http_code: httpCode,
        ...(debugDetails ? { debug: debugDetails } : {})
      };
      throw new IntegraException(
        JSON.stringify(errorPayload),
        httpCode,
        parsedData,
        debugDetails
      );
    }

    if (this.debug) {
      return {
        data: parsedData,
        debug: debugDetails
      } as unknown as T;
    }

    return parsedData as T;
  }
}
