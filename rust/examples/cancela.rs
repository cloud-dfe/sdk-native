use integranotas_sdk::IntegraClass;
use serde_json::json;

fn main() {
    let is_production = false;
    let token = "SEU_TOKEN_AQUI";
    let debug = true;

    let servico = match IntegraClass::new(is_production, token, debug) {
        Ok(client) => client,
        Err(e) => {
            eprintln!("Erro de inicialização: {}", e.message);
            return;
        }
    };

    let payload = json!({
        "chave": "35230912345678000195550010000000011234567890",
        "justificativa": "Cancelamento por desacordo comercial"
    });

    match servico.cancela(&payload) {
        Ok(resposta) => {
            println!("Resposta do cancelamento:\n{}", serde_json::to_string_pretty(&resposta).unwrap());
        }
        Err(e) => {
            eprintln!("Erro na API (HTTP {}): {}", e.http_code, e.message);
            if let Some(debug_info) = e.debug {
                eprintln!("Debug Info: {:?}", debug_info);
            }
        }
    }
}
