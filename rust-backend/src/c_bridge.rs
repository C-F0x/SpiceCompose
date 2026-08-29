//! C ABI bridge for iOS Kotlin/Native cinterop.
//!
//! Returned strings are caller-owned and must be freed with
//! `spice_native_free_string`.

use crate::native_core;
use std::ffi::{CStr, CString, c_char};

fn cstr_to_string(ptr: *const c_char) -> String {
    if ptr.is_null() {
        return String::new();
    }
    unsafe { CStr::from_ptr(ptr) }
        .to_string_lossy()
        .into_owned()
}

/// Open the main and touch connections.
#[unsafe(no_mangle)]
pub extern "C" fn spice_native_connect(
    host: *const c_char,
    port: i32,
    password: *const c_char,
) -> bool {
    native_core::connect(
        &cstr_to_string(host),
        port as u16,
        &cstr_to_string(password),
    )
}

/// Request on the main connection. The caller owns the returned JSON string.
#[unsafe(no_mangle)]
pub extern "C" fn spice_native_request(
    module: *const c_char,
    function: *const c_char,
    params_json: *const c_char,
) -> *mut c_char {
    let out = native_core::request(
        &cstr_to_string(module),
        &cstr_to_string(function),
        &cstr_to_string(params_json),
    );
    match CString::new(out) {
        Ok(c) => c.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Request on the touch connection, falling back to main.
#[unsafe(no_mangle)]
pub extern "C" fn spice_native_touch_request(
    module: *const c_char,
    function: *const c_char,
    params_json: *const c_char,
) -> *mut c_char {
    let out = native_core::touch_request(
        &cstr_to_string(module),
        &cstr_to_string(function),
        &cstr_to_string(params_json),
    );
    match CString::new(out) {
        Ok(c) => c.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Close both connections.
#[unsafe(no_mangle)]
pub extern "C" fn spice_native_disconnect() {
    native_core::disconnect();
}

/// Return the last connect error. The caller owns the returned string.
#[unsafe(no_mangle)]
pub extern "C" fn spice_native_last_error() -> *mut c_char {
    let out = native_core::last_error();
    match CString::new(out) {
        Ok(c) => c.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

/// Free a string returned by a request function.
#[unsafe(no_mangle)]
pub extern "C" fn spice_native_free_string(s: *mut c_char) {
    if !s.is_null() {
        unsafe {
            drop(CString::from_raw(s));
        }
    }
}
