package br.com.integranotas.sdk;

import br.com.integranotas.sdk.util.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Responsável pela execução de requisições HTTP para a API da IntegraNotas usando java.net.http.HttpClient nativo.
 */
public class IntegraServices {

    public static final String URI_PRODUCTION = "https://api.integranotas.com.br/v";
    public static final String URI_TESTS = "https://hom-api.integranotas.com.br/v";

    private final boolean isProduction;
    private String version;
    private final String baseUri;
    private final boolean debug;
    private final HttpClient httpClient;

    public IntegraServices(boolean isProduction, String version, boolean debug, HttpClient httpClient) {
        this.isProduction = isProduction;
        this.version = (version != null && !version.trim().isEmpty()) ? version.trim() : "1";
        this.debug = debug;
        this.baseUri = isProduction ? URI_PRODUCTION : URI_TESTS;

        if (httpClient != null) {
            this.httpClient = httpClient;
        } else {
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(60))
                    .build();
        }
    }

    public boolean isProduction() {
        return isProduction;
    }

    public String getVersion() {
        return version;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("A versão da API não pode ser vazia.");
        }
        this.version = version.trim();
    }

    /**
     * Executa a requisição HTTP para a API da IntegraNotas.
     */
    public String request(String method, String endpoint, Object body, Map<String, String> headers, String versionOverride) {
        if (versionOverride != null && !versionOverride.trim().isEmpty()) {
            setVersion(versionOverride);
        }

        String fullUrl = this.baseUri + this.version + endpoint;

        Map<String, String> requestHeaders = new LinkedHashMap<>();
        requestHeaders.put("Accept", "application/json");
        if (headers != null) {
            requestHeaders.putAll(headers);
        }

        HttpRequest.BodyPublisher bodyPublisher;
        String serializedBody = null;

        if (body != null) {
            requestHeaders.put("Content-Type", "application/json");
            serializedBody = (body instanceof String str && JsonUtils.isValidJson(str))
                    ? str
                    : JsonUtils.toJson(body);
            bodyPublisher = HttpRequest.BodyPublishers.ofString(serializedBody);
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        }

        String debugInfo = null;
        if (this.debug) {
            Map<String, Object> debugMap = new LinkedHashMap<>();
            debugMap.put("method", method.toUpperCase());
            debugMap.put("url", fullUrl);
            debugMap.put("body", serializedBody);
            debugMap.put("headers", requestHeaders);
            debugInfo = JsonUtils.toJson(debugMap);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(60))
                .method(method.toUpperCase(), bodyPublisher);

        for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        HttpResponse<String> response;
        try {
            response = this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("message", "Timeout na Requisição");
            err.put("error", "Tempo limite de conexão excedido (60s)");
            err.put("http_code", 408);
            if (debugInfo != null) {
                err.put("debug", debugInfo);
            }
            throw new IntegraException(JsonUtils.toJson(err), 408, null, debugInfo, e);
        } catch (IOException e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("message", "Erro na Requisição HTTP");
            err.put("error", e.getMessage() != null ? e.getMessage() : e.toString());
            err.put("http_code", 0);
            if (debugInfo != null) {
                err.put("debug", debugInfo);
            }
            throw new IntegraException(JsonUtils.toJson(err), 0, null, debugInfo, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("message", "Requisição Interrompida");
            err.put("error", e.getMessage());
            err.put("http_code", 0);
            throw new IntegraException(JsonUtils.toJson(err), 0, null, debugInfo, e);
        }

        int httpCode = response.statusCode();
        String responseBody = response.body();

        if (responseBody == null || responseBody.trim().isEmpty()) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("message", "Resposta Vazia");
            err.put("http_code", httpCode);
            if (debugInfo != null) {
                err.put("debug", debugInfo);
            }
            throw new IntegraException(JsonUtils.toJson(err), httpCode, responseBody, debugInfo);
        }

        if (!JsonUtils.isValidJson(responseBody)) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("message", "Resposta JSON Inválida");
            err.put("response", responseBody);
            err.put("http_code", httpCode);
            if (debugInfo != null) {
                err.put("debug", debugInfo);
            }
            throw new IntegraException(JsonUtils.toJson(err), httpCode, responseBody, debugInfo);
        }

        if (httpCode >= 400) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("message", "Erro na API (HTTP " + httpCode + ")");
            err.put("response", responseBody);
            err.put("http_code", httpCode);
            if (debugInfo != null) {
                err.put("debug", debugInfo);
            }
            throw new IntegraException(JsonUtils.toJson(err), httpCode, responseBody, debugInfo);
        }

        if (this.debug) {
            return "{\"data\":" + responseBody + ",\"debug\":" + debugInfo + "}";
        }

        return responseBody;
    }
}
