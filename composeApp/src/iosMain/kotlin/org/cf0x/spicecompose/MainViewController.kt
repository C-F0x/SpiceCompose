package org.cf0x.spicecompose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.ComposeUIViewController
import org.cf0x.spicecompose.ui.dispatchIOSBack as dispatchBackEvent
import platform.UIKit.UIViewController

/**
 * Entry point consumed by the iosApp Xcode project
 * (via embedAndSignAppleFrameworkForXcode).
 */
@OptIn(ExperimentalMaterial3Api::class)
fun MainViewController(): UIViewController {
    // iOS 14+: proactively request local-network permission at launch so the
    // system dialog appears before the user tries to connect to spice2x.
    requestLocalNetworkPermission()
    return ComposeUIViewController { App() }
}

/** Entry point for the native iOS edge-swipe back gesture. */
fun dispatchIOSBack(): Boolean = dispatchBackEvent()
