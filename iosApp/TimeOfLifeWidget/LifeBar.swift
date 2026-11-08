import SwiftUI

struct LifeBar: View {
    let snapshot: LifeSnapshot?

    var body: some View {
        Canvas { context, size in
            let overhang = size.height * 0.1
            let bar = CGRect(x: 0, y: overhang, width: size.width, height: size.height - overhang * 2)
            let shape = Path(roundedRect: bar, cornerRadius: bar.height * 0.3)
            context.fill(shape, with: .color(Brand.future))
            guard let snapshot else { return }
            context.drawLayer { layer in
                layer.clip(to: shape)
                var left = bar.minX
                for slice in snapshot.slices {
                    let width = bar.width * slice.fraction
                    layer.fill(Path(CGRect(x: left, y: bar.minY, width: width, height: bar.height)), with: .color(slice.color))
                    left += width
                }
            }
            let x = bar.minX + bar.width * snapshot.progress
            var marker = Path()
            marker.move(to: CGPoint(x: x, y: 0))
            marker.addLine(to: CGPoint(x: x, y: size.height))
            context.drawLayer { layer in
                layer.addFilter(.shadow(color: .black.opacity(0.65), radius: bar.height * 0.1))
                layer.stroke(marker, with: .color(Brand.hand), style: StrokeStyle(lineWidth: bar.height * 0.14, lineCap: .round))
            }
        }
    }
}
