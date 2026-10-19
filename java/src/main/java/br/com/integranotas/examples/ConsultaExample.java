package br.com.integranotas.examples;

import br.com.integranotas.sdk.IntegraClass;
import br.com.integranotas.sdk.IntegraException;

public class ConsultaExample {

    public static void main(String[] args) {
        boolean isProduction = false;
        String token = "SEU_TOKEN_AQUI";
        boolean debug = true;

        try {
            IntegraClass servico = new IntegraClass(isProduction, token, debug);

            // Chave de 44 dígitos (DFe/NFe/CTe) ou 50 dígitos (NFSe)
            String chave = "35230912345678000195550010000000011234567890";

            String resposta = servico.consulta(chave, null);
            System.out.println("Resposta da consulta:\n" + resposta);

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
