import SwiftUI

public struct TimelineView: View {
    @ObservedObject var storage = StorageManager.shared
    @ObservedObject var playerService = AudioPlayerService.shared
    
    public var body: some View {
        NavigationStack {
            ZStack {
                ColorTokens.amoledBlack.ignoresSafeArea()
                
                if storage.playbackHistory.isEmpty {
                    VStack(spacing: 16) {
                        Image(systemName: "clock.arrow.circlepath")
                            .font(.system(size: 56))
                            .foregroundColor(ColorTokens.textSecondary)
                        Text("Chưa có ký ức phát nhạc")
                            .font(.title2.bold())
                            .foregroundColor(ColorTokens.textPrimary)
                        Text("Các bài hát bạn phát trong 24 giờ qua sẽ tự động lưu lại trên dòng thời gian tại đây.")
                            .font(.subheadline)
                            .foregroundColor(ColorTokens.textSecondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                    }
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 18) {
                            Text("Dòng thời gian 24 giờ")
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(ColorTokens.textPrimary)
                                .padding(.horizontal, 20)
                                .padding(.top, 16)
                            
                            ForEach(storage.playbackHistory) { record in
                                HStack(spacing: 16) {
                                    // Time indicator
                                    VStack(alignment: .trailing, spacing: 2) {
                                        Text(formatTime(record.timestamp))
                                            .font(.system(size: 13, weight: .semibold, design: .monospaced))
                                            .foregroundColor(ColorTokens.primaryAccent)
                                    }
                                    .frame(width: 50)
                                    
                                    // Timeline dot and line
                                    Circle()
                                        .fill(ColorTokens.primaryAccent)
                                        .frame(width: 8, height: 8)
                                    
                                    // Record Info Card
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(record.songTitle)
                                            .font(.system(size: 15, weight: .semibold))
                                            .foregroundColor(ColorTokens.textPrimary)
                                            .lineLimit(1)
                                        
                                        Text(record.artistName)
                                            .font(.system(size: 13))
                                            .foregroundColor(ColorTokens.textSecondary)
                                            .lineLimit(1)
                                    }
                                    .padding(.vertical, 10)
                                    .padding(.horizontal, 14)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .liquidGlass(cornerRadius: 12)
                                }
                                .padding(.horizontal, 20)
                            }
                        }
                        .padding(.bottom, 140)
                    }
                }
            }
            .navigationTitle("Ký ức")
        }
    }
    
    private func formatTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
}
