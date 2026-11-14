import Foundation

public struct PlaybackHistoryRecord: Identifiable, Hashable, Codable {
    public let id: String
    public let songId: String
    public let songTitle: String
    public let artistName: String
    public let timestamp: Date
    public let playbackPosition: TimeInterval
    
    public init(
        id: String = UUID().uuidString,
        songId: String,
        songTitle: String,
        artistName: String,
        timestamp: Date = Date(),
        playbackPosition: TimeInterval = 0
    ) {
        self.id = id
        self.songId = songId
        self.songTitle = songTitle
        self.artistName = artistName
        self.timestamp = timestamp
        self.playbackPosition = playbackPosition
    }
}
