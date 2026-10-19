use integranotas_sdk::IntegraClass;

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

    // Chave de 44 dígitos (DFe/NFe/CTe) ou 50 dígitos (NFSe)
    let chave = "35230912345678000195550010000000011234567890";

    match servico.consulta(Some(chave), None) {
        Ok(resposta) => {
            println!("Resposta da consulta:\n{}", serde_json::to_string_pretty(&resposta).unwrap());
        }
        Err(e) => {
            eprintln!("Erro na API (HTTP {}): {}", e.http_code, e.message);
            if let Some(debug_info) = e.debug {
                eprintln!("Debug Info: {:?}", debug_info);
            }
        }
    }
}
