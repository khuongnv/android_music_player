import SwiftUI

public struct MainTabView: View {
    @State private var selectedTab: Int = 0
    @State private var showNowPlayingSheet: Bool = false
    @ObservedObject var playerService = AudioPlayerService.shared
    
    public var body: some View {
        ZStack(alignment: .bottom) {
            TabView(selection: $selectedTab) {
                LibraryView()
                    .tabItem {
                        Label("Thư viện", systemImage: "music.note.house.fill")
                    }
                    .tag(0)
                
                TimelineView()
                    .tabItem {
                        Label("Ký ức", systemImage: "clock.arrow.circlepath")
                    }
                    .tag(1)
                
                SettingsView()
                    .tabItem {
                        Label("Cài đặt", systemImage: "gearshape.fill")
                    }
                    .tag(2)
            }
            .tint(ColorTokens.primaryAccent)
            
            // Floating MiniPlayer
            if playerService.currentSong != nil {
                MiniPlayerView(onExpand: {
                    showNowPlayingSheet = true
                })
                .padding(.bottom, 54) // Nằm nổi ngay trên TabBar
            }
        }
        .fullScreenCover(isPresented: $showNowPlayingSheet) {
            NowPlayingView()
        }
    }
}
