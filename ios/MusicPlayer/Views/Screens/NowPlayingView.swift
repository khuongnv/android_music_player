import SwiftUI

public struct NowPlayingView: View {
    @ObservedObject var playerService = AudioPlayerService.shared
    @ObservedObject var sleepTimer = SleepTimerManager.shared
    @Environment(\.dismiss) private var dismiss
    @State private var showingSleepTimerSheet = false
    
    public var body: some View {
        ZStack {
            // Dynamic Blurred Artwork Background
            if let song = playerService.currentSong,
               let artURL = song.artworkURL,
               let img = UIImage(contentsOfFile: artURL.path) {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFill()
                    .ignoresSafeArea()
                    .blur(radius: 60)
                    .overlay(Color.black.opacity(0.68))
            } else {
                ColorTokens.amoledBlack.ignoresSafeArea()
            }
            
            VStack(spacing: 20) {
                // Top Header: Dismiss Button (Chevron Down), Grabber & Sleep Timer
                HStack(alignment: .center) {
                    Button(action: { dismiss() }) {
                        Image(systemName: "chevron.down")
                            .font(.system(size: 17, weight: .bold))
                            .foregroundColor(ColorTokens.textPrimary)
                            .frame(width: 38, height: 38)
                            .background(Color.white.opacity(0.12))
                            .clipShape(Circle())
                    }
                    
                    Spacer()
                    
                    Capsule()
                        .fill(Color.white.opacity(0.35))
                        .frame(width: 40, height: 5)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            dismiss()
                        }
                    
                    Spacer()
                    
                    // Sleep Timer Quick Button
                    Button(action: { showingSleepTimerSheet = true }) {
                        HStack(spacing: 4) {
                            Image(systemName: "moon.fill")
                            if sleepTimer.isActive {
                                Text(sleepTimer.formatRemainingTime())
                                    .font(.system(size: 12, weight: .semibold, design: .monospaced))
                            }
                        }
                        .font(.system(size: 14))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 7)
                        .background(sleepTimer.isActive ? ColorTokens.primaryAccent : Color.white.opacity(0.12))
                        .foregroundColor(sleepTimer.isActive ? .white : ColorTokens.textPrimary)
                        .cornerRadius(14)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 14)
                
                Spacer()
                
                // Big Album Artwork with Spring Scale
                if let song = playerService.currentSong {
                    SongArtworkView(
                        artworkURL: song.artworkURL,
                        size: 280,
                        cornerRadius: 24,
                        iconSize: 80
                    )
                    .scaleEffect(playerService.isPlaying ? 1.0 : 0.90)
                    .animation(.spring(response: 0.45, dampingFraction: 0.7), value: playerService.isPlaying)
                    .shadow(color: Color.black.opacity(0.6), radius: 30, x: 0, y: 16)
                }
                
                Spacer()
                
                // Song Metadata
                if let song = playerService.currentSong {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(song.title)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(ColorTokens.textPrimary)
                            .lineLimit(1)
                        
                        Text(song.artist)
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(ColorTokens.primaryAccent)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 32)
                }
                
                // Scrubber / Slider
                VStack(spacing: 8) {
                    Slider(
                        value: Binding(
                            get: { playerService.currentTime },
                            set: { newValue in playerService.seek(to: newValue) }
                        ),
                        in: 0...max(playerService.duration, 1)
                    )
                    .accentColor(ColorTokens.primaryAccent)
                    
                    HStack {
                        Text(formatTime(playerService.currentTime))
                            .font(.system(size: 12, weight: .medium, design: .monospaced))
                            .foregroundColor(ColorTokens.textSecondary)
                        Spacer()
                        Text("-" + formatTime(max(0, playerService.duration - playerService.currentTime)))
                            .font(.system(size: 12, weight: .medium, design: .monospaced))
                            .foregroundColor(ColorTokens.textSecondary)
                    }
                }
                .padding(.horizontal, 32)
                
                // Playback Control Buttons
                HStack(spacing: 40) {
                    Button(action: {
                        playerService.shuffleEnabled.toggle()
                    }) {
                        Image(systemName: "shuffle")
                            .font(.system(size: 20))
                            .foregroundColor(playerService.shuffleEnabled ? ColorTokens.primaryAccent : ColorTokens.textSecondary)
                    }
                    
                    Button(action: { playerService.skipPrevious() }) {
                        Image(systemName: "backward.fill")
                            .font(.system(size: 28))
                            .foregroundColor(ColorTokens.textPrimary)
                    }
                    
                    Button(action: { playerService.togglePlayPause() }) {
                        Image(systemName: playerService.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                            .font(.system(size: 64))
                            .foregroundColor(ColorTokens.primaryAccent)
                    }
                    
                    Button(action: { playerService.skipNext() }) {
                        Image(systemName: "forward.fill")
                            .font(.system(size: 28))
                            .foregroundColor(ColorTokens.textPrimary)
                    }
                    
                    Button(action: {
                        switch playerService.repeatMode {
                        case .off: playerService.repeatMode = .all
                        case .all: playerService.repeatMode = .one
                        case .one: playerService.repeatMode = .off
                        }
                    }) {
                        Image(systemName: playerService.repeatMode == .one ? "repeat.1" : "repeat")
                            .font(.system(size: 20))
                            .foregroundColor(playerService.repeatMode != .off ? ColorTokens.primaryAccent : ColorTokens.textSecondary)
                    }
                }
                .padding(.bottom, 36)
            }
        }
        .gesture(
            DragGesture().onEnded { val in
                if val.translation.height > 80 {
                    dismiss()
                }
            }
        )
        .sheet(isPresented: $showingSleepTimerSheet) {
            SleepTimerSheet()
                .presentationDetents([.fraction(0.4)])
        }
    }
    
    private func formatTime(_ time: TimeInterval) -> String {
        let mins = Int(time) / 60
        let secs = Int(time) % 60
        return String(format: "%02d:%02d", mins, secs)
    }
}

struct SleepTimerSheet: View {
    @ObservedObject var timer = SleepTimerManager.shared
    @Environment(\.dismiss) private var dismiss
    
    let intervals = [15, 30, 45, 60]
    
    var body: some View {
        ZStack {
            ColorTokens.darkSurface.ignoresSafeArea()
            
            VStack(spacing: 16) {
                Text("Hẹn giờ tắt nhạc")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(ColorTokens.textPrimary)
                    .padding(.top, 16)
                
                HStack(spacing: 12) {
                    ForEach(intervals, id: \.self) { mins in
                        Button("\(mins)m") {
                            timer.startTimer(minutes: mins)
                            dismiss()
                        }
                        .font(.system(size: 15, weight: .semibold))
                        .padding(.vertical, 10)
                        .padding(.horizontal, 16)
                        .background(ColorTokens.darkSurfaceVariant)
                        .foregroundColor(ColorTokens.textPrimary)
                        .cornerRadius(10)
                    }
                }
                
                if timer.isActive {
                    Button("Hủy hẹn giờ (\(timer.formatRemainingTime()))") {
                        timer.cancelTimer()
                        dismiss()
                    }
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(Color.red)
                    .padding(.top, 8)
                }
            }
            .padding(.horizontal, 20)
        }
    }
}
