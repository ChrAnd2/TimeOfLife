import SwiftUI

enum Brand {
    static let panel = Color(argb: 0xFF11262A)
    static let rim = Color(argb: 0xFF2A4A50)
    static let future = Color(argb: 0xFF1F3B40)
    static let text = Color.white
    static let textDim = Color.white.opacity(0.6)
    static let hand = Color.white
}

extension Color {
    init(argb: Int64) {
        self.init(
            .sRGB,
            red: Double((argb >> 16) & 0xFF) / 255,
            green: Double((argb >> 8) & 0xFF) / 255,
            blue: Double(argb & 0xFF) / 255,
            opacity: Double((argb >> 24) & 0xFF) / 255
        )
    }
}
