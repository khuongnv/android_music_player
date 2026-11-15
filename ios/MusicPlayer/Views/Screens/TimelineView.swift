import SwiftUI

public struct TimelineView: View {
    @ObservedObject var storage = StorageManager.shared
    @ObservedObject var playerService = AudioPlayerService.shared
    
    // Interactive Scrubber State
    @State private var scrubFraction: Double = 1.0
    
    private var history: [PlaybackHistoryRecord] {
        storage.playbackHistory
    }
    
    // Tổng thời lượng nghe trong 24h
    private var totalListeningSeconds: TimeInterval {
        history.reduce(0) { total, record in
            total + (record.duration > 0 ? min(record.duration, 300) : 180)
        }
    }
    
    // Thời điểm tương ứng với vị trí con trỏ Scrubber (24h qua)
    private var selectedTimestamp: Date {
        let now = Date()
        let start = now.addingTimeInterval(-24 * 3600)
        let timeOffset = (24 * 3600) * scrubFraction
        return start.addingTimeInterval(timeOffset)
    }
    
    // Bài hát gần nhất với vị trí con trỏ
    private var recordAtCursor: PlaybackHistoryRecord? {
        guard !history.isEmpty else { return nil }
        let targetTime = selectedTimestamp.timeIntervalSince1970
        return history.min(by: { abs($0.timestamp.timeIntervalSince1970 - targetTime) < abs($1.timestamp.timeIntervalSince1970 - targetTime) })
    }
    
    public var body: some View {
        NavigationStack {
            ZStack {
                ColorTokens.amoledBlack.ignoresSafeArea()
                
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        // 1. Apple Music Large Subtitle
                        Text("Dòng thời gian và nhật ký âm nhạc trong 24 giờ qua")
                            .font(.system(size: 14))
                            .foregroundColor(ColorTokens.textSecondary)
                            .padding(.horizontal, 20)
                            .padding(.top, 4)
                        
                        // 2. Overview Summary Card
                        overviewCard
                            .padding(.horizontal, 16)
                        
                        // 3. Interactive 24-Hour Timeline Bar & Scrubber
                        VStack(alignment: .leading, spacing: 8) {
                            Text("DÒNG THỜI GIAN KÝ ỨC (24H)")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(ColorTokens.textSecondary)
                                .padding(.horizontal, 20)
                            
                            interactiveTimelineBar
                                .padding(.horizontal, 16)
                        }
                        
                        // 4. Point Inspector Card (Hiển thị chi tiết tại con trỏ)
                        if let inspected = recordAtCursor {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("CHI TIẾT ĐIỂM KÝ ỨC")
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(ColorTokens.textSecondary)
                                    .padding(.horizontal, 20)
                                
                                inspectorCard(record: inspected)
                                    .padding(.horizontal, 16)
                            }
                        }
                        
                        // 5. Recent 24h Activity Sessions List
                        VStack(alignment: .leading, spacing: 12) {
                            Text("KÝ ỨC PHÁT NHẠC GẦN ĐÂY")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(ColorTokens.textSecondary)
                                .padding(.horizontal, 20)
                            
                            if history.isEmpty {
                                emptyHistoryCard
                                    .padding(.horizontal, 16)
                            } else {
                                LazyVStack(spacing: 10) {
                                    ForEach(history) { record in
                                        activityRow(record: record)
                                    }
                                }
                                .padding(.horizontal, 16)
                            }
                        }
                        
                        Spacer()
                            .frame(height: 140)
                    }
                }
            }
            .navigationTitle("Ký ức")
        }
    }
    
    // MARK: - Overview Summary Card
    private var overviewCard: some View {
        let totalMinutes = Int(totalListeningSeconds / 60)
        let hours = totalMinutes / 60
        let mins = totalMinutes % 60
        let formattedTime = hours > 0 ? "\(hours) giờ \(mins) phút" : "\(mins) phút"
        let percentOfDay = String(format: "%.1f%%", (totalListeningSeconds / (24 * 3600)) * 100)
        
        return HStack(spacing: 16) {
            // Icon Badge
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(ColorTokens.primaryAccent)
                    .frame(width: 44, height: 44)
                Image(systemName: "clock.badge.checkmark.fill")
                    .font(.system(size: 22))
                    .foregroundColor(.white)
            }
            
            VStack(alignment: .leading, spacing: 3) {
                Text("Tổng thời gian nghe (24h)")
                    .font(.system(size: 13))
                    .foregroundColor(ColorTokens.textSecondary)
                
                Text(formattedTime)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(ColorTokens.primaryAccent)
            }
            
            Spacer()
            
            VStack(alignment: .trailing, spacing: 4) {
                Text("\(history.count) phiên")
                    .font(.system(size: 13, weight: .semibold))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(ColorTokens.primaryAccent.opacity(0.18))
                    .foregroundColor(ColorTokens.primaryAccent)
                    .cornerRadius(8)
                
                Text("\(percentOfDay) trong ngày")
                    .font(.system(size: 11))
                    .foregroundColor(ColorTokens.textSecondary)
            }
        }
        .padding(16)
        .liquidGlass(cornerRadius: 18)
    }
    
    // MARK: - Interactive 24-Hour Timeline Bar & Scrubber
    private var interactiveTimelineBar: some View {
        VStack(spacing: 8) {
            // Thanh bar 24h có thể kéo tương tác
            GeometryReader { geo in
                let width = geo.size.width
                ZStack(alignment: .leading) {
                    // Thanh nền tiến trình
                    RoundedRectangle(cornerRadius: 8)
                        .fill(ColorTokens.darkSurfaceVariant)
                        .frame(height: 24)
                    
                    // Các điểm phát nhạc rải rác trên thanh 24h
                    ForEach(history) { rec in
                        let recordFrac = calculateTimeFraction(rec.timestamp)
                        Circle()
                            .fill(ColorTokens.primaryAccent.opacity(0.85))
                            .frame(width: 8, height: 8)
                            .offset(x: max(0, min(width - 8, width * recordFrac - 4)))
                    }
                    
                    // Con trỏ Scrubber Thumb kéo được
                    Circle()
                        .fill(Color.white)
                        .frame(width: 22, height: 22)
                        .shadow(color: Color.black.opacity(0.4), radius: 4)
                        .overlay(Circle().stroke(ColorTokens.primaryAccent, lineWidth: 3))
                        .offset(x: max(0, min(width - 22, width * scrubFraction - 11)))
                }
                .frame(height: 24)
                .contentShape(Rectangle())
                .gesture(
                    DragGesture(minimumDistance: 0)
                        .onChanged { val in
                            let clampedX = max(0, min(width, val.location.x))
                            scrubFraction = clampedX / width
                        }
                )
            }
            .frame(height: 24)
            
            // Các mốc thời gian hiển thị (0h, 6h, 12h, 18h, Bây giờ)
            HStack {
                Text("24h trước")
                Spacer()
                Text("18h")
                Spacer()
                Text("12h")
                Spacer()
                Text("6h")
                Spacer()
                Text("Bây giờ")
            }
            .font(.system(size: 11, weight: .medium, design: .monospaced))
            .foregroundColor(ColorTokens.textSecondary)
        }
        .padding(14)
        .liquidGlass(cornerRadius: 16)
    }
    
    // MARK: - Selected Point Inspector Card
    @ViewBuilder
    private func inspectorCard(record: PlaybackHistoryRecord) -> some View {
        HStack(spacing: 14) {
            SongArtworkView(
                artworkURL: record.artworkURL,
                size: 52,
                cornerRadius: 10,
                iconSize: 22
            )
            
            VStack(alignment: .leading, spacing: 3) {
                Text(record.songTitle)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(ColorTokens.textPrimary)
                    .lineLimit(1)
                
                Text(record.artistName)
                    .font(.system(size: 13))
                    .foregroundColor(ColorTokens.textSecondary)
                    .lineLimit(1)
                
                Text("Đã nghe lúc: \(formatFullTime(record.timestamp))")
                    .font(.system(size: 11))
                    .foregroundColor(ColorTokens.primaryAccent)
            }
            
            Spacer()
            
            Button(action: {
                playRecord(record)
            }) {
                Image(systemName: "play.circle.fill")
                    .font(.system(size: 36))
                    .foregroundColor(ColorTokens.primaryAccent)
            }
        }
        .padding(14)
        .liquidGlass(cornerRadius: 16)
    }
    
    // MARK: - Activity Row Item
    private func activityRow(record: PlaybackHistoryRecord) -> some View {
        HStack(spacing: 12) {
            // Mốc giờ phát
            Text(formatTimeOnly(record.timestamp))
                .font(.system(size: 13, weight: .bold, design: .monospaced))
                .foregroundColor(ColorTokens.primaryAccent)
                .frame(width: 44, alignment: .leading)
            
            // Ảnh bìa bài hát
            SongArtworkView(
                artworkURL: record.artworkURL,
                size: 44,
                cornerRadius: 8,
                iconSize: 20
            )
            
            // Thông tin bài hát
            VStack(alignment: .leading, spacing: 2) {
                Text(record.songTitle)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(ColorTokens.textPrimary)
                    .lineLimit(1)
                
                Text("\(record.artistName) • \(record.albumName)")
                    .font(.system(size: 12))
                    .foregroundColor(ColorTokens.textSecondary)
                    .lineLimit(1)
            }
            
            Spacer()
            
            // Nút phát nhanh
            Button(action: {
                playRecord(record)
            }) {
                Image(systemName: "play.fill")
                    .font(.system(size: 15))
                    .foregroundColor(ColorTokens.primaryAccent)
                    .frame(width: 32, height: 32)
                    .background(ColorTokens.primaryAccent.opacity(0.12))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .liquidGlass(cornerRadius: 14)
    }
    
    // MARK: - Empty History Card
    private var emptyHistoryCard: some View {
        VStack(spacing: 12) {
            Image(systemName: "clock.arrow.circlepath")
                .font(.system(size: 38))
                .foregroundColor(ColorTokens.primaryAccent.opacity(0.7))
                .padding(.top, 8)
            
            Text("Chưa có ký ức phát nhạc trong 24 giờ qua")
                .font(.system(size: 15, weight: .semibold))
                .foregroundColor(ColorTokens.textPrimary)
            
            Text("Hãy phát một bài hát từ Thư viện để bắt đầu lưu giữ khoảnh khắc và dòng thời gian âm nhạc của bạn.")
                .font(.system(size: 13))
                .foregroundColor(ColorTokens.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 20)
                .padding(.bottom, 8)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
        .liquidGlass(cornerRadius: 16)
    }
    
    // MARK: - Helper Methods
    private func calculateTimeFraction(_ date: Date) -> Double {
        let now = Date()
        let start = now.addingTimeInterval(-24 * 3600)
        let totalSpan = now.timeIntervalSince(start)
        let elapsed = date.timeIntervalSince(start)
        return max(0, min(1, elapsed / totalSpan))
    }
    
    private func formatTimeOnly(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
    
    private func formatFullTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm:ss"
        return formatter.string(from: date)
    }
    
    private func playRecord(_ record: PlaybackHistoryRecord) {
        // Tìm bài hát tương ứng trong danh sách để phát
        Task {
            let docSongs = await LocalAudioScanner.shared.scanDocumentsDirectory()
            let mediaSongs = await LocalAudioScanner.shared.scanNativeMediaLibrary()
            let all = docSongs + mediaSongs
            if let target = all.first(where: { $0.id == record.songId || $0.title == record.songTitle }) {
                playerService.playSong(target)
                if record.playbackPosition > 0 {
                    playerService.seek(to: record.playbackPosition)
                }
            }
        }
    }
}
