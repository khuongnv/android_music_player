import SwiftUI

public enum ColorTokens {
    // Apple Music Red Accent
    public static let primaryAccent = Color(red: 250/255, green: 45/255, blue: 85/255) // #FA2D55
    public static let secondaryAccent = Color(red: 255/255, green: 55/255, blue: 95/255) // #FF375F
    
    // Backgrounds
    public static let amoledBlack = Color.black
    public static let darkSurface = Color(red: 28/255, green: 28/255, blue: 30/255) // #1C1C1E
    public static let darkSurfaceVariant = Color(red: 44/255, green: 44/255, blue: 46/255) // #2C2C2E
    
    // Hairline Border
    public static let luminousRim = Color.white.opacity(0.12)
    public static let hairlineDivider = Color.white.opacity(0.08)
    
    // Text
    public static let textPrimary = Color.white
    public static let textSecondary = Color(red: 142/255, green: 142/255, blue: 147/255) // #8E8E93
}

public struct LiquidGlassModifier: ViewModifier {
    var cornerRadius: CGFloat = 16
    var blurRadius: CGFloat = 20
    
    public func body(content: Content) -> some View {
        content
            .background(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(.ultraThinMaterial)
                    .background(ColorTokens.darkSurface.opacity(0.6))
            )
            .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
            .overlay(
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(ColorTokens.luminousRim, lineWidth: 0.75)
            )
            .shadow(color: Color.black.opacity(0.35), radius: 12, x: 0, y: 6)
    }
}

public extension View {
    func liquidGlass(cornerRadius: CGFloat = 16) -> some View {
        modifier(LiquidGlassModifier(cornerRadius: cornerRadius))
    }
}

