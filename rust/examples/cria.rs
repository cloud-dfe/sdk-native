use integranotas_sdk::IntegraClass;
use serde_json::json;

fn main() {
    let is_production = false; // false = Homologação / Testes; true = Produção
    let token = "SEU_TOKEN_AQUI"; // Token emitente ou softhouse
    let debug = true; // Se true, inclui detalhes da requisição caso ocorra erro

    let servico = match IntegraClass::new(is_production, token, debug) {
        Ok(client) => client,
        Err(e) => {
            eprintln!("Erro de inicialização: {}", e.message);
            return;
        }
    };

    // Monte o payload da nota fiscal
    // Consulte https://integranotas.com.br/doc para os campos do payload
    let payload = json!({
        "natureza_operacao": "Venda de mercadoria"
        // ... demais campos da nota
    });

    match servico.cria(&payload) {
        Ok(resposta) => {
            println!("Resposta da emissão:\n{}", serde_json::to_string_pretty(&resposta).unwrap());
        }
        Err(e) => {
            eprintln!("Erro na API (HTTP {}): {}", e.http_code, e.message);
            if let Some(debug_info) = e.debug {
                eprintln!("Debug Info: {:?}", debug_info);
            }
        }
    }
}
