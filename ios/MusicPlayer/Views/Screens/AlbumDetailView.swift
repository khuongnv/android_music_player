import SwiftUI

public struct AlbumDetailView: View {
    var album: Album
    var songs: [Song]
    @ObservedObject var playerService = AudioPlayerService.shared
    
    public var body: some View {
        ZStack {
            ColorTokens.amoledBlack.ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 20) {
                    // Big Artwork Header
                    RoundedRectangle(cornerRadius: 16)
                        .fill(ColorTokens.darkSurfaceVariant)
                        .frame(width: 200, height: 200)
                        .overlay(
                            Image(systemName: "music.note")
                                .font(.system(size: 60))
                                .foregroundColor(ColorTokens.primaryAccent)
                        )
                        .shadow(color: Color.black.opacity(0.4), radius: 16, x: 0, y: 8)
                        .padding(.top, 20)
                    
                    VStack(spacing: 4) {
                        Text(album.title)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(ColorTokens.textPrimary)
                            .multilineTextAlignment(.center)
                        
                        Text(album.artist)
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(ColorTokens.primaryAccent)
                    }
                    .padding(.horizontal, 24)
                    
                    // Action Buttons (Play All & Shuffle)
                    HStack(spacing: 16) {
                        Button(action: {
                            if !songs.isEmpty {
                                playerService.shuffleEnabled = false
                                playerService.play(songs: songs)
                            }
                        }) {
                            HStack {
                                Image(systemName: "play.fill")
                                Text("Phát")
                            }
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(ColorTokens.darkSurface)
                            .foregroundColor(ColorTokens.primaryAccent)
                            .cornerRadius(12)
                        }
                        
                        Button(action: {
                            if !songs.isEmpty {
                                playerService.shuffleEnabled = true
                                playerService.play(songs: songs.shuffled())
                            }
                        }) {
                            HStack {
                                Image(systemName: "shuffle")
                                Text("Trộn bài")
                            }
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(ColorTokens.darkSurface)
                            .foregroundColor(ColorTokens.primaryAccent)
                            .cornerRadius(12)
                        }
                    }
                    .padding(.horizontal, 24)
                    
                    // Tracklist
                    VStack(spacing: 0) {
                        ForEach(Array(songs.enumerated()), id: \.element.id) { index, song in
                            HStack(spacing: 16) {
                                Text("\(index + 1)")
                                    .font(.system(size: 14, weight: .medium))
                                    .foregroundColor(ColorTokens.textSecondary)
                                    .frame(width: 24, alignment: .trailing)
                                
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(song.title)
                                        .font(.system(size: 15, weight: .medium))
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
                            .padding(.horizontal, 24)
                            .padding(.vertical, 12)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                playerService.play(songs: songs, startIndex: index)
                                StorageManager.shared.recordPlayback(song: song, position: 0)
                            }
                            
                            Divider()
                                .background(ColorTokens.hairlineDivider)
                                .padding(.leading, 64)
                        }
                    }
                    .padding(.top, 10)
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }
    
    private func formatDuration(_ duration: TimeInterval) -> String {
        let m = Int(duration) / 60
        let s = Int(duration) % 60
        return String(format: "%d:%02d", m, s)
    }
}
