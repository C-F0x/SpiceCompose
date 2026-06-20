use crate::spice::SpiceConnection;
use crate::AppState;
use axum::{
    Router,
    extract::ws::{Message, WebSocket, WebSocketUpgrade},
    extract::State,
    http::StatusCode,
    response::IntoResponse,
    response::Json,
    routing::{get, post},
};
use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::sync::Arc;
use std::time::Duration;

// ── Request/response types ──

#[derive(Debug, Deserialize)]
pub struct ConnectRequest {
    pub host: String,
    pub port: u16,
    #[serde(default)]
    pub password: String,
}

#[derive(Debug, Serialize)]
pub struct StatusResponse {
    pub connected: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub host: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub port: Option<u16>,
}

#[derive(Debug, Deserialize)]
pub struct SpiceApiRequest {
    pub module: String,
    pub function: String,
    #[serde(default)]
    pub params: Vec<Value>,
}

// ── Routes ──

pub fn router(state: Arc<AppState>) -> Router {
    Router::new()
        .route("/status", get(status))
        .route("/connect", post(connect))
        .route("/request", post(spice_request))
        .route("/disconnect", post(disconnect))
        .route("/ws", get(ws_handler))
        .with_state(state)
}

// ── Handlers ──

async fn status(State(state): State<Arc<AppState>>) -> Json<StatusResponse> {
    let conn = state.connection.lock().await;
    Json(StatusResponse {
        connected: conn.is_some(),
        host: None,
        port: None,
    })
}

async fn connect(
    State(state): State<Arc<AppState>>,
    Json(req): Json<ConnectRequest>,
) -> Result<Json<StatusResponse>, (StatusCode, String)> {
    let mut conn_lock = state.connection.lock().await;

    if let Some(existing) = conn_lock.take() {
        existing.disconnect().await;
    }

    let connection = SpiceConnection::connect(&req.host, req.port, &req.password, Duration::from_secs(5))
        .await
        .map_err(|e| (StatusCode::BAD_GATEWAY, e))?;

    *conn_lock = Some(connection);

    Ok(Json(StatusResponse {
        connected: true,
        host: Some(req.host),
        port: Some(req.port),
    }))
}

async fn spice_request(
    State(state): State<Arc<AppState>>,
    Json(req): Json<SpiceApiRequest>,
) -> Result<Json<Value>, (StatusCode, String)> {
    let mut conn_lock = state.connection.lock().await;
    let conn = conn_lock
        .as_mut()
        .ok_or_else(|| (StatusCode::BAD_REQUEST, "Not connected".to_string()))?;

    let response = conn
        .request(&req.module, &req.function, req.params)
        .await
        .map_err(|e| (StatusCode::BAD_GATEWAY, e))?;

    Ok(Json(serde_json::to_value(response).unwrap()))
}

async fn disconnect(State(state): State<Arc<AppState>>) -> Json<StatusResponse> {
    let mut conn_lock = state.connection.lock().await;
    if let Some(existing) = conn_lock.take() {
        existing.disconnect().await;
    }
    Json(StatusResponse {
        connected: false,
        host: None,
        port: None,
    })
}

async fn ws_handler(ws: WebSocketUpgrade, State(state): State<Arc<AppState>>) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_ws(socket, state))
}

async fn handle_ws(mut socket: WebSocket, state: Arc<AppState>) {
    tracing::info!("WebSocket connected");

    while let Some(Ok(msg)) = socket.recv().await {
        let text = match &msg {
            Message::Text(t) => t.to_string(),
            Message::Close(_) => break,
            _ => continue,
        };

        // Parse the incoming message as a SPICE request.
        let spice_req: SpiceApiRequest = match serde_json::from_str(&text) {
            Ok(r) => r,
            Err(e) => {
                let err = serde_json::json!({ "error": format!("Invalid request: {e}") });
                let _ = socket.send(Message::Text(err.to_string().into())).await;
                continue;
            }
        };

        // Forward to the SPICE device through the shared connection.
        let result = {
            let mut conn_lock = state.connection.lock().await;
            match conn_lock.as_mut() {
                Some(conn) => {
                    conn.request(&spice_req.module, &spice_req.function, spice_req.params).await
                }
                None => Err("Not connected to a device".to_string()),
            }
        };

        let response = match result {
            Ok(resp) => serde_json::to_value(resp).unwrap(),
            Err(e) => serde_json::json!({ "error": e }),
        };

        let response_text = response.to_string();
        if socket.send(Message::Text(response_text.into())).await.is_err() {
            break;
        }
    }

    tracing::info!("WebSocket disconnected");
}
