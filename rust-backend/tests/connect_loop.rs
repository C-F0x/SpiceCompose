//! Integration test: simulate the spice2x API server behaviour (per
//! `controller.cpp` / `control.cpp` in spice2x.github.io) and hammer
//! `SpiceConnection::connect` + `request` in a loop to flush out
//! intermittent failures in the client protocol handling.
//!
//! Server semantics modelled after the real implementation:
//! - per-connection state: cipher initialized from the configured password
//!   (or plaintext when empty)
//! - frames are NUL-terminated JSON, encrypted with a shared RC4 stream
//! - `control/session_refresh` returns a fresh 256-char hex password in
//!   `data[0]` and swaps the server-side cipher AFTER sending the response
//! - a frame that fails to decrypt/parse gets a bare NUL reply and the
//!   connection is closed (matching `controller.cpp` parse-error handling)

use serde_json::{json, Value};
use spice_backend::rc4::RC4;
use spice_backend::spice::SpiceConnection;
use std::time::Duration;
use tokio::io::{AsyncReadExt, AsyncWriteExt};
use tokio::net::{TcpListener, TcpStream};

fn random_hex(len: usize) -> String {
    use rand::Rng;
    let mut rng = rand::thread_rng();
    (0..len)
        .map(|_| format!("{:x}", rng.gen_range(0..16)))
        .collect()
}

/// Handle one client connection exactly like `Controller::connection_handler`.
async fn handle_client(mut socket: TcpStream, configured_password: &str) {
    let mut cipher: Option<RC4> = if configured_password.is_empty() {
        None
    } else {
        Some(RC4::new(configured_password.as_bytes()))
    };

    let mut buf: Vec<u8> = Vec::new();
    let mut read_buf = vec![0u8; 8192];

    loop {
        // Extract complete frames (data in `buf` is already decrypted).
        if let Some(pos) = buf.iter().position(|&b| b == 0) {
            let frame: Vec<u8> = buf.drain(..pos).collect();
            buf.remove(0);

            if frame.is_empty() {
                continue;
            }

            // Parse; on failure reply with bare NUL and close (like upstream).
            let req: Value = match serde_json::from_slice(&frame) {
                Ok(v) => v,
                Err(_) => {
                    let _ = socket.write_all(&[0u8]).await;
                    return;
                }
            };

            let id = req["id"].as_i64().unwrap_or(0);
            let function = req["function"].as_str().unwrap_or("");
            let mut resp = json!({ "id": id, "data": [], "errors": [] });

            let mut swap_after_send = false;
            let mut new_password = String::new();

            if function == "session_refresh" {
                new_password = random_hex(256);
                resp["data"] = json!([new_password]);
                swap_after_send = true;
            }

            // Serialize + NUL-terminate, encrypt with CURRENT cipher, send.
            let mut out = serde_json::to_vec(&resp).unwrap();
            out.push(0u8);
            if let Some(c) = &mut cipher {
                c.crypt(&mut out);
            }
            if socket.write_all(&out).await.is_err() {
                return;
            }

            // Swap cipher AFTER send (matches `process_password_change`).
            if swap_after_send {
                cipher = Some(RC4::new(new_password.as_bytes()));
            }
            continue;
        }

        let n = match socket.read(&mut read_buf).await {
            Ok(0) => return, // EOF
            Ok(n) => n,
            Err(_) => return,
        };

        let mut chunk = read_buf[..n].to_vec();
        if let Some(c) = &mut cipher {
            c.crypt(&mut chunk);
        }
        buf.extend_from_slice(&chunk);
    }
}

async fn spawn_server(configured_password: &str) -> u16 {
    let listener = TcpListener::bind(("127.0.0.1", 0)).await.unwrap();
    let port = listener.local_addr().unwrap().port();
    let pw = configured_password.to_string();
    tokio::spawn(async move {
        loop {
            let (socket, _) = match listener.accept().await {
                Ok(s) => s,
                Err(_) => return,
            };
            let pw = pw.clone();
            tokio::spawn(async move {
                handle_client(socket, &pw).await;
            });
        }
    });
    port
}

#[tokio::test]
async fn connect_with_password_loop_100() {
    let port = spawn_server("testpassword").await;
    for i in 0..100 {
        let mut conn = SpiceConnection::connect("127.0.0.1", port, "testpassword", Duration::from_secs(3))
            .await
            .unwrap_or_else(|e| panic!("iter {i}: connect failed: {e}"));
        // After the handshake the session key must be the server-issued one:
        // a follow-up request must round-trip and echo a positive id.
        let resp = conn
            .request("info", "avs", vec![])
            .await
            .unwrap_or_else(|e| panic!("iter {i}: request failed: {e}"));
        assert!(resp.id > 0, "iter {i}: bad id");
        assert!(resp.errors.is_empty(), "iter {i}: unexpected errors: {:?}", resp.errors);
        conn.disconnect().await;
    }
}

#[tokio::test]
async fn connect_without_password_loop_100() {
    let port = spawn_server("").await;
    for i in 0..100 {
        let mut conn = SpiceConnection::connect("127.0.0.1", port, "", Duration::from_secs(3))
            .await
            .unwrap_or_else(|e| panic!("iter {i}: connect failed: {e}"));
        let resp = conn
            .request("info", "avs", vec![])
            .await
            .unwrap_or_else(|e| panic!("iter {i}: request failed: {e}"));
        assert!(resp.id > 0, "iter {i}: bad id");
        assert!(resp.errors.is_empty(), "iter {i}: unexpected errors: {:?}", resp.errors);
        conn.disconnect().await;
    }
}

#[tokio::test]
async fn wrong_password_fails() {
    let port = spawn_server("correct-password").await;
    // Wrong password → server cannot decrypt → bare NUL + close → connect must
    // fail within the request timeout (never hang forever).
    let result = tokio::time::timeout(
        Duration::from_secs(10),
        SpiceConnection::connect("127.0.0.1", port, "wrong", Duration::from_secs(3)),
    )
    .await
    .expect("wrong password connect must not hang forever");
    assert!(result.is_err(), "wrong password should fail, got: {:?}", result.err());
}
