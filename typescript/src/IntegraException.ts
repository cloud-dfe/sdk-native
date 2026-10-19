export interface DebugInfo {
  method: string;
  url: string;
  body?: unknown;
  headers?: Record<string, string>;
}

export interface IntegraErrorPayload {
  message: string;
  error?: string;
  response?: unknown;
  http_code: number;
  json_error?: string;
  debug?: DebugInfo;
}

/**
 * Exceção personalizada para erros originados na comunicação com a API da IntegraNotas.
 */
export class IntegraException extends Error {
  public readonly httpCode: number;
  public readonly responseBody: unknown;
  public readonly debugInfo?: DebugInfo | null;

  constructor(
    message: string,
    httpCode: number = 0,
    responseBody: unknown = null,
    debugInfo: DebugInfo | null = null
  ) {
    super(message);
    this.name = "IntegraException";
    this.httpCode = httpCode;
    this.responseBody = responseBody;
    this.debugInfo = debugInfo;

    // Garante compatibilidade correta do protótipo em TypeScript/ES5+
    Object.setPrototypeOf(this, IntegraException.prototype);
  }
}
