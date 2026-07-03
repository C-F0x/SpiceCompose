pub mod rc4;
pub mod spice;
pub mod api;
pub mod jni_bridge;

use std::sync::Arc;
use tokio::sync::RwLock;

/// Shared application state.
pub struct AppState {
    pub connection: RwLock<Option<spice::SpiceConnection>>,
}

impl AppState {
    pub fn new() -> Arc<Self> {
        Arc::new(Self {
            connection: RwLock::new(None),
        })
    }
}
