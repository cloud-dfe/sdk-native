package br.com.integranotas.sdk;

/**
 * Exceção personalizada para erros originados na comunicação com a API da IntegraNotas.
 */
public class IntegraException extends RuntimeException {

    private final int httpCode;
    private final String responseBody;
    private final String debugInfo;

    public IntegraException(String message) {
        this(message, 0, null, null, null);
    }

    public IntegraException(String message, int httpCode, String responseBody, String debugInfo) {
        this(message, httpCode, responseBody, debugInfo, null);
    }

    public IntegraException(String message, int httpCode, String responseBody, String debugInfo, Throwable cause) {
        super(message, cause);
        this.httpCode = httpCode;
        this.responseBody = responseBody;
        this.debugInfo = debugInfo;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getDebugInfo() {
        return debugInfo;
    }
}
