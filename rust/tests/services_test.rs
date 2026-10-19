use integranotas_sdk::services::{IntegraServices, URI_PRODUCTION, URI_TESTS};
use std::io::{Read, Write};
use std::net::TcpListener;
use std::thread;

fn spawn_mock_server<F>(handler: F) -> String
where
    F: FnOnce(&str) -> (u16, &str, &str) + Send + 'static,
{
    let listener = TcpListener::bind("127.0.0.1:0").expect("falha ao criar TcpListener");
    let port = listener.local_addr().expect("falha ao obter porta").port();

    thread::spawn(move || {
        if let Ok((mut stream, _)) = listener.accept() {
            let mut buf = [0u8; 4096];
            let n = stream.read(&mut buf).unwrap_or(0);
            let req_str = String::from_utf8_lossy(&buf[..n]);

            let (status, content_type, body) = handler(&req_str);
            let resp = format!(
                "HTTP/1.1 {} OK\r\nContent-Type: {}\r\nContent-Length: {}\r\nConnection: close\r\n\r\n{}",
                status,
                content_type,
                body.len(),
                body
            );
            let _ = stream.write_all(resp.as_bytes());
            let _ = stream.flush();
        }
    });

    format!("http://127.0.0.1:{}/v", port)
}

#[test]
fn test_base_uris() {
    let hom = IntegraServices::new(false, "1", false);
    assert_eq!(hom.base_uri, URI_TESTS);
    assert!(!hom.is_production);

    let prod = IntegraServices::new(true, "1", false);
    assert_eq!(prod.base_uri, URI_PRODUCTION);
    assert!(prod.is_production);
}

#[test]
fn test_set_version() {
    let mut s = IntegraServices::new(false, "1", false);
    assert!(s.set_version("2").is_ok());
    assert_eq!(s.version, "2");

    assert!(s.set_version("   ").is_err());
    assert!(s.set_version("").is_err());
}

#[test]
fn test_request_sucesso() {
    let server_url = spawn_mock_server(|req| {
        assert!(req.contains("POST /v1/servico HTTP/1.1"));
        assert!(req.contains("Authorization: TOKEN123"));
        assert!(req.contains("Content-Type: application/json"));
        assert!(req.contains("\"teste\":true"));
        (200, "application/json", "{\"status\":\"ok\",\"id\":123}")
    });

    let mut services = IntegraServices::new(false, "1", false);
    services.base_uri = server_url;

    let payload = serde_json::json!({ "teste": true });
    let mut headers = std::collections::HashMap::new();
    headers.insert("Authorization".to_string(), "TOKEN123".to_string());

    let resp = services
        .request("POST", "/servico", Some(&payload), Some(&headers), None)
        .expect("esperava sucesso");

    assert_eq!(resp["status"], "ok");
    assert_eq!(resp["id"], 123);
}

#[test]
fn test_modo_debug() {
    let server_url = spawn_mock_server(|_| (200, "application/json", "{\"sucesso\":true}"));

    let mut services = IntegraServices::new(false, "1", true);
    services.base_uri = server_url;

    let resp = services
        .request("POST", "/servico", None, None, None)
        .expect("esperava sucesso no modo debug");

    assert!(resp.get("data").is_some());
    assert!(resp.get("debug").is_some());
    assert_eq!(resp["data"]["sucesso"], true);
}

#[test]
fn test_resposta_vazia() {
    let server_url = spawn_mock_server(|_| (200, "application/json", "   "));

    let mut services = IntegraServices::new(false, "1", false);
    services.base_uri = server_url;

    let err = services
        .request("GET", "/servico", None, None, None)
        .expect_err("esperava erro");

    assert_eq!(err.message, "Resposta Vazia");
}

#[test]
fn test_resposta_json_invalida() {
    let server_url = spawn_mock_server(|_| (200, "text/plain", "não é json"));

    let mut services = IntegraServices::new(false, "1", false);
    services.base_uri = server_url;

    let err = services
        .request("GET", "/servico", None, None, None)
        .expect_err("esperava erro");

    assert_eq!(err.message, "Resposta JSON Inválida");
}

#[test]
fn test_erro_http() {
    let server_url = spawn_mock_server(|_| (401, "application/json", "{\"erro\":\"não autorizado\"}"));

    let mut services = IntegraServices::new(false, "1", false);
    services.base_uri = server_url;

    let err = services
        .request("GET", "/servico", None, None, None)
        .expect_err("esperava erro");

    assert_eq!(err.http_code, 401);
    assert!(err.message.contains("Erro na API (HTTP 401)"));
}
