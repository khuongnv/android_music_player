import Foundation

public struct PlaybackHistoryRecord: Identifiable, Hashable, Codable {
    public let id: String
    public let songId: String
    public let songTitle: String
    public let artistName: String
    public let albumName: String
    public let duration: TimeInterval
    public let timestamp: Date
    public let playbackPosition: TimeInterval
    public let artworkURL: URL?
    
    public init(
        id: String = UUID().uuidString,
        songId: String,
        songTitle: String,
        artistName: String,
        albumName: String = "Tập tin ngoại tuyến",
        duration: TimeInterval = 0,
        timestamp: Date = Date(),
        playbackPosition: TimeInterval = 0,
        artworkURL: URL? = nil
    ) {
        self.id = id
        self.songId = songId
        self.songTitle = songTitle
        self.artistName = artistName
        self.albumName = albumName
        self.duration = duration
        self.timestamp = timestamp
        self.playbackPosition = playbackPosition
        self.artworkURL = artworkURL
    }
}
