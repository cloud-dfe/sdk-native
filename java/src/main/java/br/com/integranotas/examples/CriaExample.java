package br.com.integranotas.examples;

import br.com.integranotas.sdk.IntegraClass;
import br.com.integranotas.sdk.IntegraException;

import java.util.LinkedHashMap;
import java.util.Map;

public class CriaExample {

    public static void main(String[] args) {
        boolean isProduction = false; // false = Homologação / Testes; true = Produção
        String token = "SEU_TOKEN_AQUI"; // Token emitente ou softhouse
        boolean debug = true; // Se true, inclui detalhes da requisição caso ocorra erro

        try {
            IntegraClass servico = new IntegraClass(isProduction, token, debug);

            // Monte o payload da nota fiscal
            // Consulte https://integranotas.com.br/doc para os campos do payload
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("natureza_operacao", "Venda de mercadoria");
            // ... demais campos da nota

            String resposta = servico.cria(payload);
            System.out.println("Resposta da emissão:\n" + resposta);

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
