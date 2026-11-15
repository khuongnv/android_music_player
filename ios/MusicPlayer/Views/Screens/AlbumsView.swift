import SwiftUI

public struct AlbumsView: View {
    var songs: [Song]
    @Environment(\.dismiss) private var dismiss
    
    private var albums: [Album] {
        var dict = [String: [Song]]()
        for song in songs {
            dict[song.album, default: []].append(song)
        }
        return dict.map { (albumTitle, songList) in
            // Lấy bài đầu tiên có artworkURL nếu có
            let firstArt = songList.first(where: { $0.artworkURL != nil })?.artworkURL ?? songList.first?.artworkURL
            return Album(
                id: albumTitle,
                title: albumTitle,
                artist: songList.first?.artist ?? "Không xác định",
                artworkURL: firstArt,
                songCount: songList.count
            )
        }.sorted { $0.title.localizedCompare($1.title) == .orderedAscending }
    }
    
    private let columns = [
        GridItem(.flexible(), spacing: 16),
        GridItem(.flexible(), spacing: 16)
    ]
    
    public var body: some View {
        ZStack {
            ColorTokens.amoledBlack.ignoresSafeArea()
            
            ScrollView {
                LazyVGrid(columns: columns, spacing: 20) {
                    ForEach(albums) { album in
                        NavigationLink(destination: AlbumDetailView(album: album, songs: songs.filter { $0.album == album.title })) {
                            VStack(alignment: .leading, spacing: 8) {
                                SongArtworkView(
                                    artworkURL: album.artworkURL,
                                    size: 160,
                                    cornerRadius: 12,
                                    iconSize: 40
                                )
                                .shadow(color: Color.black.opacity(0.35), radius: 8, x: 0, y: 4)
                                
                                Text(album.title)
                                    .font(.system(size: 15, weight: .semibold))
                                    .foregroundColor(ColorTokens.textPrimary)
                                    .lineLimit(1)
                                
                                Text("\(album.artist) • \(album.songCount) bài")
                                    .font(.system(size: 13))
                                    .foregroundColor(ColorTokens.textSecondary)
                                    .lineLimit(1)
                            }
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 80)
            }
        }
        .navigationTitle("Album (\(albums.count))")
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
}
