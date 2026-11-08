import SwiftUI
import WidgetKit

struct ClockWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "ClockWidget", intent: LegendIntent.self, provider: LifeTimelineProvider()) { entry in
            ClockWidgetView(entry: entry)
                .containerBackground(Brand.panel, for: .widget)
        }
        .configurationDisplayName("Life clock")
        .description("Your life as a 24-hour clock, split by how you spent it.")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    }
}

struct ClockWidgetView: View {
    let entry: LifeEntry

    var body: some View {
        GeometryReader { geometry in
            let legend = entry.snapshot?.legend ?? []
            let position = legend.isEmpty ? .hidden : resolve(entry.legend, geometry.size)
            switch position {
            case .side:
                HStack(spacing: 12) {
                    ClockFace(snapshot: entry.snapshot)
                        .frame(maxWidth: geometry.size.width * Self.maxClockShare)
                    LegendView(rows: legend)
                }
            case .bottom:
                VStack(spacing: 8) {
                    ClockFace(snapshot: entry.snapshot)
                        .frame(maxHeight: geometry.size.height * Self.maxClockShare)
                    LegendView(rows: legend)
                }
            default:
                ClockFace(snapshot: entry.snapshot)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .overlay(alignment: .bottom) {
            if entry.snapshot == nil {
                SetUpHint()
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(entry.snapshot.map { "Life clock at \($0.clockText)" } ?? "Life clock")
    }

    private func resolve(_ option: LegendPositionOption, _ size: CGSize) -> LegendPositionOption {
        guard option == .automatic else { return option }
        if size.width >= size.height * Self.legendAspectRatio { return .side }
        if size.height >= size.width * Self.legendAspectRatio { return .bottom }
        return .hidden
    }

    private static let legendAspectRatio: CGFloat = 1.5
    private static let maxClockShare: CGFloat = 0.6
}

struct SetUpHint: View {
    var body: some View {
        Text("Open TimeOfLife to set up")
            .font(.caption2.weight(.semibold))
            .foregroundStyle(Brand.text)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Capsule().fill(Brand.future))
    }
}
