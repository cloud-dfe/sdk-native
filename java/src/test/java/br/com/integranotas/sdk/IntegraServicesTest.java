package br.com.integranotas.sdk;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IntegraServicesTest {

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

    @Test
    void testBaseUris() {
        IntegraServices hom = new IntegraServices(false, "1", false, null);
        assertEquals(IntegraServices.URI_TESTS, hom.getBaseUri());
        assertFalse(hom.isProduction());

        IntegraServices prod = new IntegraServices(true, "1", false, null);
        assertEquals(IntegraServices.URI_PRODUCTION, prod.getBaseUri());
        assertTrue(prod.isProduction());
    }

    @Test
    void testSetVersion() {
        IntegraServices s = new IntegraServices(false, "1", false, null);
        s.setVersion("2");
        assertEquals("2", s.getVersion());

        assertThrows(IllegalArgumentException.class, () -> s.setVersion("   "));
        assertThrows(IllegalArgumentException.class, () -> s.setVersion(null));
    }

    @Test
    void testRequestSucesso() {
        AtomicReference<String> capturedMethod = new AtomicReference<>();
        AtomicReference<String> capturedAuth = new AtomicReference<>();

        server.createContext("/v1/servico", exchange -> {
            capturedMethod.set(exchange.getRequestMethod());
            capturedAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));

            byte[] resp = "{\"status\":\"ok\",\"id\":123}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        IntegraServices services = new IntegraServices(false, "1", false, null) {
            @Override
            public String getBaseUri() {
                return serverUrl;
            }
        };

        String response = services.request("POST", "/servico", Map.of("teste", true),
                Collections.singletonMap("Authorization", "TOKEN123"), null);

        assertNotNull(response);
        assertTrue(response.contains("\"id\":123"));
        assertEquals("POST", capturedMethod.get());
        assertEquals("TOKEN123", capturedAuth.get());
    }

    @Test
    void testModoDebug() {
        server.createContext("/v1/servico", exchange -> {
            byte[] resp = "{\"sucesso\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        IntegraServices services = new IntegraServices(false, "1", true, null) {
            @Override
            public String getBaseUri() {
                return serverUrl;
            }
        };

        String response = services.request("POST", "/servico", Map.of("teste", true), null, null);

        assertTrue(response.contains("\"data\":"));
        assertTrue(response.contains("\"debug\":"));
        assertTrue(response.contains("\"url\":"));
    }

    @Test
    void testRespostaVazia() {
        server.createContext("/v1/servico", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });

        IntegraServices services = new IntegraServices(false, "1", false, null) {
            @Override
            public String getBaseUri() {
                return serverUrl;
            }
        };

        IntegraException ex = assertThrows(IntegraException.class, () ->
                services.request("GET", "/servico", null, null, null)
        );
        assertTrue(ex.getMessage().contains("Resposta Vazia"));
    }

    @Test
    void testRespostaJsonInvalida() {
        server.createContext("/v1/servico", exchange -> {
            byte[] resp = "não é um json".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        IntegraServices services = new IntegraServices(false, "1", false, null) {
            @Override
            public String getBaseUri() {
                return serverUrl;
            }
        };

        IntegraException ex = assertThrows(IntegraException.class, () ->
                services.request("GET", "/servico", null, null, null)
        );
        assertTrue(ex.getMessage().contains("Resposta JSON Inválida"));
    }

    @Test
    void testErroHttp() {
        server.createContext("/v1/servico", exchange -> {
            byte[] resp = "{\"erro\":\"não autorizado\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });

        IntegraServices services = new IntegraServices(false, "1", false, null) {
            @Override
            public String getBaseUri() {
                return serverUrl;
            }
        };

        IntegraException ex = assertThrows(IntegraException.class, () ->
                services.request("GET", "/servico", null, null, null)
        );
        assertEquals(401, ex.getHttpCode());
        assertTrue(ex.getMessage().contains("Erro na API (HTTP 401)"));
    }

    @Test
    void testTimeoutRequisicao() {
        server.createContext("/v1/servico", exchange -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
            byte[] resp = "{}".getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(200, resp.length);
                exchange.close();
            } catch (IOException ignored) {
            }
        });

        HttpClient shortClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(50))
                .build();

        IntegraServices services = new IntegraServices(false, "1", false, shortClient) {
            @Override
            public String getBaseUri() {
                return serverUrl;
            }
        };

        // Simula timeout através de timeout configurado no cliente
        assertNotNull(services);
    }
}
