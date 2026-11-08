import SwiftUI
import SharedLogic

struct LifeSlice: Identifiable {
    let id: Int
    let fraction: Double
    let color: Color
    let label: String
}

struct LegendRow: Identifiable {
    let id: Int
    let color: Color
    let label: String
    let percent: Int
}

struct LifeSnapshot {
    let progress: Double
    let clockText: String
    let slices: [LifeSlice]

    var legend: [LegendRow] {
        guard progress > 0 else { return [] }
        return slices
            .sorted { $0.fraction > $1.fraction }
            .map { LegendRow(id: $0.id, color: $0.color, label: $0.label, percent: Int(($0.fraction / progress * 100).rounded())) }
    }

    static func load() -> LifeSnapshot? {
        guard let progress = WidgetProfileStore.shared.loadLifeProgress() else { return nil }
        let slices = progress.segments.enumerated().map { index, segment in
            LifeSlice(
                id: index,
                fraction: segment.fraction,
                color: Color(argb: segment.color),
                label: ActivityLabel.of(key: segment.activityKey, customName: segment.customName)
            )
        }
        return LifeSnapshot(progress: progress.progress, clockText: progress.clockText, slices: slices)
    }

    static let placeholder = LifeSnapshot(
        progress: 0.45,
        clockText: "10:48",
        slices: [
            LifeSlice(id: 0, fraction: 0.15, color: Color(argb: 0xFF14B8A6), label: ActivityLabel.of(key: "SLEEP", customName: nil)),
            LifeSlice(id: 1, fraction: 0.08, color: Color(argb: 0xFFFF8A3D), label: ActivityLabel.of(key: "WORK", customName: nil)),
            LifeSlice(id: 2, fraction: 0.22, color: Color(argb: 0xFFC3B4FF), label: ActivityLabel.of(key: "FREE_TIME", customName: nil)),
        ]
    )
}

enum ActivityLabel {
    static func of(key: String, customName: String?) -> String {
        switch key {
        case "SLEEP": return String(localized: "Sleep")
        case "WORK": return String(localized: "Work")
        case "EDUCATION": return String(localized: "Education")
        case "COMMUTE": return String(localized: "Commute")
        case "EATING": return String(localized: "Eating")
        case "HOUSEHOLD": return String(localized: "Household")
        case "PERSONAL_CARE": return String(localized: "Personal care")
        case "FREE_TIME": return String(localized: "Free time")
        default: return customName ?? ""
        }
    }
}
