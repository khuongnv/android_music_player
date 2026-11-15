import SwiftUI
import UniformTypeIdentifiers

public struct LibraryView: View {
    @State private var songs: [Song] = []
    @State private var isLoading: Bool = false
    @State private var showingFileImporter: Bool = false
    @State private var showingPermissionAlert: Bool = false
    @State private var importAlertMessage: String? = nil
    @State private var showingImportAlert: Bool = false
    
    @ObservedObject var playerService = AudioPlayerService.shared
    
    public var body: some View {
        NavigationStack {
            ZStack {
                ColorTokens.amoledBlack.ignoresSafeArea()
                
                if isLoading {
                    VStack(spacing: 16) {
                        ProgressView()
                            .tint(ColorTokens.primaryAccent)
                            .scaleEffect(1.2)
                        Text("Đang quét bài hát trong máy...")
                            .font(.system(size: 15, weight: .medium))
                            .foregroundColor(ColorTokens.textSecondary)
                    }
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
                            
                            // Section: Content or Empty State
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Text("Bài hát trong máy")
                                        .font(.system(size: 20, weight: .bold))
                                        .foregroundColor(ColorTokens.textPrimary)
                                    Spacer()
                                    if !songs.isEmpty {
                                        Text("\(songs.count) bài")
                                            .font(.system(size: 14))
                                            .foregroundColor(ColorTokens.textSecondary)
                                    }
                                }
                                .padding(.horizontal, 20)
                                
                                if songs.isEmpty {
                                    VStack(spacing: 16) {
                                        Image(systemName: "music.note.list")
                                            .font(.system(size: 48))
                                            .foregroundColor(ColorTokens.primaryAccent.opacity(0.8))
                                            .padding(.top, 12)
                                        
                                        Text("Chưa tìm thấy bài hát nào")
                                            .font(.system(size: 17, weight: .semibold))
                                            .foregroundColor(ColorTokens.textPrimary)
                                        
                                        Text("Bạn có thể chọn trực tiếp các file nhạc (.mp3, .m4a, .flac) từ ứng dụng Tệp (Files / Tải về) hoặc cấp quyền đọc thư viện máy.")
                                            .font(.system(size: 13))
                                            .foregroundColor(ColorTokens.textSecondary)
                                            .multilineTextAlignment(.center)
                                            .padding(.horizontal, 24)
                                        
                                        HStack(spacing: 12) {
                                            Button(action: {
                                                showingFileImporter = true
                                            }) {
                                                Label("Chọn từ Tệp", systemImage: "folder.badge.plus")
                                                    .font(.system(size: 14, weight: .semibold))
                                                    .foregroundColor(.white)
                                                    .padding(.horizontal, 16)
                                                    .padding(.vertical, 10)
                                                    .background(ColorTokens.primaryAccent)
                                                    .cornerRadius(20)
                                            }
                                            
                                            Button(action: {
                                                Task { await loadLocalMusic() }
                                            }) {
                                                Label("Quét lại", systemImage: "arrow.clockwise")
                                                    .font(.system(size: 14, weight: .semibold))
                                                    .foregroundColor(ColorTokens.textPrimary)
                                                    .padding(.horizontal, 16)
                                                    .padding(.vertical, 10)
                                                    .background(ColorTokens.darkSurfaceVariant)
                                                    .cornerRadius(20)
                                            }
                                        }
                                        .padding(.top, 6)
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 24)
                                    .liquidGlass(cornerRadius: 18)
                                    .padding(.horizontal, 16)
                                } else {
                                    LazyVStack(spacing: 0) {
                                        ForEach(songs.prefix(50)) { song in
                                            HStack(spacing: 14) {
                                                SongArtworkView(artworkURL: song.artworkURL, size: 44, cornerRadius: 8, iconSize: 20)
                                                
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
                                                
                                                Text(formatDuration(song.duration))
                                                    .font(.system(size: 12, design: .monospaced))
                                                    .foregroundColor(ColorTokens.textSecondary)
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
                            .padding(.bottom, 140) // Chừa khoảng trống an toàn cho Floating Bar & MiniPlayer
                        }
                    }
                }
            }
            .navigationTitle("Thư viện")
            .toolbar {
                ToolbarItemGroup(placement: .navigationBarTrailing) {
                    Button(action: {
                        showingFileImporter = true
                    }) {
                        Image(systemName: "folder.badge.plus")
                            .foregroundColor(ColorTokens.primaryAccent)
                    }
                    
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
            .fileImporter(
                isPresented: $showingFileImporter,
                allowedContentTypes: [.audio],
                allowsMultipleSelection: true
            ) { result in
                switch result {
                case .success(let urls):
                    Task {
                        isLoading = true
                        let count = await LocalAudioScanner.shared.importAudioFiles(from: urls)
                        await loadLocalMusic()
                        importAlertMessage = "Đã nhập thành công \(count) bài hát vào Thư viện!"
                        showingImportAlert = true
                    }
                case .failure(let error):
                    importAlertMessage = "Không thể đọc tệp: \(error.localizedDescription)"
                    showingImportAlert = true
                }
            }
            .alert("Thông báo", isPresented: $showingImportAlert) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(importAlertMessage ?? "")
            }
        }
    }
    
    private func loadLocalMusic() async {
        isLoading = true
        let docSongs = await LocalAudioScanner.shared.scanDocumentsDirectory()
        let mediaSongs = await LocalAudioScanner.shared.scanNativeMediaLibrary()
        
        // Hợp nhất bài hát và loại bỏ trùng lặp theo ID hoặc Title+Artist
        var combined = docSongs
        for s in mediaSongs {
            if !combined.contains(where: { $0.title == s.title && $0.artist == s.artist }) {
                combined.append(s)
            }
        }
        
        self.songs = combined
        self.isLoading = false
    }
    
    private func formatDuration(_ duration: TimeInterval) -> String {
        let mins = Int(duration) / 60
        let secs = Int(duration) % 60
        return String(format: "%d:%02d", mins, secs)
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
