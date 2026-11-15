import SwiftUI

public struct SongsView: View {
    var songs: [Song]
    @ObservedObject var playerService = AudioPlayerService.shared
    @Environment(\.dismiss) private var dismiss
    
    @State private var searchText: String = ""
    @State private var sortOption: SortOption = .title
    
    public enum SortOption: String, CaseIterable, Identifiable {
        case title = "Tiêu đề"
        case artist = "Nghệ sĩ"
        case duration = "Thời lượng"
        public var id: String { rawValue }
    }
    
    private var filteredSongs: [Song] {
        var result = songs
        if !searchText.isEmpty {
            result = result.filter {
                $0.title.localizedCaseInsensitiveContains(searchText) ||
                $0.artist.localizedCaseInsensitiveContains(searchText) ||
                $0.album.localizedCaseInsensitiveContains(searchText)
            }
        }
        switch sortOption {
        case .title:
            result.sort { $0.title.localizedCompare($1.title) == .orderedAscending }
        case .artist:
            result.sort { $0.artist.localizedCompare($1.artist) == .orderedAscending }
        case .duration:
            result.sort { $0.duration > $1.duration }
        }
        return result
    }
    
    public var body: some View {
        ZStack {
            ColorTokens.amoledBlack.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header Shuffle & Sort Controls
                HStack {
                    Button(action: {
                        if !filteredSongs.isEmpty {
                            playerService.shuffleEnabled = true
                            playerService.play(songs: filteredSongs.shuffled())
                        }
                    }) {
                        HStack(spacing: 6) {
                            Image(systemName: "shuffle")
                            Text("Trộn bài")
                        }
                        .font(.system(size: 14, weight: .semibold))
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(ColorTokens.primaryAccent)
                        .foregroundColor(.white)
                        .cornerRadius(20)
                    }
                    
                    Spacer()
                    
                    Menu {
                        Picker("Sắp xếp", selection: $sortOption) {
                            ForEach(SortOption.allCases) { opt in
                                Text(opt.rawValue).tag(opt)
                            }
                        }
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: "arrow.up.arrow.down")
                            Text(sortOption.rawValue)
                        }
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(ColorTokens.primaryAccent)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                
                // Songs List
                List {
                    ForEach(filteredSongs) { song in
                        HStack(spacing: 14) {
                            SongArtworkView(artworkURL: song.artworkURL, size: 44, cornerRadius: 8, iconSize: 20)
                            
                            VStack(alignment: .leading, spacing: 3) {
                                Text(song.title)
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(ColorTokens.textPrimary)
                                    .lineLimit(1)
                                
                                Text(song.artist)
                                    .font(.system(size: 13))
                                    .foregroundColor(ColorTokens.textSecondary)
                                    .lineLimit(1)
                            }
                            
                            Spacer()
                            
                            Text(formatDuration(song.duration))
                                .font(.system(size: 12, design: .monospaced))
                                .foregroundColor(ColorTokens.textSecondary)
                        }
                        .listRowBackground(ColorTokens.amoledBlack)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            if let idx = filteredSongs.firstIndex(of: song) {
                                playerService.play(songs: filteredSongs, startIndex: idx)
                                StorageManager.shared.recordPlayback(song: song, position: 0)
                            }
                        }
                    }
                    
                    // Padding khoảng trống dưới cùng
                    Spacer()
                        .frame(height: 70)
                        .listRowBackground(Color.clear)
                }
                .listStyle(.plain)
                .background(ColorTokens.amoledBlack)
                .searchable(text: $searchText, prompt: "Tìm bài hát, nghệ sĩ...")
            }
        }
        .navigationTitle("Bài hát (\(songs.count))")
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { dismiss() }) {
                    HStack(spacing: 4) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .bold))
                        Text("Thư viện")
                    }
                    .foregroundColor(ColorTokens.primaryAccent)
                }
            }
        }
    }
    
    private func formatDuration(_ duration: TimeInterval) -> String {
        let m = Int(duration) / 60
        let s = Int(duration) % 60
        return String(format: "%d:%02d", m, s)
    }
}
