using System.Text.Json;
using IntegraNotas.Validation;

namespace IntegraNotas;

/// <summary>
/// Fachada principal para comunicação com os serviços da API IntegraNotas.
/// Equivalente idiomático em C# para IntegraClass do PHP.
/// </summary>
public class IntegraClass : IDisposable
{
    public IntegraServices Services { get; }
    public string Token { get; }

    /// <summary>
    /// Construtor da classe de serviço.
    /// </summary>
    /// <param name="isProduction">Define se o ambiente é de produção (true) ou homologação (false).</param>
    /// <param name="token">Token de acesso à API (Softhouse ou Emitente).</param>
    /// <param name="debug">Ativa o modo de depuração para detalhar erros e requisições.</param>
    /// <param name="httpClient">Instância customizada de HttpClient (opcional).</param>
    /// <exception cref="ArgumentException">Quando o token não for informado ou for vazio.</exception>
    public IntegraClass(
        bool isProduction = false,
        string token = "",
        bool debug = false,
        HttpClient? httpClient = null)
    {
        if (string.IsNullOrWhiteSpace(token))
        {
            throw new ArgumentException("Token é obrigatório.", nameof(token));
        }

        Token = token;
        Services = new IntegraServices(isProduction, "1", debug, httpClient);
    }

    private Dictionary<string, string> GetHeaders()
    {
        return new Dictionary<string, string>
        {
            ["Authorization"] = Token,
            ["Accept"] = "application/json"
        };
    }

    /// <summary>
    /// Valida a chave de acesso do documento fiscal eletrônico (44 dígitos para DFe ou 50 dígitos para NFSe).
    /// </summary>
    /// <param name="chave">Chave de acesso a ser validada.</param>
    /// <exception cref="ArgumentException">Quando a chave for inválida.</exception>
    public static void CheckKey(string chave)
    {
        KeyValidator.CheckKey(chave);
    }

    /// <summary>
    /// Envia o payload de emissão/criação do documento fiscal para a API.
    /// </summary>
    public async Task<string> CriaAsync(object payload, CancellationToken cancellationToken = default)
    {
        return await Services.RequestAsync(
            method: "POST",
            endpoint: "/servico",
            body: payload,
            headers: GetHeaders(),
            cancellationToken: cancellationToken).ConfigureAwait(false);
    }

    /// <summary>
    /// Versão síncrona de Cria.
    /// </summary>
    public string Cria(object payload)
    {
        return CriaAsync(payload).GetAwaiter().GetResult();
    }

    /// <summary>
    /// Consulta o status de um documento fiscal a partir da chave de acesso.
    /// </summary>
    public async Task<string> ConsultaAsync(
        string chave = "",
        object? payload = null,
        CancellationToken cancellationToken = default)
    {
        if (string.IsNullOrWhiteSpace(chave) && payload != null)
        {
            chave = ExtractChaveFromPayload(payload);
        }

        KeyValidator.CheckKey(chave);

        return await Services.RequestAsync(
            method: "GET",
            endpoint: $"/servico/{chave}",
            body: payload,
            headers: GetHeaders(),
            cancellationToken: cancellationToken).ConfigureAwait(false);
    }

    /// <summary>
    /// Versão síncrona de Consulta.
    /// </summary>
    public string Consulta(string chave = "", object? payload = null)
    {
        return ConsultaAsync(chave, payload).GetAwaiter().GetResult();
    }

    /// <summary>
    /// Envia uma solicitação de cancelamento de documento fiscal.
    /// </summary>
    public async Task<string> CancelaAsync(object payload, CancellationToken cancellationToken = default)
    {
        return await Services.RequestAsync(
            method: "POST",
            endpoint: "/servico/cancela",
            body: payload,
            headers: GetHeaders(),
            cancellationToken: cancellationToken).ConfigureAwait(false);
    }

    /// <summary>
    /// Versão síncrona de Cancela.
    /// </summary>
    public string Cancela(object payload)
    {
        return CancelaAsync(payload).GetAwaiter().GetResult();
    }

    /// <summary>
    /// Gera a pré-visualização do documento fiscal antes da emissão definitiva.
    /// </summary>
    public async Task<string> PreviewAsync(object payload, CancellationToken cancellationToken = default)
    {
        return await Services.RequestAsync(
            method: "POST",
            endpoint: "/servico/preview",
            body: payload,
            headers: GetHeaders(),
            cancellationToken: cancellationToken).ConfigureAwait(false);
    }

    /// <summary>
    /// Versão síncrona de Preview.
    /// </summary>
    public string Preview(object payload)
    {
        return PreviewAsync(payload).GetAwaiter().GetResult();
    }

    private static string ExtractChaveFromPayload(object payload)
    {
        if (payload is IDictionary<string, object> dict && dict.TryGetValue("chave", out var valDict) && valDict != null)
        {
            return valDict.ToString() ?? string.Empty;
        }

        if (payload is IDictionary<string, string> strDict && strDict.TryGetValue("chave", out var valStrDict) && valStrDict != null)
        {
            return valStrDict;
        }

        try
        {
            string json = payload is string str ? str : JsonSerializer.Serialize(payload);
            using var doc = JsonDocument.Parse(json);
            if (doc.RootElement.TryGetProperty("chave", out var prop))
            {
                return prop.GetString() ?? string.Empty;
            }
        }
        catch
        {
            // Fallback se não for JSON válido
        }

        return string.Empty;
    }

    public void Dispose()
    {
        Services.Dispose();
    }
}
