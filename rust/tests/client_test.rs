use integranotas_sdk::IntegraClass;
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
fn test_construtor_token_invalido() {
    let err1 = IntegraClass::new(false, "", false).expect_err("esperava erro");
    assert!(err1.message.contains("Token é obrigatório."));

    let err2 = IntegraClass::new(false, "   ", false).expect_err("esperava erro");
    assert!(err2.message.contains("Token é obrigatório."));
}

#[test]
fn test_cria() {
    let server_url = spawn_mock_server(|req| {
        assert!(req.contains("POST /v1/servico HTTP/1.1"));
        assert!(req.contains("Authorization: MEU_TOKEN"));
        (200, "application/json", "{\"id\":123}")
    });

    let mut client = IntegraClass::new(false, "MEU_TOKEN", false).expect("sucesso");
    client.services.base_uri = server_url;

    let payload = serde_json::json!({ "natureza_operacao": "venda" });
    let resp = client.cria(&payload).expect("sucesso na emissão");

    assert_eq!(resp["id"], 123);
}

#[test]
fn test_consulta() {
    let chave = "35230912345678000195550010000000011234567890";
    let server_url = spawn_mock_server(move |req| {
        assert!(req.contains(&format!("GET /v1/servico/{} HTTP/1.1", chave)));
        assert!(req.contains("Authorization: MEU_TOKEN"));
        (200, "application/json", "{\"status\":\"autorizada\"}")
    });

    let mut client = IntegraClass::new(false, "MEU_TOKEN", false).expect("sucesso");
    client.services.base_uri = server_url;

    // Consulta direta por chave
    let resp1 = client.consulta(Some(chave), None).expect("sucesso");
    assert_eq!(resp1["status"], "autorizada");
}

#[test]
fn test_consulta_extraindo_do_payload() {
    let chave = "35230912345678000195550010000000011234567890";
    let server_url = spawn_mock_server(move |req| {
        assert!(req.contains(&format!("GET /v1/servico/{} HTTP/1.1", chave)));
        (200, "application/json", "{\"status\":\"autorizada\"}")
    });

    let mut client = IntegraClass::new(false, "MEU_TOKEN", false).expect("sucesso");
    client.services.base_uri = server_url;

    let payload = serde_json::json!({ "chave": chave });
    let resp = client.consulta(None, Some(&payload)).expect("sucesso");
    assert_eq!(resp["status"], "autorizada");
}

#[test]
fn test_consulta_chave_invalida_nao_chama_rede() {
    let mut client = IntegraClass::new(false, "MEU_TOKEN", false).expect("sucesso");
    client.services.base_uri = "http://127.0.0.1:9999/v".to_string();

    let err = client.consulta(Some("chave_invalida"), None).expect_err("esperava erro");
    assert!(err.message.contains("A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe."));
}

#[test]
fn test_cancela() {
    let server_url = spawn_mock_server(|req| {
        assert!(req.contains("POST /v1/servico/cancela HTTP/1.1"));
        assert!(req.contains("Authorization: MEU_TOKEN"));
        (200, "application/json", "{\"cancelado\":true}")
    });

    let mut client = IntegraClass::new(false, "MEU_TOKEN", false).expect("sucesso");
    client.services.base_uri = server_url;

    let payload = serde_json::json!({
        "chave": "35230912345678000195550010000000011234567890",
        "justificativa": "Cancelamento por desacordo"
    });

    let resp = client.cancela(&payload).expect("sucesso");
    assert_eq!(resp["cancelado"], true);
}

#[test]
fn test_preview() {
    let server_url = spawn_mock_server(|req| {
        assert!(req.contains("POST /v1/servico/preview HTTP/1.1"));
        assert!(req.contains("Authorization: MEU_TOKEN"));
        (200, "application/json", "{\"pdf_base64\":\"JVBERi0xLjQK...\"}")
    });

    let mut client = IntegraClass::new(false, "MEU_TOKEN", false).expect("sucesso");
    client.services.base_uri = server_url;

    let payload = serde_json::json!({ "natureza_operacao": "venda" });
    let resp = client.preview(&payload).expect("sucesso");
    assert_eq!(resp["pdf_base64"], "JVBERi0xLjQK...");
}
