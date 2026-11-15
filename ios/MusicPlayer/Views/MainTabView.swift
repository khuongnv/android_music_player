import SwiftUI

public struct MainTabView: View {
    @State private var selectedTab: Int = 0
    @State private var showNowPlayingSheet: Bool = false
    @ObservedObject var playerService = AudioPlayerService.shared
    
    public var body: some View {
        ZStack(alignment: .bottom) {
            // AMOLED Black Root Background
            ColorTokens.amoledBlack.ignoresSafeArea()
            
            // Nội dung tab tương ứng
            Group {
                switch selectedTab {
                case 0:
                    LibraryView()
                case 1:
                    TimelineView()
                case 2:
                    SettingsView()
                default:
                    LibraryView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            
            // Các thành phần nổi ở đáy màn hình
            VStack(spacing: 8) {
                // 1. Floating MiniPlayer (Nổi phía trên Bottom Nav Bar)
                if playerService.currentSong != nil {
                    MiniPlayerView(onExpand: {
                        showNowPlayingSheet = true
                    })
                    .padding(.horizontal, 16)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                
                // 2. Liquid Glass Floating Bottom Navigation Bar
                HStack(spacing: 0) {
                    navItem(
                        tag: 0,
                        title: "Thư viện",
                        selectedIcon: "music.note.house.fill",
                        unselectedIcon: "music.note.house"
                    )
                    
                    navItem(
                        tag: 1,
                        title: "Ký ức",
                        selectedIcon: "clock.arrow.circlepath",
                        unselectedIcon: "clock.arrow.circlepath"
                    )
                    
                    navItem(
                        tag: 2,
                        title: "Cài đặt",
                        selectedIcon: "gearshape.fill",
                        unselectedIcon: "gearshape"
                    )
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 6)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 26)
                        .fill(.ultraThinMaterial)
                        .background(ColorTokens.darkSurface.opacity(0.65))
                )
                .clipShape(RoundedRectangle(cornerRadius: 26))
                .overlay(
                    RoundedRectangle(cornerRadius: 26)
                        .stroke(ColorTokens.luminousRim, lineWidth: 0.75)
                )
                .shadow(color: Color.black.opacity(0.4), radius: 14, x: 0, y: 7)
                .padding(.horizontal, 16)
                .padding(.bottom, 6)
            }
        }
        .fullScreenCover(isPresented: $showNowPlayingSheet) {
            NowPlayingView()
        }
    }
    
    // MARK: - Component nút bấm Tab với hiệu ứng Liquid Glass Pill
    @ViewBuilder
    private func navItem(tag: Int, title: String, selectedIcon: String, unselectedIcon: String) -> some View {
        let isSelected = (selectedTab == tag)
        
        Button(action: {
            withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                selectedTab = tag
            }
        }) {
            VStack(spacing: 3) {
                Image(systemName: isSelected ? selectedIcon : unselectedIcon)
                    .font(.system(size: 20))
                    .foregroundColor(isSelected ? ColorTokens.primaryAccent : ColorTokens.textSecondary)
                
                Text(title)
                    .font(.system(size: 11, weight: isSelected ? .bold : .medium))
                    .foregroundColor(isSelected ? ColorTokens.primaryAccent : ColorTokens.textSecondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 6)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(isSelected ? ColorTokens.primaryAccent.opacity(0.14) : Color.clear)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(isSelected ? ColorTokens.primaryAccent.opacity(0.32) : Color.clear, lineWidth: 0.75)
            )
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
        .buttonStyle(.plain)
    }
}
