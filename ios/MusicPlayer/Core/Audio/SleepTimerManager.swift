import Foundation
import Combine

@MainActor
public class SleepTimerManager: ObservableObject {
    public static let shared = SleepTimerManager()
    
    @Published public private(set) var remainingSeconds: Int = 0
    @Published public private(set) var isActive: Bool = false
    @Published public var selectedPresetMinutes: Int = 30
    @Published public private(set) var targetEndTime: Date? = nil
    
    private var timer: Timer?
    
    private init() {
        let savedMinutes = UserDefaults.standard.integer(forKey: "sleep_timer_preset_minutes")
        if savedMinutes > 0 {
            self.selectedPresetMinutes = savedMinutes
        }
    }
    
    public func startTimer(minutes: Int) {
        cancelTimer()
        guard minutes > 0 else { return }
        self.selectedPresetMinutes = minutes
        UserDefaults.standard.set(minutes, forKey: "sleep_timer_preset_minutes")
        
        self.remainingSeconds = minutes * 60
        self.targetEndTime = Date().addingTimeInterval(TimeInterval(minutes * 60))
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
        targetEndTime = nil
    }
    
    private func completeTimer() {
        cancelTimer()
        AudioPlayerService.shared.pause()
    }
    
    public func formatRemainingTime() -> String {
        let mins = remainingSeconds / 60
        let secs = remainingSeconds % 60
        return String(format: "%02d:%02d", mins, secs)
    }
    
    public func formatTargetEndTime() -> String {
        guard let targetEndTime = targetEndTime else { return "" }
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: targetEndTime)
    }
}
