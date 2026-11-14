import SwiftUI

public struct MiniPlayerView: View {
    @ObservedObject var playerService = AudioPlayerService.shared
    var onExpand: () -> Void
    
    public var body: some View {
        if let song = playerService.currentSong {
            HStack(spacing: 12) {
                // Album Art
                RoundedRectangle(cornerRadius: 8)
                    .fill(ColorTokens.darkSurfaceVariant)
                    .frame(width: 48, height: 48)
                    .overlay(
                        Image(systemName: "music.note")
                            .foregroundColor(ColorTokens.primaryAccent)
                    )
                
                // Track Info
                VStack(alignment: .leading, spacing: 2) {
                    Text(song.title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(ColorTokens.textPrimary)
                        .lineLimit(1)
                    
                    Text(song.artist)
                        .font(.system(size: 13, weight: .regular))
                        .foregroundColor(ColorTokens.textSecondary)
                        .lineLimit(1)
                }
                
                Spacer()
                
                // Play / Pause Button
                Button(action: {
                    playerService.togglePlayPause()
                }) {
                    Image(systemName: playerService.isPlaying ? "pause.fill" : "play.fill")
                        .font(.system(size: 20))
                        .foregroundColor(ColorTokens.textPrimary)
                        .frame(width: 44, height: 44)
                }
                
                // Next Track Button
                Button(action: {
                    playerService.skipNext()
                }) {
                    Image(systemName: "forward.fill")
                        .font(.system(size: 20))
                        .foregroundColor(ColorTokens.textPrimary)
                        .frame(width: 44, height: 44)
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .liquidGlass(cornerRadius: 14)
            .padding(.horizontal, 12)
            .contentShape(Rectangle())
            .onTapGesture {
                onExpand()
            }
        }
    }
}

