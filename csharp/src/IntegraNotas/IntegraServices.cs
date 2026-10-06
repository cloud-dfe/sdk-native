using System.Net.Http.Headers;
using System.Text;
using System.Text.Encodings.Web;
using System.Text.Json;

namespace IntegraNotas;

/// <summary>
/// Responsável pela execução de requisições HTTP para a API IntegraNotas.
/// </summary>
public class IntegraServices : IDisposable
{
    public const string UriProduction = "https://api.integranotas.com.br/v";
    public const string UriTests = "https://hom-api.integranotas.com.br/v";

    private readonly HttpClient _httpClient;
    private readonly bool _ownsHttpClient;
    private bool _disposed;

    public bool IsProduction { get; }
    public string Version { get; private set; }
    public string BaseUri { get; }
    public bool Debug { get; }

    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
        WriteIndented = false,
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping
    };

    public IntegraServices(
        bool isProduction = false,
        string version = "1",
        bool debug = false,
        HttpClient? httpClient = null)
    {
        IsProduction = isProduction;
        Version = version;
        Debug = debug;
        BaseUri = isProduction ? UriProduction : UriTests;

        if (httpClient != null)
        {
            _httpClient = httpClient;
            _ownsHttpClient = false;
        }
        else
        {
            var handler = new SocketsHttpHandler
            {
                ConnectTimeout = TimeSpan.FromSeconds(60),
                PooledConnectionLifetime = TimeSpan.FromMinutes(15)
            };

            // Em homologação/testes, permite certificados autoassinados/desenvolvimento caso necessário
            if (!isProduction)
            {
                handler.SslOptions.RemoteCertificateValidationCallback = (_, _, _, _) => true;
            }

            _httpClient = new HttpClient(handler)
            {
                Timeout = TimeSpan.FromSeconds(60)
            };
            _ownsHttpClient = true;
        }
    }

    /// <summary>
    /// Define a versão da API utilizada nas requisições.
    /// </summary>
    public void SetVersion(string version)
    {
        if (string.IsNullOrWhiteSpace(version))
        {
            throw new ArgumentException("A versão da API não pode ser vazia.", nameof(version));
        }
        Version = version;
    }

    /// <summary>
    /// Executa uma requisição HTTP assíncrona para a API.
    /// </summary>
    public async Task<string> RequestAsync(
        string method,
        string endpoint,
        object? body = null,
        IDictionary<string, string>? headers = null,
        string? version = null,
        CancellationToken cancellationToken = default)
    {
        if (!string.IsNullOrEmpty(version))
        {
            SetVersion(version);
        }

        string fullUrl = $"{BaseUri}{Version}{endpoint}";
        using var request = new HttpRequestMessage(new HttpMethod(method), fullUrl);

        // Headers padrão
        request.Headers.Accept.Clear();
        request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

        if (headers != null)
        {
            foreach (var kvp in headers)
            {
                if (kvp.Key.Equals("Content-Type", StringComparison.OrdinalIgnoreCase))
                {
                    continue; // Definido no StringContent
                }
                request.Headers.TryAddWithoutValidation(kvp.Key, kvp.Value);
            }
        }

        string? jsonBody = null;
        if (body != null)
        {
            jsonBody = body is string strBody ? strBody : JsonSerializer.Serialize(body, JsonOptions);
            request.Content = new StringContent(jsonBody, Encoding.UTF8, "application/json");
        }

        HttpResponseMessage response;
        string responseContent;
        int httpCode = 0;

        try
        {
            response = await _httpClient.SendAsync(request, cancellationToken).ConfigureAwait(false);
            httpCode = (int)response.StatusCode;
            responseContent = await response.Content.ReadAsStringAsync(cancellationToken).ConfigureAwait(false);
        }
        catch (HttpRequestException ex)
        {
            var debugDetails = Debug ? BuildDebugData(method, fullUrl, body, headers) : null;
            var errorPayload = new Dictionary<string, object?>
            {
                ["message"] = "Erro na Requisição HTTP",
                ["error"] = ex.Message,
                ["http_code"] = httpCode
            };
            if (debugDetails != null)
            {
                errorPayload["debug"] = debugDetails;
            }

            throw new IntegraException(
                message: JsonSerializer.Serialize(errorPayload, JsonOptions),
                httpCode: httpCode,
                responseBody: null,
                debugInfo: debugDetails,
                innerException: ex);
        }
        catch (TaskCanceledException ex) when (!cancellationToken.IsCancellationRequested)
        {
            var debugDetails = Debug ? BuildDebugData(method, fullUrl, body, headers) : null;
            var errorPayload = new Dictionary<string, object?>
            {
                ["message"] = "Timeout na Requisição",
                ["error"] = "Tempo limite de conexão excedido (60s)",
                ["http_code"] = 408
            };
            if (debugDetails != null)
            {
                errorPayload["debug"] = debugDetails;
            }

            throw new IntegraException(
                message: JsonSerializer.Serialize(errorPayload, JsonOptions),
                httpCode: 408,
                responseBody: null,
                debugInfo: debugDetails,
                innerException: ex);
        }

        // Validação da resposta
        if (string.IsNullOrWhiteSpace(responseContent))
        {
            var debugDetails = Debug ? BuildDebugData(method, fullUrl, body, headers) : null;
            var errorPayload = new Dictionary<string, object?>
            {
                ["message"] = "Resposta Vazia",
                ["http_code"] = httpCode
            };
            if (debugDetails != null)
            {
                errorPayload["debug"] = debugDetails;
            }

            throw new IntegraException(
                message: JsonSerializer.Serialize(errorPayload, JsonOptions),
                httpCode: httpCode,
                responseBody: responseContent,
                debugInfo: debugDetails);
        }

        // Validação de JSON válido
        try
        {
            using var doc = JsonDocument.Parse(responseContent);
        }
        catch (JsonException ex)
        {
            var debugDetails = Debug ? BuildDebugData(method, fullUrl, body, headers) : null;
            var errorPayload = new Dictionary<string, object?>
            {
                ["message"] = "Resposta JSON Inválida",
                ["response"] = responseContent,
                ["http_code"] = httpCode,
                ["json_error"] = ex.Message
            };
            if (debugDetails != null)
            {
                errorPayload["debug"] = debugDetails;
            }

            throw new IntegraException(
                message: JsonSerializer.Serialize(errorPayload, JsonOptions),
                httpCode: httpCode,
                responseBody: responseContent,
                debugInfo: debugDetails,
                innerException: ex);
        }

        // Se a resposta retornou erro HTTP (>= 400), lança exceção com formato estruturado
        if (!response.IsSuccessStatusCode)
        {
            var debugDetails = Debug ? BuildDebugData(method, fullUrl, body, headers) : null;
            var errorPayload = new Dictionary<string, object?>
            {
                ["message"] = $"Erro na API (HTTP {httpCode})",
                ["response"] = responseContent,
                ["http_code"] = httpCode
            };
            if (debugDetails != null)
            {
                errorPayload["debug"] = debugDetails;
            }

            throw new IntegraException(
                message: JsonSerializer.Serialize(errorPayload, JsonOptions),
                httpCode: httpCode,
                responseBody: responseContent,
                debugInfo: debugDetails);
        }

        if (Debug)
        {
            using var parsedDoc = JsonDocument.Parse(responseContent);
            var debugWrapper = new Dictionary<string, object?>
            {
                ["data"] = parsedDoc.RootElement.Clone(),
                ["debug"] = BuildDebugData(method, fullUrl, body, headers)
            };
            return JsonSerializer.Serialize(debugWrapper, new JsonSerializerOptions
            {
                WriteIndented = true,
                Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping
            });
        }

        return responseContent;
    }

    /// <summary>
    /// Executa uma requisição de forma síncrona (wrapper para RequestAsync).
    /// </summary>
    public string Request(
        string method,
        string endpoint,
        object? body = null,
        IDictionary<string, string>? headers = null,
        string? version = null)
    {
        return RequestAsync(method, endpoint, body, headers, version).GetAwaiter().GetResult();
    }

    private static Dictionary<string, object?> BuildDebugData(
        string method,
        string url,
        object? body,
        IDictionary<string, string>? headers)
    {
        return new Dictionary<string, object?>
        {
            ["method"] = method,
            ["url"] = url,
            ["body"] = body,
            ["headers"] = headers
        };
    }

    public void Dispose()
    {
        if (!_disposed)
        {
            if (_ownsHttpClient)
            {
                _httpClient.Dispose();
            }
            _disposed = true;
        }
    }
}
