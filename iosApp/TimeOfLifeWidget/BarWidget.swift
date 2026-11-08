import SwiftUI
import WidgetKit

struct BarWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "BarWidget", intent: LegendIntent.self, provider: LifeTimelineProvider()) { entry in
            BarWidgetView(entry: entry)
                .containerBackground(Brand.panel, for: .widget)
        }
        .configurationDisplayName("Life bar")
        .description("Your life as a bar, split by how you spent it.")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

struct BarWidgetView: View {
    let entry: LifeEntry

    var body: some View {
        GeometryReader { geometry in
            let legend = entry.snapshot?.legend ?? []
            let position = legend.isEmpty ? .hidden : resolve(entry.legend, geometry.size)
            switch position {
            case .side:
                HStack(spacing: 12) {
                    centeredBar
                        .frame(width: geometry.size.width * Self.maxBarShare)
                    LegendView(rows: legend)
                }
            case .bottom:
                VStack(spacing: 8) {
                    LifeBar(snapshot: entry.snapshot)
                        .frame(height: min(Self.maxBarHeight, geometry.size.height * Self.maxBarShare))
                    LegendView(rows: legend)
                }
            default:
                centeredBar
            }
        }
        .overlay(alignment: .bottom) {
            if entry.snapshot == nil {
                SetUpHint()
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(entry.snapshot.map { "Life bar, \(Int(($0.progress * 100).rounded())) % lived" } ?? "Life bar")
    }

    private var centeredBar: some View {
        VStack {
            Spacer(minLength: 0)
            LifeBar(snapshot: entry.snapshot)
                .frame(height: Self.maxBarHeight)
            Spacer(minLength: 0)
        }
    }

    private func resolve(_ option: LegendPositionOption, _ size: CGSize) -> LegendPositionOption {
        guard option == .automatic else { return option }
        return size.height >= Self.maxBarHeight + 8 + 2 * LegendView.minRowHeight ? .bottom : .hidden
    }

    private static let maxBarHeight: CGFloat = 40
    private static let maxBarShare: CGFloat = 0.6
}
