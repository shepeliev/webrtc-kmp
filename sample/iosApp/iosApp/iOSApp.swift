import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
    
    init() {
        WebRtc.shared.configure(loggingSeverity: .rtcloggingseverityinfo)
    }
}
