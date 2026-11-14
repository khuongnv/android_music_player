import SwiftUI

public struct NowPlayingView: View {
    @ObservedObject var playerService = AudioPlayerService.shared
    @ObservedObject var sleepTimer = SleepTimerManager.shared
    @Environment(\.dismiss) private var dismiss
    @State private var showingSleepTimerSheet = false
    
    public var body: some View {
        ZStack {
            // AMOLED Black Background
            ColorTokens.amoledBlack.ignoresSafeArea()
            
            VStack(spacing: 20) {
                // Top Grabber & Dismiss Bar
                Capsule()
                    .fill(Color.white.opacity(0.3))
                    .frame(width: 36, height: 5)
                    .padding(.top, 12)
                
                HStack {
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
                        .padding(.vertical, 6)
                        .background(sleepTimer.isActive ? ColorTokens.primaryAccent : ColorTokens.darkSurface)
                        .foregroundColor(sleepTimer.isActive ? .white : ColorTokens.textSecondary)
                        .cornerRadius(14)
                    }
                }
                .padding(.horizontal, 24)
                
                Spacer()
                
                // Big Album Artwork with Spring Scale
                RoundedRectangle(cornerRadius: 24)
                    .fill(ColorTokens.darkSurfaceVariant)
                    .aspectRatio(1, contentMode: .fit)
                    .padding(.horizontal, 32)
                    .overlay(
                        Image(systemName: "music.note")
                            .font(.system(size: 80))
                            .foregroundColor(ColorTokens.primaryAccent)
                    )
                    .scaleEffect(playerService.isPlaying ? 1.0 : 0.88)
                    .animation(.spring(response: 0.45, dampingFraction: 0.7), value: playerService.isPlaying)
                    .shadow(color: Color.black.opacity(0.5), radius: 25, x: 0, y: 15)
                
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
                            .foregroundColor(ColorTokens.textSecondary)
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
