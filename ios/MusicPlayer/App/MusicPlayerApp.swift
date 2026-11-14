import SwiftUI

@main
struct MusicPlayerApp: App {
    init() {
        // Áp dụng phong cách thanh TabBar và NavigationBar trong suốt / Liquid Glass
        let appearance = UITabBarAppearance()
        appearance.configureWithDefaultBackground()
        appearance.backgroundColor = UIColor(ColorTokens.darkSurface.opacity(0.85))
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }
    
    var body: some Scene {
        WindowGroup {
            MainTabView()
                .preferredColorScheme(.dark) // Ưu tiên Dark Theme phong cách Apple Music
        }
    }
}

