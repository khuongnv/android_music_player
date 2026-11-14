import Foundation
import Combine

@MainActor
public class SleepTimerManager: ObservableObject {
    public static let shared = SleepTimerManager()
    
    @Published public private(set) var remainingSeconds: Int = 0
    @Published public private(set) var isActive: Bool = false
    
    private var timer: Timer?
    
    private init() {}
    
    public func startTimer(minutes: Int) {
        cancelTimer()
        guard minutes > 0 else { return }
        self.remainingSeconds = minutes * 60
        self.isActive = true
        
        self.timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            Task { @MainActor [weak self] in
                guard let self = self else { return }
                if self.remainingSeconds > 1 {
                    self.remainingSeconds -= 1
                } else {
                    self.completeTimer()
                }
            }
        }
    }
    
    public func cancelTimer() {
        timer?.invalidate()
        timer = nil
        isActive = false
        remainingSeconds = 0
    }
    
    private func completeTimer() {
        cancelTimer()
        // Tự động tạm dừng nhạc
        AudioPlayerService.shared.togglePlayPause()
    }
    
    public func formatRemainingTime() -> String {
        let mins = remainingSeconds / 60
        let secs = remainingSeconds % 60
        return String(format: "%02d:%02d", mins, secs)
    }
}
