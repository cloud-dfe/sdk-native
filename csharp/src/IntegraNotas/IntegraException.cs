using System.Text.Encodings.Web;
using System.Text.Json;

namespace IntegraNotas;

/// <summary>
/// Exceção lançada em caso de erros na comunicação ou resposta da API IntegraNotas.
/// </summary>
public class IntegraException : Exception
{
    /// <summary>
    /// Código de status HTTP retornado pela requisição.
    /// </summary>
    public int HttpCode { get; }

    /// <summary>
    /// Conteúdo bruto da resposta retornada pela API.
    /// </summary>
    public string? ResponseBody { get; }

    /// <summary>
    /// Dados contextuais de depuração quando o modo debug estiver ativo.
    /// </summary>
    public object? DebugInfo { get; }

    public IntegraException(string message, int httpCode = 0, string? responseBody = null, object? debugInfo = null, Exception? innerException = null)
        : base(message, innerException)
    {
        HttpCode = httpCode;
        ResponseBody = responseBody;
        DebugInfo = debugInfo;
    }

    /// <summary>
    /// Retorna a representação do erro em formato JSON estruturado, compatível com a biblioteca PHP.
    /// </summary>
    public string ToJson()
    {
        var errorData = new Dictionary<string, object?>
        {
            ["message"] = Message,
            ["http_code"] = HttpCode
        };

        if (!string.IsNullOrEmpty(ResponseBody))
        {
            errorData["response"] = ResponseBody;
        }

        if (DebugInfo != null)
        {
            errorData["debug"] = DebugInfo;
        }

        return JsonSerializer.Serialize(errorData, new JsonSerializerOptions
        {
            WriteIndented = true,
            Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping
        });
    }
}
