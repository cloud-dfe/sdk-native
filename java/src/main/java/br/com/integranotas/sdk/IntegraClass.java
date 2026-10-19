package br.com.integranotas.sdk;

import br.com.integranotas.sdk.util.JsonUtils;
import br.com.integranotas.sdk.validation.KeyValidator;

import java.net.http.HttpClient;
import java.util.Collections;
import java.util.Map;

/**
 * Fachada principal para comunicação com a API da IntegraNotas em Java.
 */
public class IntegraClass {

    private final IntegraServices services;
    private final String token;

    public IntegraClass(boolean isProduction, String token, boolean debug) {
        this(isProduction, token, debug, null);
    }

    public IntegraClass(boolean isProduction, String token, boolean debug, HttpClient httpClient) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token é obrigatório.");
        }
        this.token = token.trim();
        this.services = new IntegraServices(isProduction, "1", debug, httpClient);
    }

    public IntegraServices getServices() {
        return services;
    }

    public String getToken() {
        return token;
    }

    private Map<String, String> getHeaders() {
        return Collections.singletonMap("Authorization", this.token);
    }

    /**
     * Valida a chave de acesso do documento fiscal eletrônico (44 dígitos para DFe ou 50 dígitos para NFSe).
     */
    public static String checkKey(String chave) {
        return KeyValidator.checkKey(chave);
    }

    /**
     * Envia o payload de emissão do documento fiscal para a API.
     */
    public String cria(Object payload) {
        return this.services.request("POST", "/servico", payload, getHeaders(), null);
    }

    /**
     * Consulta o status de um documento fiscal a partir da chave de acesso.
     */
    public String consulta(String chave, Object payload) {
        String targetKey = chave;
        if ((targetKey == null || targetKey.trim().isEmpty()) && payload != null) {
            targetKey = JsonUtils.extractChave(payload);
        }

        String cleanKey = KeyValidator.checkKey(targetKey);
        return this.services.request("GET", "/servico/" + cleanKey, payload, getHeaders(), null);
    }

    /**
     * Envia uma solicitação de cancelamento de documento fiscal.
     */
    public String cancela(Object payload) {
        return this.services.request("POST", "/servico/cancela", payload, getHeaders(), null);
    }

    /**
     * Gera a pré-visualização do documento antes da emissão definitiva.
     */
    public String preview(Object payload) {
        return this.services.request("POST", "/servico/preview", payload, getHeaders(), null);
    }
}
