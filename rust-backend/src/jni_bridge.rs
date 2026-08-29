use crate::native_core;
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jstring};

// Connect.

#[unsafe(no_mangle)]
pub extern "system" fn Java_org_cf0x_spicecompose_platform_SpiceNative_nativeConnect(
    mut env: JNIEnv,
    _class: JClass,
    host: JString,
    port: jint,
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

    if native_core::connect(&host, port as u16, &password) {
        1
    } else {
        0
    }
}

// Request.

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

    let json = native_core::request(&module, &function, &params_json);
    env.new_string(json).unwrap().into_raw()
}

// Touch request.

#[unsafe(no_mangle)]
pub extern "system" fn Java_org_cf0x_spicecompose_platform_SpiceNative_nativeTouchRequest(
    mut env: JNIEnv,
    _class: JClass,
    module: JString,
    function: JString,
    params_json: JString,
) -> jstring {
    let module: String = unwrap_or_return_null!(env, module);
    let function: String = unwrap_or_return_null!(env, function);
    let params_json: String = unwrap_or_return_null!(env, params_json);

    let json = native_core::touch_request(&module, &function, &params_json);
    env.new_string(json).unwrap().into_raw()
}

// Disconnect.

#[unsafe(no_mangle)]
pub extern "system" fn Java_org_cf0x_spicecompose_platform_SpiceNative_nativeDisconnect(
    _env: JNIEnv,
    _class: JClass,
) {
    native_core::disconnect();
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
