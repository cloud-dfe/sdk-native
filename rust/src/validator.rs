use crate::error::IntegraError;

pub struct KeyValidator;

impl KeyValidator {
    /// Valida e higieniza a chave de acesso do documento fiscal eletrônico (44 dígitos para DFe ou 50 dígitos para NFSe).
    pub fn check_key(chave: &str) -> Result<String, IntegraError> {
        if chave.trim().is_empty() {
            return Err(IntegraError {
                message: "A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.".to_string(),
                http_code: 0,
                response: None,
                error: None,
                json_error: None,
                debug: None,
            });
        }

        let clean_key: String = chave.chars().filter(|c| c.is_ascii_digit()).collect();
        let len = clean_key.len();

        if len != 44 && len != 50 {
            return Err(IntegraError {
                message: "A chave deve ter 44 dígitos para DFe ou 50 dígitos para NFSe.".to_string(),
                http_code: 0,
                response: None,
                error: None,
                json_error: None,
                debug: None,
            });
        }

        Ok(clean_key)
    }
}
