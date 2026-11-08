import SwiftUI

struct LegendView: View {
    let rows: [LegendRow]

    var body: some View {
        GeometryReader { geometry in
            let visibleCount = min(rows.count, Int(geometry.size.height / Self.minRowHeight))
            let rowHeight = min(geometry.size.height / CGFloat(max(visibleCount, 4)), geometry.size.width * 0.16)
            VStack(alignment: .leading, spacing: 0) {
                ForEach(rows.prefix(visibleCount)) { row in
                    HStack(spacing: rowHeight * 0.3) {
                        Circle()
                            .fill(row.color)
                            .frame(width: rowHeight * 0.4, height: rowHeight * 0.4)
                        Text(row.label)
                            .foregroundStyle(Brand.text)
                            .lineLimit(1)
                        Spacer(minLength: rowHeight * 0.3)
                        Text("\(row.percent) %")
                            .foregroundStyle(Brand.textDim)
                            .monospacedDigit()
                    }
                    .font(.system(size: rowHeight * 0.5, weight: .semibold))
                    .frame(height: rowHeight)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }

    static let minRowHeight: CGFloat = 16
}
