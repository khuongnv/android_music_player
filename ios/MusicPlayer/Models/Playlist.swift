import Foundation

public struct Artist: Identifiable, Hashable, Codable {
    public let id: String
    public let name: String
    public let trackCount: Int
    
    public init(id: String = UUID().uuidString, name: String, trackCount: Int = 0) {
        self.id = id
        self.name = name
        self.trackCount = trackCount
    }
}

public struct Playlist: Identifiable, Hashable, Codable {
    public let id: String
    public var name: String
    public var songIds: [String]
    public let createdAt: Date
    
    public init(
        id: String = UUID().uuidString,
        name: String,
        songIds: [String] = [],
        createdAt: Date = Date()
    ) {
        self.id = id
        self.name = name
        self.songIds = songIds
        self.createdAt = createdAt
    }
}

