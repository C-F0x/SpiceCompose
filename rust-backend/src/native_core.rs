//! Shared connection core for the JNI and C ABI bridges.

use crate::spice::SpiceConnection;
use serde_json::Value;
use std::sync::LazyLock;
use std::time::Duration;
use tokio::sync::Mutex as AsyncMutex;

static CONNECTION: LazyLock<AsyncMutex<Option<SpiceConnection>>> =
    LazyLock::new(|| AsyncMutex::new(None));

/// Dedicated touch connection.
static TOUCH_CONNECTION: LazyLock<AsyncMutex<Option<SpiceConnection>>> =
    LazyLock::new(|| AsyncMutex::new(None));

/// Last connect error, or empty after a successful connect.
static LAST_ERROR: LazyLock<AsyncMutex<String>> = LazyLock::new(|| AsyncMutex::new(String::new()));

/// Persistent Tokio runtime for all connections.
static RT: LazyLock<tokio::runtime::Runtime> = LazyLock::new(|| {
    tokio::runtime::Builder::new_multi_thread()
        .worker_threads(1)
        .enable_all()
        .build()
        .unwrap()
});

/// Open the main and dedicated touch connections.
pub fn connect(host: &str, port: u16, password: &str) -> bool {
    match RT.block_on(SpiceConnection::connect(
        host,
        port,
        password,
        Duration::from_secs(3),
    )) {
        Ok(conn) => {
            *RT.block_on(LAST_ERROR.lock()) = String::new();
            let mut guard = RT.block_on(CONNECTION.lock());
            if let Some(old) = guard.take() {
                RT.block_on(old.disconnect());
            }
            *guard = Some(conn);

            // Use a separate touch connection to avoid blocking screen polls.
            // Touch setup is best effort; fall back to the main connection.
            match RT.block_on(SpiceConnection::connect(
                host,
                port,
                password,
                Duration::from_secs(3),
            )) {
                Ok(touch_conn) => {
                    let mut tguard = RT.block_on(TOUCH_CONNECTION.lock());
                    if let Some(old) = tguard.take() {
                        RT.block_on(old.disconnect());
                    }
                    *tguard = Some(touch_conn);
                }
                Err(e) => {
                    // Keep the main connection if touch setup fails.
                    eprintln!("[SpiceCompose] touch connection failed: {e}");
                }
            }
            true
        }
        Err(e) => {
            // Keep the native reason for diagnostics.
            eprintln!("[SpiceCompose] nativeConnect failed: {e}");
            *RT.block_on(LAST_ERROR.lock()) = e;
            false
        }
    }
}

/// Return the last connect error, or an empty string.
pub fn last_error() -> String {
    RT.block_on(LAST_ERROR.lock()).clone()
}

/// Send a request on the main connection.
pub fn request(module: &str, function: &str, params_json: &str) -> String {
    let params: Vec<Value> = match serde_json::from_str(params_json) {
        Ok(v) => v,
        Err(e) => return format!("{{\"error\":\"{e}\"}}"),
    };

    let mut guard = RT.block_on(CONNECTION.lock());
    let conn = match guard.as_mut() {
        Some(c) => c,
        None => return r#"{"error":"not connected"}"#.to_string(),
    };

    match RT.block_on(conn.request(module, function, params)) {
        Ok(resp) => serde_json::to_string(&resp).unwrap_or_else(|_| "{}".into()),
        Err(e) => format!("{{\"error\":\"{e}\"}}"),
    }
}

/// Send a request on the touch connection, falling back to main.
pub fn touch_request(module: &str, function: &str, params_json: &str) -> String {
    let params: Vec<Value> = match serde_json::from_str(params_json) {
        Ok(v) => v,
        Err(e) => return format!("{{\"error\":\"{e}\"}}"),
    };

    let mut guard = RT.block_on(TOUCH_CONNECTION.lock());
    if guard.is_none() {
        // Fall back when the touch connection is unavailable.
        drop(guard);
        let mut main_guard = RT.block_on(CONNECTION.lock());
        let conn = match main_guard.as_mut() {
            Some(c) => c,
            None => return r#"{"error":"not connected"}"#.to_string(),
        };
        return match RT.block_on(conn.request(module, function, params)) {
            Ok(resp) => serde_json::to_string(&resp).unwrap_or_else(|_| "{}".into()),
            Err(e) => format!("{{\"error\":\"{e}\"}}"),
        };
    }

    let conn = guard.as_mut().unwrap();
    match RT.block_on(conn.request(module, function, params)) {
        Ok(resp) => serde_json::to_string(&resp).unwrap_or_else(|_| "{}".into()),
        Err(e) => format!("{{\"error\":\"{e}\"}}"),
    }
}

/// Close both connections.
pub fn disconnect() {
    let mut guard = RT.block_on(CONNECTION.lock());
    if let Some(conn) = guard.take() {
        RT.block_on(conn.disconnect());
    }
    let mut tguard = RT.block_on(TOUCH_CONNECTION.lock());
    if let Some(conn) = tguard.take() {
        RT.block_on(conn.disconnect());
    }
}
