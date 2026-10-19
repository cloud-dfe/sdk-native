use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::fmt;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DebugInfo {
    pub method: String,
    pub url: String,
    pub body: Option<serde_json::Value>,
    pub headers: HashMap<String, String>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct IntegraError {
    pub message: String,
    #[serde(default)]
    pub http_code: u16,
    #[serde(default)]
    pub response: Option<serde_json::Value>,
    #[serde(default)]
    pub error: Option<String>,
    #[serde(default)]
    pub json_error: Option<String>,
    #[serde(default)]
    pub debug: Option<DebugInfo>,
}

impl fmt::Display for IntegraError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        if let Ok(json_str) = serde_json::to_string(self) {
            write!(f, "{}", json_str)
        } else {
            write!(f, "{}", self.message)
        }
    }
}

impl std::error::Error for IntegraError {}
