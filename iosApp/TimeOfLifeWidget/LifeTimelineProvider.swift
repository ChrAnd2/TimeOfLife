import WidgetKit

struct LifeEntry: TimelineEntry {
    let date: Date
    let snapshot: LifeSnapshot?
    let legend: LegendPositionOption
}

struct LifeTimelineProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> LifeEntry {
        LifeEntry(date: .now, snapshot: .placeholder, legend: .automatic)
    }

    func snapshot(for configuration: LegendIntent, in context: Context) async -> LifeEntry {
        LifeEntry(date: .now, snapshot: LifeSnapshot.load() ?? .placeholder, legend: configuration.legend)
    }

    func timeline(for configuration: LegendIntent, in context: Context) async -> Timeline<LifeEntry> {
        let entry = LifeEntry(date: .now, snapshot: LifeSnapshot.load(), legend: configuration.legend)
        let calendar = Calendar.current
        let tomorrow = calendar.startOfDay(for: calendar.date(byAdding: .day, value: 1, to: .now) ?? .now)
        return Timeline(entries: [entry], policy: .after(tomorrow))
    }
}
