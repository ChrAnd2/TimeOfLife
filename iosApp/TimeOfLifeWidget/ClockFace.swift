import SwiftUI

struct ClockFace: View {
    let snapshot: LifeSnapshot?

    var body: some View {
        Canvas { context, size in
            let radius = min(size.width, size.height) / 2
            let center = CGPoint(x: size.width / 2, y: size.height / 2)
            context.fill(circle(center, radius), with: .color(Brand.panel))
            let rimWidth = radius * 0.025
            context.stroke(circle(center, radius - rimWidth / 2), with: .color(Brand.rim), lineWidth: rimWidth)
            if let snapshot {
                drawPie(snapshot, in: context, center: center, radius: radius * 0.70)
            }
            drawTicks(in: context, center: center, radius: radius)
            if let snapshot {
                drawHand(snapshot.progress, in: context, center: center, radius: radius)
            }
        }
        .aspectRatio(1, contentMode: .fit)
    }

    private func drawPie(_ snapshot: LifeSnapshot, in context: GraphicsContext, center: CGPoint, radius: CGFloat) {
        context.fill(circle(center, radius), with: .color(Brand.future))
        var start = -90.0
        for slice in snapshot.slices {
            let sweep = 360 * slice.fraction
            var path = Path()
            path.move(to: center)
            path.addArc(center: center, radius: radius, startAngle: .degrees(start), endAngle: .degrees(start + sweep), clockwise: false)
            path.closeSubpath()
            context.fill(path, with: .color(slice.color))
            start += sweep
        }
    }

    private func drawTicks(in context: GraphicsContext, center: CGPoint, radius: CGFloat) {
        for hour in 0..<24 {
            let major = hour % 3 == 0
            let angle = fractionAngle(Double(hour) / 24)
            var path = Path()
            path.move(to: point(center, angle, radius * (major ? 0.85 : 0.89)))
            path.addLine(to: point(center, angle, radius * 0.94))
            context.stroke(
                path,
                with: .color(major ? Brand.text : Brand.textDim),
                style: StrokeStyle(lineWidth: radius * (major ? 0.022 : 0.012), lineCap: .round)
            )
        }
    }

    private func drawHand(_ progress: Double, in context: GraphicsContext, center: CGPoint, radius: CGFloat) {
        var hand = Path()
        hand.move(to: center)
        hand.addLine(to: point(center, fractionAngle(progress), radius * 0.62))
        context.drawLayer { layer in
            layer.addFilter(.shadow(color: .black.opacity(0.4), radius: radius * 0.03))
            layer.stroke(hand, with: .color(Brand.hand), style: StrokeStyle(lineWidth: radius * 0.06, lineCap: .round))
        }
        context.fill(circle(center, radius * 0.055), with: .color(Brand.hand))
        context.fill(circle(center, radius * 0.022), with: .color(Brand.panel))
    }

    private func circle(_ center: CGPoint, _ radius: CGFloat) -> Path {
        Path(ellipseIn: CGRect(x: center.x - radius, y: center.y - radius, width: radius * 2, height: radius * 2))
    }

    private func point(_ center: CGPoint, _ angle: Double, _ radius: CGFloat) -> CGPoint {
        CGPoint(x: center.x + radius * cos(angle), y: center.y + radius * sin(angle))
    }

    private func fractionAngle(_ fraction: Double) -> Double {
        (-90 + 360 * fraction) * .pi / 180
    }
}
