import UIKit
import SwiftUI
import ComposeApp
import CoreNFC
import Network

final class NetworkAccessMonitor: ObservableObject {
    @Published var showSettingsAlert = false
    @Published var alertMessage = ""

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "org.cf0x.spicecompose.network-monitor")
    private var started = false

    func start() {
        guard !started else { return }
        started = true
        monitor.pathUpdateHandler = { [weak self] path in
            guard let self else { return }
            let localNetworkDenied: Bool
            if #available(iOS 14.2, *) {
                localNetworkDenied = path.status == .unsatisfied && path.unsatisfiedReason == .localNetworkDenied
            } else {
                localNetworkDenied = false
            }
            let noNetwork = path.status == .unsatisfied && !localNetworkDenied
            guard localNetworkDenied || noNetwork else { return }
            DispatchQueue.main.async {
                self.alertMessage = localNetworkDenied
                    ? "Local Network access is disabled for SpiceCompose. Enable it in Settings to connect to spice2x."
                    : "No internet or local network connection is available. Check your network settings."
                self.showSettingsAlert = true
            }
        }
        monitor.start(queue: queue)
    }

    func stop() {
        guard started else { return }
        monitor.cancel()
        started = false
    }
}

/// Bridges Core NFC tag identifiers into the shared Kotlin flow used by the
/// card-management screens. Core NFC sessions are short-lived: after a tag is
/// detected the session is invalidated and restarted for the next card.
final class NFCReader: NSObject, NFCTagReaderSessionDelegate {
    static let shared = NFCReader()

    private var session: NFCTagReaderSession?
    private var restartWorkItem: DispatchWorkItem?
    private var shouldRestart = false

    func start() {
        shouldRestart = true
        guard NFCTagReaderSession.readingAvailable, session == nil else { return }
        guard let reader = NFCTagReaderSession(
            pollingOption: [.iso14443, .iso15693, .iso18092],
            delegate: self,
            queue: .main
        ) else { return }
        reader.alertMessage = "Hold your iPhone near the card."
        session = reader
        reader.begin()
    }

    func stop() {
        shouldRestart = false
        restartWorkItem?.cancel()
        restartWorkItem = nil
        session?.invalidate()
        session = nil
    }

    func tagReaderSessionDidBecomeActive(_ session: NFCTagReaderSession) {}

    func tagReaderSession(_ session: NFCTagReaderSession, didInvalidateWithError error: Error) {
        if self.session === session {
            self.session = nil
        }
        // Invalidations also happen after a successful read. Restart after a
        // short delay so the same tag cannot be emitted repeatedly in a loop.
        guard shouldRestart, NFCTagReaderSession.readingAvailable else { return }
        let work = DispatchWorkItem { [weak self] in self?.start() }
        restartWorkItem = work
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5, execute: work)
    }

    func tagReaderSession(_ session: NFCTagReaderSession, didDetect tags: [NFCTag]) {
        guard let tag = tags.first else { return }
        session.connect(to: tag) { [weak self] error in
            guard let self else { return }
            guard error == nil else {
                session.invalidate(errorMessage: error?.localizedDescription ?? "Unable to read NFC tag.")
                return
            }

            let identifier: Data?
            switch tag {
            case .miFare(let tag):
                identifier = tag.identifier
            case .iso7816(let tag):
                identifier = tag.identifier
            case .iso15693(let tag):
                identifier = tag.identifier
            case .feliCa(let tag):
                identifier = tag.currentIDm
            @unknown default:
                identifier = nil
            }

            guard let identifier, !identifier.isEmpty else {
                session.invalidate(errorMessage: "This NFC tag has no readable identifier.")
                return
            }

            let id = identifier.map { String(format: "%02X", $0) }.joined()
            NfcManager.shared.onTagDiscovered(id: id)
            session.alertMessage = "Card read successfully."
            session.invalidate()
            self.session = nil
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @StateObject private var networkAccess = NetworkAccessMonitor()

    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // Compose has its own keyboard handler
            .onAppear { NFCReader.shared.start() }
            .onDisappear { NFCReader.shared.stop() }
            .onAppear { networkAccess.start() }
            .onDisappear { networkAccess.stop() }
            .alert(isPresented: $networkAccess.showSettingsAlert) {
                Alert(
                    title: Text("Network Access"),
                    message: Text(networkAccess.alertMessage),
                    primaryButton: .default(Text("Open Settings")) {
                    guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                    UIApplication.shared.open(url)
                    },
                    secondaryButton: .cancel(Text("Cancel"))
                )
            }
    }
}
