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
            VStack(spacing: 10) {
                // 1. Floating MiniPlayer (Nổi phía trên Bottom Nav Bar)
                if playerService.currentSong != nil {
                    MiniPlayerView(onExpand: {
                        showNowPlayingSheet = true
                    })
                    .padding(.horizontal, 16)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                
                // 2. Liquid Glass Floating Bottom Navigation Bar (Cân đối, rộng rãi hơn)
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
                .padding(.horizontal, 10)
                .padding(.vertical, 8)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 28)
                        .fill(.ultraThinMaterial)
                        .background(ColorTokens.darkSurface.opacity(0.70))
                )
                .clipShape(RoundedRectangle(cornerRadius: 28))
                .overlay(
                    RoundedRectangle(cornerRadius: 28)
                        .stroke(ColorTokens.luminousRim, lineWidth: 0.8)
                )
                .shadow(color: Color.black.opacity(0.45), radius: 16, x: 0, y: 8)
                .padding(.horizontal, 16)
                .padding(.bottom, 10)
            }
        }
        .sheet(isPresented: $showNowPlayingSheet) {
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
            VStack(spacing: 4) {
                Image(systemName: isSelected ? selectedIcon : unselectedIcon)
                    .font(.system(size: 22, weight: isSelected ? .bold : .medium))
                    .foregroundColor(isSelected ? ColorTokens.primaryAccent : ColorTokens.textSecondary)
                
                Text(title)
                    .font(.system(size: 11.5, weight: isSelected ? .bold : .medium))
                    .foregroundColor(isSelected ? ColorTokens.primaryAccent : ColorTokens.textSecondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
            .background(
                RoundedRectangle(cornerRadius: 22)
                    .fill(isSelected ? ColorTokens.primaryAccent.opacity(0.14) : Color.clear)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 22)
                    .stroke(isSelected ? ColorTokens.primaryAccent.opacity(0.32) : Color.clear, lineWidth: 0.75)
            )
            .clipShape(RoundedRectangle(cornerRadius: 22))
        }
        .buttonStyle(.plain)
    }
}
