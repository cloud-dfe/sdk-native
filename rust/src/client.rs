use crate::error::IntegraError;
use crate::services::IntegraServices;
use crate::validator::KeyValidator;
use std::collections::HashMap;

#[derive(Debug, Clone)]
pub struct IntegraClass {
    pub services: IntegraServices,
    pub token: String,
}

impl IntegraClass {
    /// Inicializa uma nova instância de IntegraClass.
    pub fn new(is_production: bool, token: &str, debug: bool) -> Result<Self, IntegraError> {
        let trimmed_token = token.trim();
        if trimmed_token.is_empty() {
            return Err(IntegraError {
                message: "Token é obrigatório.".to_string(),
                http_code: 0,
                response: None,
                error: None,
                json_error: None,
                debug: None,
            });
        }

        let services = IntegraServices::new(is_production, "1", debug);
        Ok(Self {
            services,
            token: trimmed_token.to_string(),
        })
    }

    fn get_headers(&self) -> HashMap<String, String> {
        let mut headers = HashMap::new();
        headers.insert("Authorization".to_string(), self.token.clone());
        headers.insert("Accept".to_string(), "application/json".to_string());
        headers
    }

    /// Valida a chave de acesso do documento fiscal eletrônico (44 dígitos para DFe ou 50 dígitos para NFSe).
    pub fn check_key(chave: &str) -> Result<String, IntegraError> {
        KeyValidator::check_key(chave)
    }

    /// Envia o payload de emissão do documento fiscal para a API.
    pub fn cria(&self, payload: &serde_json::Value) -> Result<serde_json::Value, IntegraError> {
        self.services.request(
            "POST",
            "/servico",
            Some(payload),
            Some(&self.get_headers()),
            None,
        )
    }

    /// Consulta o status de um documento fiscal a partir da chave de acesso.
    pub fn consulta(
        &self,
        chave: Option<&str>,
        payload: Option<&serde_json::Value>,
    ) -> Result<serde_json::Value, IntegraError> {
        let mut target_key = chave.unwrap_or("").trim();

        if target_key.is_empty() {
            if let Some(p) = payload {
                if let Some(k) = p.get("chave").and_then(|v| v.as_str()) {
                    target_key = k.trim();
                }
            }
        }

        let clean_key = KeyValidator::check_key(target_key)?;
        let endpoint = format!("/servico/{}", clean_key);

        self.services.request(
            "GET",
            &endpoint,
            payload,
            Some(&self.get_headers()),
            None,
        )
    }

    /// Envia uma solicitação de cancelamento de documento fiscal.
    pub fn cancela(&self, payload: &serde_json::Value) -> Result<serde_json::Value, IntegraError> {
        self.services.request(
            "POST",
            "/servico/cancela",
            Some(payload),
            Some(&self.get_headers()),
            None,
        )
    }

    /// Gera a pré-visualização do documento antes da emissão definitiva.
    pub fn preview(&self, payload: &serde_json::Value) -> Result<serde_json::Value, IntegraError> {
        self.services.request(
            "POST",
            "/servico/preview",
            Some(payload),
            Some(&self.get_headers()),
            None,
        )
    }
}
