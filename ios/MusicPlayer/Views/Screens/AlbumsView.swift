import SwiftUI

public struct AlbumsView: View {
    var songs: [Song]
    
    private var albums: [Album] {
        var dict = [String: [Song]]()
        for song in songs {
            dict[song.album, default: []].append(song)
        }
        return dict.map { (albumTitle, songList) in
            Album(
                id: albumTitle,
                title: albumTitle,
                artist: songList.first?.artist ?? "Unknown Artist",
                artworkURL: songList.first?.artworkURL,
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
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(ColorTokens.darkSurfaceVariant)
                                    .aspectRatio(1, contentMode: .fit)
                                    .overlay(
                                        Image(systemName: "music.note")
                                            .font(.system(size: 36))
                                            .foregroundColor(ColorTokens.primaryAccent)
                                    )
                                    .shadow(color: Color.black.opacity(0.3), radius: 6, x: 0, y: 3)
                                
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
                .padding(.bottom, 70)
            }
        }
        .navigationTitle("Album (\(albums.count))")
    }
}
