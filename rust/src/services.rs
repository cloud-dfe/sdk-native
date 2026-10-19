use crate::error::{DebugInfo, IntegraError};
use std::collections::HashMap;
use std::time::Duration;

pub const URI_PRODUCTION: &str = "https://api.integranotas.com.br/v";
pub const URI_TESTS: &str = "https://hom-api.integranotas.com.br/v";

#[derive(Debug, Clone)]
pub struct IntegraServices {
    pub is_production: bool,
    pub version: String,
    pub base_uri: String,
    pub debug: bool,
}

impl IntegraServices {
    pub fn new(is_production: bool, version: &str, debug: bool) -> Self {
        let v = if version.trim().is_empty() {
            "1".to_string()
        } else {
            version.trim().to_string()
        };

        let base_uri = if is_production {
            URI_PRODUCTION.to_string()
        } else {
            URI_TESTS.to_string()
        };

        Self {
            is_production,
            version: v,
            base_uri,
            debug,
        }
    }

    pub fn set_version(&mut self, version: &str) -> Result<(), IntegraError> {
        let trimmed = version.trim();
        if trimmed.is_empty() {
            return Err(IntegraError {
                message: "A versão da API não pode ser vazia.".to_string(),
                http_code: 0,
                response: None,
                error: None,
                json_error: None,
                debug: None,
            });
        }
        self.version = trimmed.to_string();
        Ok(())
    }

    pub fn request(
        &self,
        method: &str,
        endpoint: &str,
        body: Option<&serde_json::Value>,
        headers: Option<&HashMap<String, String>>,
        version: Option<&str>,
    ) -> Result<serde_json::Value, IntegraError> {
        let mut active_version = self.version.clone();
        if let Some(v) = version {
            let trimmed = v.trim();
            if !trimmed.is_empty() {
                active_version = trimmed.to_string();
            }
        }

        let full_url = format!("{}{}{}", self.base_uri, active_version, endpoint);

        let mut req_headers = HashMap::new();
        req_headers.insert("Accept".to_string(), "application/json".to_string());
        if let Some(h) = headers {
            for (k, v) in h {
                req_headers.insert(k.clone(), v.clone());
            }
        }

        let debug_info = if self.debug {
            Some(DebugInfo {
                method: method.to_uppercase(),
                url: full_url.clone(),
                body: body.cloned(),
                headers: req_headers.clone(),
            })
        } else {
            None
        };

        let agent = ureq::AgentBuilder::new()
            .timeout_connect(Duration::from_secs(60))
            .timeout_read(Duration::from_secs(60))
            .build();

        let mut req = agent.request(&method.to_uppercase(), &full_url);
        for (k, v) in &req_headers {
            req = req.set(k, v);
        }

        let response_result = if let Some(b) = body {
            req = req.set("Content-Type", "application/json");
            req.send_json(b.clone())
        } else {
            req.call()
        };

        match response_result {
            Ok(response) => {
                let http_code = response.status();
                let text = response.into_string().unwrap_or_default();

                if text.trim().is_empty() {
                    return Err(IntegraError {
                        message: "Resposta Vazia".to_string(),
                        http_code,
                        response: None,
                        error: None,
                        json_error: None,
                        debug: debug_info,
                    });
                }

                let parsed: serde_json::Value = match serde_json::from_str(&text) {
                    Ok(val) => val,
                    Err(e) => {
                        return Err(IntegraError {
                            message: "Resposta JSON Inválida".to_string(),
                            http_code,
                            response: Some(serde_json::Value::String(text)),
                            error: None,
                            json_error: Some(e.to_string()),
                            debug: debug_info,
                        });
                    }
                };

                if self.debug {
                    Ok(serde_json::json!({
                        "data": parsed,
                        "debug": debug_info
                    }))
                } else {
                    Ok(parsed)
                }
            }
            Err(ureq::Error::Status(code, response)) => {
                let text = response.into_string().unwrap_or_default();
                let parsed_resp: serde_json::Value =
                    serde_json::from_str(&text).unwrap_or(serde_json::Value::String(text));

                Err(IntegraError {
                    message: format!("Erro na API (HTTP {})", code),
                    http_code: code,
                    response: Some(parsed_resp),
                    error: None,
                    json_error: None,
                    debug: debug_info,
                })
            }
            Err(ureq::Error::Transport(transport)) => {
                let error_str = transport.to_string();
                let is_timeout = error_str.to_lowercase().contains("timeout")
                    || transport.kind() == ureq::ErrorKind::ConnectionFailed;

                let (message, http_code) = if is_timeout {
                    ("Timeout na Requisição".to_string(), 408)
                } else {
                    ("Erro na Requisição HTTP".to_string(), 0)
                };

                Err(IntegraError {
                    message,
                    http_code,
                    response: None,
                    error: Some(error_str),
                    json_error: None,
                    debug: debug_info,
                })
            }
        }
    }
}
