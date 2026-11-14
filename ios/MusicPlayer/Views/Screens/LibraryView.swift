import SwiftUI

public struct LibraryView: View {
    @State private var songs: [Song] = []
    @State private var isLoading: Bool = false
    @ObservedObject var playerService = AudioPlayerService.shared
    
    public var body: some View {
        NavigationStack {
            ZStack {
                ColorTokens.amoledBlack.ignoresSafeArea()
                
                if isLoading {
                    ProgressView("Đang quét thư viện...")
                        .tint(ColorTokens.primaryAccent)
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 20) {
                            // Section: Quick Categories Navigation
                            VStack(spacing: 0) {
                                NavigationLink(destination: SongsView(songs: songs)) {
                                    CategoryRow(title: "Bài hát", icon: "music.note", count: songs.count)
                                }
                                Divider().background(ColorTokens.hairlineDivider).padding(.leading, 56)
                                
                                NavigationLink(destination: AlbumsView(songs: songs)) {
                                    CategoryRow(title: "Album", icon: "square.stack", count: Set(songs.map { $0.album }).count)
                                }
                                Divider().background(ColorTokens.hairlineDivider).padding(.leading, 56)
                                
                                NavigationLink(destination: PlaylistsView()) {
                                    CategoryRow(title: "Danh sách phát", icon: "music.note.list", count: StorageManager.shared.playlists.count)
                                }
                            }
                            .padding(.horizontal, 16)
                            .liquidGlass(cornerRadius: 16)
                            .padding(.horizontal, 16)
                            .padding(.top, 12)
                            
                            // Section: Recently Added
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Đã thêm gần đây")
                                    .font(.system(size: 20, weight: .bold))
                                    .foregroundColor(ColorTokens.textPrimary)
                                    .padding(.horizontal, 20)
                                
                                if songs.isEmpty {
                                    VStack(spacing: 12) {
                                        Text("Không có bài hát nào trong kho nhạc")
                                            .font(.subheadline)
                                            .foregroundColor(ColorTokens.textSecondary)
                                        Text("Sao chép file vào app qua iTunes / Files hoặc cấp quyền đọc thư viện máy.")
                                            .font(.caption)
                                            .foregroundColor(ColorTokens.textSecondary)
                                            .multilineTextAlignment(.center)
                                            .padding(.horizontal, 32)
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 32)
                                } else {
                                    LazyVStack(spacing: 0) {
                                        ForEach(songs.prefix(20)) { song in
                                            HStack(spacing: 14) {
                                                RoundedRectangle(cornerRadius: 6)
                                                    .fill(ColorTokens.darkSurfaceVariant)
                                                    .frame(width: 44, height: 44)
                                                    .overlay(
                                                        Image(systemName: "music.note")
                                                            .foregroundColor(ColorTokens.primaryAccent)
                                                    )
                                                
                                                VStack(alignment: .leading, spacing: 3) {
                                                    Text(song.title)
                                                        .font(.system(size: 15, weight: .semibold))
                                                        .foregroundColor(ColorTokens.textPrimary)
                                                        .lineLimit(1)
                                                    
                                                    Text(song.artist)
                                                        .font(.system(size: 12))
                                                        .foregroundColor(ColorTokens.textSecondary)
                                                        .lineLimit(1)
                                                }
                                                
                                                Spacer()
                                            }
                                            .padding(.horizontal, 20)
                                            .padding(.vertical, 10)
                                            .contentShape(Rectangle())
                                            .onTapGesture {
                                                if let index = songs.firstIndex(of: song) {
                                                    playerService.play(songs: songs, startIndex: index)
                                                    StorageManager.shared.recordPlayback(song: song, position: 0)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .padding(.bottom, 80)
                        }
                    }
                }
            }
            .navigationTitle("Thư viện")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        Task { await loadLocalMusic() }
                    }) {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(ColorTokens.primaryAccent)
                    }
                }
            }
            .task {
                await loadLocalMusic()
            }
        }
    }
    
    private func loadLocalMusic() async {
        isLoading = true
        let docSongs = await LocalAudioScanner.shared.scanDocumentsDirectory()
        let mediaSongs = await LocalAudioScanner.shared.scanNativeMediaLibrary()
        self.songs = docSongs + mediaSongs
        self.isLoading = false
    }
}

struct CategoryRow: View {
    let title: String
    let icon: String
    let count: Int
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(ColorTokens.primaryAccent)
                .frame(width: 28)
            
            Text(title)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(ColorTokens.textPrimary)
            
            Spacer()
            
            Text("\(count)")
                .font(.system(size: 14))
                .foregroundColor(ColorTokens.textSecondary)
            
            Image(systemName: "chevron.right")
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(ColorTokens.textSecondary.opacity(0.6))
        }
        .padding(.vertical, 14)
        .contentShape(Rectangle())
    }
}
