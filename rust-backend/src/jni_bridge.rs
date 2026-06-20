use crate::spice::SpiceConnection;
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jstring};
use serde_json::Value;
use std::sync::{LazyLock, Mutex};
use std::time::Duration;

static CONNECTION: Mutex<Option<SpiceConnection>> = Mutex::new(None);

/// Single persistent Tokio runtime — must outlive all connections.
static RT: LazyLock<tokio::runtime::Runtime> = LazyLock::new(|| {
    tokio::runtime::Builder::new_multi_thread()
        .worker_threads(1)
        .enable_all()
        .build()
        .unwrap()
});

// ── connect ──

#[unsafe(no_mangle)]
pub extern "system" fn Java_org_cf0x_spicecompose_platform_SpiceNative_nativeConnect(
    mut env: JNIEnv,
    _class: JClass,
    host: JString,
    port: jni::sys::jint,
    password: JString,
) -> jboolean {
    let host: String = match env.get_string(&host) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    let password: String = match env.get_string(&password) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };

    match RT.block_on(SpiceConnection::connect(&host, port as u16, &password, Duration::from_secs(3))) {
        Ok(conn) => {
            let mut guard = CONNECTION.lock().unwrap();
            if let Some(old) = guard.take() {
                RT.block_on(old.disconnect());
            }
            *guard = Some(conn);
            1
        }
        Err(_) => 0,
    }
}

// ── request ──

#[unsafe(no_mangle)]
pub extern "system" fn Java_org_cf0x_spicecompose_platform_SpiceNative_nativeRequest(
    mut env: JNIEnv,
    _class: JClass,
    module: JString,
    function: JString,
    params_json: JString,
) -> jstring {
    let module: String = unwrap_or_return_null!(env, module);
    let function: String = unwrap_or_return_null!(env, function);
    let params_json: String = unwrap_or_return_null!(env, params_json);

    let params: Vec<Value> = match serde_json::from_str(&params_json) {
        Ok(v) => v,
        Err(e) => {
            let err = format!("{{\"error\":\"{e}\"}}");
            return env.new_string(err).unwrap().into_raw();
        }
    };

    let mut guard = CONNECTION.lock().unwrap();
    let conn = match guard.as_mut() {
        Some(c) => c,
        None => {
            let err = r#"{"error":"not connected"}"#;
            return env.new_string(err).unwrap().into_raw();
        }
    };

    match RT.block_on(conn.request(&module, &function, params)) {
        Ok(resp) => {
            let json = serde_json::to_string(&resp).unwrap_or_else(|_| "{}".into());
            env.new_string(json).unwrap().into_raw()
        }
        Err(e) => {
            let err = format!("{{\"error\":\"{e}\"}}");
            env.new_string(err).unwrap().into_raw()
        }
    }
}

// ── disconnect ──

#[unsafe(no_mangle)]
pub extern "system" fn Java_org_cf0x_spicecompose_platform_SpiceNative_nativeDisconnect(
    _env: JNIEnv,
    _class: JClass,
) {
    let mut guard = CONNECTION.lock().unwrap();
    if let Some(conn) = guard.take() {
        RT.block_on(conn.disconnect());
    }
}

macro_rules! unwrap_or_return_null {
    ($env:expr, $jstr:expr) => {
        match $env.get_string(&$jstr) {
            Ok(s) => s.into(),
            Err(_) => return std::ptr::null_mut(),
        }
    };
}
use unwrap_or_return_null;
