package br.com.integranotas.examples;

import br.com.integranotas.sdk.IntegraClass;
import br.com.integranotas.sdk.IntegraException;

import java.util.LinkedHashMap;
import java.util.Map;

public class PreviewExample {

    public static void main(String[] args) {
        boolean isProduction = false;
        String token = "SEU_TOKEN_AQUI";
        boolean debug = true;

        try {
            IntegraClass servico = new IntegraClass(isProduction, token, debug);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("natureza_operacao", "Venda de mercadoria");
            // ... demais campos

            String resposta = servico.preview(payload);
            System.out.println("Resposta da pré-visualização:\n" + resposta);

        } catch (IntegraException e) {
            System.err.printf("Erro na API (HTTP %d): %s%n", e.getHttpCode(), e.getMessage());
            if (e.getDebugInfo() != null) {
                System.err.println("Debug: " + e.getDebugInfo());
            }
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
        }
    }
}
