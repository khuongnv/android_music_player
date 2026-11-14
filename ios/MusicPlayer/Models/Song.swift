import Foundation

public struct Song: Identifiable, Hashable, Codable {
    public let id: String
    public let title: String
    public let artist: String
    public let album: String
    public let duration: TimeInterval
    public let fileURL: URL
    public let artworkURL: URL?
    public let dateAdded: Date
    
    public init(
        id: String = UUID().uuidString,
        title: String,
        artist: String = "Unknown Artist",
        album: String = "Unknown Album",
        duration: TimeInterval = 0,
        fileURL: URL,
        artworkURL: URL? = nil,
        dateAdded: Date = Date()
    ) {
        self.id = id
        self.title = title
        self.artist = artist
        self.album = album
        self.duration = duration
        self.fileURL = fileURL
        self.artworkURL = artworkURL
        self.dateAdded = dateAdded
    }
}

