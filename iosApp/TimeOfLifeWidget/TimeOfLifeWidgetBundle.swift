import SwiftUI
import WidgetKit

@main
struct TimeOfLifeWidgetBundle: WidgetBundle {
    var body: some Widget {
        ClockWidget()
        BarWidget()
    }
}
