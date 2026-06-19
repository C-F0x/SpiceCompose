use crate::rc4::RC4;
use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::sync::atomic::{AtomicI32, Ordering};
use std::sync::Arc;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::TcpStream;
use tokio::sync::{mpsc, Mutex};

/// SPICE request message.
#[derive(Debug, Clone, Serialize)]
pub struct SpiceRequest {
    pub id: i32,
    pub module: String,
    pub function: String,
    pub params: Vec<Value>,
}

/// SPICE response message.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SpiceResponse {
    pub id: i32,
    #[serde(default)]
    pub data: Vec<Value>,
    #[serde(default)]
    pub errors: Vec<String>,
}

static NEXT_ID: AtomicI32 = AtomicI32::new(1);

fn next_id() -> i32 {
    NEXT_ID.fetch_add(1, Ordering::Relaxed)
}

/// A connection to a SPICE-compatible device over TCP.
pub struct SpiceConnection {
    /// Write half of the TCP stream (request side).
    write: tokio::net::tcp::OwnedWriteHalf,
    /// Shared RC4 cipher — both read and write advance the same state.
    cipher: Arc<Mutex<Option<RC4>>>,
    /// Channel receiving parsed responses from the background reader.
    rx: mpsc::Receiver<Result<SpiceResponse, String>>,
    /// Held so the background reader stops when this connection is dropped.
    _tx: mpsc::Sender<Result<SpiceResponse, String>>,
}

impl SpiceConnection {
    /// Connect and start a background reader immediately.
    pub async fn connect(host: &str, port: u16, password: &str) -> Result<Self, String> {
        let stream = TcpStream::connect((host, port))
            .await
            .map_err(|e| format!("TCP connect failed: {e}"))?;

        let cipher: Option<RC4> = if password.is_empty() {
            None
        } else {
            Some(RC4::new(password.as_bytes()))
        };

        let cipher = Arc::new(Mutex::new(cipher));
        let (read, write) = stream.into_split();
        let (tx, rx) = mpsc::channel::<Result<SpiceResponse, String>>(256);

        tokio::spawn(reader_loop(read, cipher.clone(), tx.clone()));

        Ok(Self {
            write,
            cipher,
            rx,
            _tx: tx,
        })
    }

    /// Send a request and wait for the matching response.
    pub async fn request(
        &mut self,
        module: &str,
        function: &str,
        params: Vec<Value>,
    ) -> Result<SpiceResponse, String> {
        let id = next_id();
        let req = SpiceRequest {
            id,
            module: module.to_string(),
            function: function.to_string(),
            params,
        };

        let mut json = serde_json::to_string(&req).map_err(|e| format!("Serialize: {e}"))?;
        eprintln!("SPICE send: {json}");
        json.push('\0');
        let mut data = json.into_bytes();

        // Encrypt with shared cipher.
        {
            let mut guard = self.cipher.lock().await;
            if let Some(ref mut c) = *guard {
                c.crypt(&mut data);
            }
        }

        // Send.
        self.write
            .write_all(&data)
            .await
            .map_err(|e| format!("Write: {e}"))?;

        // Wait for matching response (5-second total timeout).
        let start = std::time::Instant::now();
        loop {
            if start.elapsed() > std::time::Duration::from_secs(5) {
                return Err("Request timed out".to_string());
            }

            let response = self
                .rx
                .recv()
                .await
                .ok_or_else(|| "Connection closed".to_string())??;

            if response.id == id {
                return Ok(response);
            }
            // Non-matching responses are discarded.
        }
    }

    /// Close the connection gracefully.
    pub async fn disconnect(self) {
        // Dropping self drops _tx which signals the reader to stop.
        // Dropping write sends TCP FIN.
        drop(self);
    }
}

/// Background task: continuously read, decrypt, and parse frames.
async fn reader_loop(
    mut read: tokio::net::tcp::OwnedReadHalf,
    cipher: Arc<Mutex<Option<RC4>>>,
    tx: mpsc::Sender<Result<SpiceResponse, String>>,
) {
    let mut read_buf = vec![0u8; 8192];
    let mut frame_buf: Vec<u8> = Vec::new();

    loop {
        // Extract complete frames from the buffer.
        if let Some(null_pos) = frame_buf.iter().position(|&b| b == 0) {
            let frame: Vec<u8> = frame_buf.drain(..null_pos).collect();
            frame_buf.remove(0); // skip null byte

            let result = String::from_utf8(frame)
                .map_err(|e| format!("UTF-8: {e}"))
                .and_then(|json_str| {
                    eprintln!("SPICE recv: {json_str}");
                    serde_json::from_str::<SpiceResponse>(&json_str)
                        .map_err(|e| format!("Parse: {e}"))
                });

            if tx.send(result).await.is_err() {
                return; // receiver dropped
            }
            continue;
        }

        // Read more data.
        let n = match read.read(&mut read_buf).await {
            Ok(0) => return, // EOF
            Ok(n) => n,
            Err(_) => return,
        };

        let mut chunk = read_buf[..n].to_vec();
        eprintln!("SPICE raw recv ({} bytes): {}", n, hex_fmt(&chunk));

        // Decrypt with shared cipher.
        {
            let mut guard = cipher.lock().await;
            if let Some(ref mut c) = *guard {
                c.crypt(&mut chunk);
            }
        }

        frame_buf.extend_from_slice(&chunk);
    }
}

fn hex_fmt(data: &[u8]) -> String {
    data.iter()
        .take(64)
        .map(|b| format!("{b:02x}"))
        .collect::<Vec<_>>()
        .join(" ")
        + if data.len() > 64 { "..." } else { "" }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_next_id_monotonic() {
        let a = next_id();
        let b = next_id();
        assert!(b > a);
    }
}
