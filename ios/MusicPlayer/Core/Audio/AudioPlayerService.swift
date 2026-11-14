import Foundation
import AVFoundation
import MediaPlayer
import Combine

@MainActor
public class AudioPlayerService: ObservableObject {
    public static let shared = AudioPlayerService()
    
    @Published public private(set) var currentSong: Song?
    @Published public private(set) var isPlaying: Bool = false
    @Published public private(set) var currentTime: TimeInterval = 0
    @Published public private(set) var duration: TimeInterval = 0
    @Published public var shuffleEnabled: Bool = false
    @Published public var repeatMode: RepeatMode = .off
    
    public enum RepeatMode {
        case off, one, all
    }
    
    private var player: AVQueuePlayer?
    private var timeObserverToken: Any?
    private var cancellables = Set<AnyCancellable>()
    private var queue: [Song] = []
    private var originalQueue: [Song] = []
    
    private init() {
        setupAudioSession()
        setupRemoteCommandCenter()
    }
    
    // MARK: - Audio Session Setup
    private func setupAudioSession() {
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .default, policy: .longFormAudio)
            try session.setActive(true)
            
            NotificationCenter.default.addObserver(
                self,
                selector: #selector(handleInterruption),
                name: AVAudioSession.interruptionNotification,
                object: session
            )
            NotificationCenter.default.addObserver(
                self,
                selector: #selector(handleRouteChange),
                name: AVAudioSession.routeChangeNotification,
                object: session
            )
        } catch {
            print("Failed to setup AVAudioSession: \(error.localizedDescription)")
        }
    }
    
    // MARK: - Playback Controls
    public func play(songs: [Song], startIndex: Int = 0) {
        guard !songs.isEmpty, startIndex >= 0, startIndex < songs.count else { return }
        self.originalQueue = songs
        self.queue = shuffleEnabled ? songs.shuffled() : songs
        
        let targetSong = songs[startIndex]
        playSong(targetSong)
    }
    
    public func playSong(_ song: Song) {
        self.currentSong = song
        let playerItem = AVPlayerItem(url: song.fileURL)
        
        if player == nil {
            player = AVQueuePlayer(playerItem: playerItem)
            setupTimeObserver()
        } else {
            player?.replaceCurrentItem(with: playerItem)
        }
        
        player?.play()
        self.isPlaying = true
        self.duration = song.duration
        updateNowPlayingInfo()
    }
    
    public func togglePlayPause() {
        guard let player = player else { return }
        if isPlaying {
            player.pause()
            isPlaying = false
        } else {
            player.play()
            isPlaying = true
        }
        updateNowPlayingPlaybackState()
    }
    
    public func seek(to time: TimeInterval) {
        let cmTime = CMTime(seconds: time, preferredTimescale: 600)
        player?.seek(to: cmTime) { [weak self] _ in
            Task { @MainActor in
                self?.updateNowPlayingPlaybackState()
            }
        }
    }
    
    public func skipNext() {
        guard let current = currentSong, let idx = queue.firstIndex(of: current) else { return }
        let nextIndex = idx + 1
        if nextIndex < queue.count {
            playSong(queue[nextIndex])
        } else if repeatMode == .all {
            if let first = queue.first { playSong(first) }
        }
    }
    
    public func skipPrevious() {
        if currentTime > 3.0 {
            seek(to: 0)
            return
        }
        guard let current = currentSong, let idx = queue.firstIndex(of: current) else { return }
        let prevIndex = idx - 1
        if prevIndex >= 0 {
            playSong(queue[prevIndex])
        } else {
            seek(to: 0)
        }
    }
    
    // MARK: - Periodic Time Observer
    private func setupTimeObserver() {
        let interval = CMTime(seconds: 0.5, preferredTimescale: 600)
        timeObserverToken = player?.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] time in
            Task { @MainActor in
                self?.currentTime = time.seconds
            }
        }
    }
    
    // MARK: - MPRemoteCommandCenter (Lock Screen / Control Center)
    private func setupRemoteCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        
        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.togglePlayPause()
            return .success
        }
        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.togglePlayPause()
            return .success
        }
        commandCenter.nextTrackCommand.addTarget { [weak self] _ in
            self?.skipNext()
            return .success
        }
        commandCenter.previousTrackCommand.addTarget { [weak self] _ in
            self?.skipPrevious()
            return .success
        }
        commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            if let positionEvent = event as? MPChangePlaybackPositionCommandEvent {
                self?.seek(to: positionEvent.positionTime)
                return .success
            }
            return .commandFailed
        }
    }
    
    // MARK: - MPNowPlayingInfoCenter
    private func updateNowPlayingInfo() {
        guard let song = currentSong else { return }
        var info = [String: Any]()
        info[MPMediaItemPropertyTitle] = song.title
        info[MPMediaItemPropertyArtist] = song.artist
        info[MPMediaItemPropertyAlbumTitle] = song.album
        info[MPMediaItemPropertyPlaybackDuration] = song.duration
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentTime
        info[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        
        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }
    
    private func updateNowPlayingPlaybackState() {
        guard var info = MPNowPlayingInfoCenter.default().nowPlayingInfo else { return }
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentTime
        info[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }
    
    // MARK: - Audio Interruption / Route Change
    @objc private func handleInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
              let type = AVAudioSession.InterruptionType(rawValue: typeValue) else { return }
        
        if type == .began {
            player?.pause()
            isPlaying = false
        } else if type == .ended {
            if let optionsValue = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt {
                let options = AVAudioSession.InterruptionOptions(rawValue: optionsValue)
                if options.contains(.shouldResume) {
                    player?.play()
                    isPlaying = true
                }
            }
        }
    }
    
    @objc private func handleRouteChange(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let reasonValue = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
              let reason = AVAudioSession.RouteChangeReason(rawValue: reasonValue) else { return }
        
        if reason == .oldDeviceUnavailable {
            // Tai nghe bị rút ra -> dừng phát nhạc
            player?.pause()
            isPlaying = false
        }
    }
}

