package br.com.integranotas.sdk;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IntegraClassTest {

    private HttpServer server;
    private String serverUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        serverUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v";
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void testConstrutorTokenInvalido(String tokenInvalido) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new IntegraClass(false, tokenInvalido, false)
        );
        assertTrue(ex.getMessage().contains("Token é obrigatório."));
    }

    @Test
    void testConstrutorTokenNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new IntegraClass(false, null, false)
        );
        assertTrue(ex.getMessage().contains("Token é obrigatório."));
    }

    @Test
    void testCria() {
        AtomicReference<String> capturedMethod = new AtomicReference<>();
        AtomicReference<String> capturedAuth = new AtomicReference<>();

        server.createContext("/v1/servico", exchange -> {
            capturedMethod.set(exchange.getRequestMethod());
            capturedAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));

            byte[] resp = "{\"id\":123}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        IntegraClass client = new CustomUrlIntegraClass(false, "MEU_TOKEN", false, serverUrl);
        String response = client.cria(Map.of("natureza_operacao", "venda"));

        assertTrue(response.contains("\"id\":123"));
        assertEquals("POST", capturedMethod.get());
        assertEquals("MEU_TOKEN", capturedAuth.get());
    }

    @Test
    void testConsulta() {
        String chave = "35230912345678000195550010000000011234567890";

        server.createContext("/v1/servico/" + chave, exchange -> {
            byte[] resp = "{\"status\":\"autorizada\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        IntegraClass client = new CustomUrlIntegraClass(false, "MEU_TOKEN", false, serverUrl);

        // Consulta direta por chave
        String resp1 = client.consulta(chave, null);
        assertTrue(resp1.contains("\"status\":\"autorizada\""));

        // Consulta extraindo chave do payload
        String resp2 = client.consulta("", Map.of("chave", chave));
        assertTrue(resp2.contains("\"status\":\"autorizada\""));
    }

    @Test
    void testConsultaChaveInvalida() {
        AtomicBoolean redeChamada = new AtomicBoolean(false);
        server.createContext("/v1/servico/", exchange -> {
            redeChamada.set(true);
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });

        IntegraClass client = new CustomUrlIntegraClass(false, "MEU_TOKEN", false, serverUrl);

        assertThrows(IllegalArgumentException.class, () -> client.consulta("chave_invalida", null));
        assertFalse(redeChamada.get());
    }

    @Test
    void testCancela() {
        server.createContext("/v1/servico/cancela", exchange -> {
            byte[] resp = "{\"cancelado\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        IntegraClass client = new CustomUrlIntegraClass(false, "MEU_TOKEN", false, serverUrl);
        String response = client.cancela(Map.of(
                "chave", "35230912345678000195550010000000011234567890",
                "justificativa", "Cancelamento por desacordo"
        ));

        assertTrue(response.contains("\"cancelado\":true"));
    }

    @Test
    void testPreview() {
        server.createContext("/v1/servico/preview", exchange -> {
            byte[] resp = "{\"pdf_base64\":\"JVBERi0xLjQK...\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        IntegraClass client = new CustomUrlIntegraClass(false, "MEU_TOKEN", false, serverUrl);
        String response = client.preview(Map.of("natureza_operacao", "venda"));

        assertTrue(response.contains("\"pdf_base64\":\"JVBERi0xLjQK...\""));
    }

    private static class CustomUrlIntegraClass extends IntegraClass {
        public CustomUrlIntegraClass(boolean isProduction, String token, boolean debug, String customUrl) {
            super(isProduction, token, debug, null);
            // Substitui o baseUri para testes locais
            try {
                var field = IntegraServices.class.getDeclaredField("baseUri");
                field.setAccessible(true);
                field.set(getServices(), customUrl);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
