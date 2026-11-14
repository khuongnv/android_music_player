import Foundation

public struct Album: Identifiable, Hashable, Codable {
    public let id: String
    public let title: String
    public let artist: String
    public let artworkURL: URL?
    public let songCount: Int
    
    public init(
        id: String = UUID().uuidString,
        title: String,
        artist: String = "Unknown Artist",
        artworkURL: URL? = nil,
        songCount: Int = 0
    ) {
        self.id = id
        self.title = title
        self.artist = artist
        self.artworkURL = artworkURL
        self.songCount = songCount
    }
}

