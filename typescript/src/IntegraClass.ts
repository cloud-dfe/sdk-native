import { IntegraServices } from "./IntegraServices.js";
import { KeyValidator } from "./validation/KeyValidator.js";

/**
 * Fachada principal para comunicação com os serviços da API IntegraNotas.
 * Equivalente idiomático em TypeScript para IntegraClass do PHP e C#.
 */
export class IntegraClass {
  public readonly services: IntegraServices;
  public readonly token: string;

  /**
   * Construtor da classe de serviço.
   *
   * @param isProduction Define se o ambiente é de produção (true) ou homologação (false).
   * @param token Token de acesso à API (Softhouse ou Emitente).
   * @param debug Ativa o modo de depuração para detalhar erros e requisições.
   * @param customFetch Instância customizada da função fetch (opcional para testes ou mocks).
   * @throws Error Quando o token não for informado ou for vazio.
   */
  constructor(
    isProduction: boolean = false,
    token: string = "",
    debug: boolean = false,
    customFetch?: typeof fetch
  ) {
    if (!token || token.trim() === "") {
      throw new Error("Token é obrigatório.");
    }

    this.token = token;
    this.services = new IntegraServices(isProduction, "1", debug, customFetch);
  }

  /**
   * Obtém os cabeçalhos padrão de autorização para a requisição.
   */
  private getHeaders(): Record<string, string> {
    return {
      Authorization: this.token,
      Accept: "application/json"
    };
  }

  /**
   * Valida a chave de acesso do documento fiscal eletrônico (44 dígitos para DFe ou 50 dígitos para NFSe).
   *
   * @param chave Chave a ser validada.
   * @returns Chave limpa e validada.
   */
  public static checkKey(chave: string | null | undefined): string {
    return KeyValidator.checkKey(chave);
  }

  /**
   * Envia o payload de emissão/criação do documento fiscal para a API.
   *
   * @param payload Objeto contendo os dados do documento fiscal.
   */
  public async cria<T = any>(payload: Record<string, any>): Promise<T> {
    return this.services.request<T>(
      "POST",
      "/servico",
      payload,
      this.getHeaders()
    );
  }

  /**
   * Consulta o status de um documento fiscal a partir da chave de acesso.
   *
   * @param chave Chave de acesso de 44 ou 50 dígitos (opcional se informada dentro do payload).
   * @param payload Objeto opcional contendo filtros ou a própria chave no campo 'chave'.
   */
  public async consulta<T = any>(
    chave: string = "",
    payload: Record<string, any> = {}
  ): Promise<T> {
    let targetKey = chave;
    if (!targetKey && payload && typeof payload.chave === "string") {
      targetKey = payload.chave;
    }

    const cleanKey = KeyValidator.checkKey(targetKey);

    return this.services.request<T>(
      "GET",
      `/servico/${cleanKey}`,
      payload,
      this.getHeaders()
    );
  }

  /**
   * Envia uma solicitação de cancelamento de documento fiscal.
   *
   * @param payload Objeto contendo a chave e justificativa do cancelamento.
   */
  public async cancela<T = any>(payload: Record<string, any>): Promise<T> {
    return this.services.request<T>(
      "POST",
      "/servico/cancela",
      payload,
      this.getHeaders()
    );
  }

  /**
   * Gera a pré-visualização do documento fiscal antes da emissão definitiva.
   *
   * @param payload Objeto contendo os dados do documento fiscal a ser pré-visualizado.
   */
  public async preview<T = any>(payload: Record<string, any>): Promise<T> {
    return this.services.request<T>(
      "POST",
      "/servico/preview",
      payload,
      this.getHeaders()
    );
  }
}
