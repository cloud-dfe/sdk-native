package br.com.integranotas.examples;

import br.com.integranotas.sdk.IntegraClass;
import br.com.integranotas.sdk.IntegraException;

import java.util.LinkedHashMap;
import java.util.Map;

public class CancelaExample {

    public static void main(String[] args) {
        boolean isProduction = false;
        String token = "SEU_TOKEN_AQUI";
        boolean debug = true;

        try {
            IntegraClass servico = new IntegraClass(isProduction, token, debug);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("chave", "35230912345678000195550010000000011234567890");
            payload.put("justificativa", "Cancelamento por desacordo comercial");

            String resposta = servico.cancela(payload);
            System.out.println("Resposta do cancelamento:\n" + resposta);

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
