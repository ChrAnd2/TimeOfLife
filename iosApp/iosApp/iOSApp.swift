import SwiftUI
import WidgetKit
import SharedUI

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.doInitKoin()
        MainViewControllerKt.observeProfileForWidgets {
            WidgetCenter.shared.reloadAllTimelines()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
        }
    }
}
