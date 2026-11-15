import Foundation

@MainActor
public class StorageManager: ObservableObject {
    public static let shared = StorageManager()
    
    @Published public private(set) var playlists: [Playlist] = []
    @Published public private(set) var playbackHistory: [PlaybackHistoryRecord] = []
    
    private let playlistsFilename = "playlists.json"
    private let historyFilename = "playback_history.json"
    
    private init() {
        loadPlaylists()
        loadPlaybackHistory()
    }
    
    // MARK: - Playlists Management
    public func createPlaylist(name: String) {
        let newPlaylist = Playlist(name: name)
        playlists.append(newPlaylist)
        savePlaylists()
    }
    
    public func deletePlaylist(id: String) {
        playlists.removeAll { $0.id == id }
        savePlaylists()
    }
    
    public func addSongToPlaylist(playlistId: String, songId: String) {
        guard let index = playlists.firstIndex(where: { $0.id == playlistId }) else { return }
        if !playlists[index].songIds.contains(songId) {
            playlists[index].songIds.append(songId)
            savePlaylists()
        }
    }
    
    public func removeSongFromPlaylist(playlistId: String, songId: String) {
        guard let index = playlists.firstIndex(where: { $0.id == playlistId }) else { return }
        playlists[index].songIds.removeAll { $0 == songId }
        savePlaylists()
    }
    
    private func savePlaylists() {
        guard let url = getFileURL(filename: playlistsFilename) else { return }
        if let data = try? JSONEncoder().encode(playlists) {
            try? data.write(to: url)
        }
    }
    
    private func loadPlaylists() {
        guard let url = getFileURL(filename: playlistsFilename),
              let data = try? Data(contentsOf: url),
              let loaded = try? JSONDecoder().decode([Playlist].self, from: data) else {
            // Default sample playlists
            self.playlists = [
                Playlist(name: "Yêu thích", songIds: []),
                Playlist(name: "Nghe gần đây", songIds: [])
            ]
            return
        }
        self.playlists = loaded
    }
    
    // MARK: - 24h Playback History
    public func recordPlayback(song: Song, position: TimeInterval) {
        let record = PlaybackHistoryRecord(
            songId: song.id,
            songTitle: song.title,
            artistName: song.artist,
            albumName: song.album,
            duration: song.duration,
            timestamp: Date(),
            playbackPosition: position,
            artworkURL: song.artworkURL
        )
        // Lưu lịch sử trong vòng 24 giờ
        let twentyFourHoursAgo = Date().addingTimeInterval(-24 * 3600)
        var updated = playbackHistory.filter { $0.timestamp >= twentyFourHoursAgo }
        updated.insert(record, at: 0)
        self.playbackHistory = updated
        savePlaybackHistory()
    }
    
    private func savePlaybackHistory() {
        guard let url = getFileURL(filename: historyFilename) else { return }
        if let data = try? JSONEncoder().encode(playbackHistory) {
            try? data.write(to: url)
        }
    }
    
    private func loadPlaybackHistory() {
        guard let url = getFileURL(filename: historyFilename),
              let data = try? Data(contentsOf: url),
              let loaded = try? JSONDecoder().decode([PlaybackHistoryRecord].self, from: data) else {
            return
        }
        let twentyFourHoursAgo = Date().addingTimeInterval(-24 * 3600)
        self.playbackHistory = loaded.filter { $0.timestamp >= twentyFourHoursAgo }
    }
    
    private func getFileURL(filename: String) -> URL? {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.appendingPathComponent(filename)
    }
}
