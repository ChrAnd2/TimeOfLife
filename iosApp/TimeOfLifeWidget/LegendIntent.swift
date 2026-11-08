import AppIntents
import WidgetKit

enum LegendPositionOption: String, AppEnum {
    case automatic
    case side
    case bottom
    case hidden

    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Legend"

    static var caseDisplayRepresentations: [LegendPositionOption: DisplayRepresentation] = [
        .automatic: "Automatic (depends on widget size)",
        .side: "Beside",
        .bottom: "Below",
        .hidden: "Hidden",
    ]
}

struct LegendIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Widget settings"

    @Parameter(title: "Legend", default: .automatic)
    var legend: LegendPositionOption
}
