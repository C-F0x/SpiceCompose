pub mod rc4;
pub mod spice;
pub mod api;
pub mod native_core;
pub mod c_bridge;
pub mod jni_bridge;

use std::sync::Arc;
use tokio::sync::RwLock;

/// Shared application state.
pub struct AppState {
    pub connection: RwLock<Option<spice::SpiceConnection>>,
    /// Dedicated connection for touch input — isolates high-frequency touch
    /// writes from queueing behind screen polling / info requests on the main
    /// connection (mirrors upstream's connection-pool concurrency model).
    pub touch_connection: RwLock<Option<spice::SpiceConnection>>,
}

impl AppState {
    pub fn new() -> Arc<Self> {
        Arc::new(Self {
            connection: RwLock::new(None),
            touch_connection: RwLock::new(None),
        })
    }
}
