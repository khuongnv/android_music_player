import Foundation
import MediaPlayer
import AVFoundation

public class LocalAudioScanner {
    public static let shared = LocalAudioScanner()
    
    private init() {}
    
    // MARK: - 1. Scan from App Documents Directory (Files App / iTunes Sharing)
    public func scanDocumentsDirectory() async -> [Song] {
        var songs: [Song] = []
        guard let documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return songs
        }
        
        let supportedExtensions = ["mp3", "m4a", "flac", "wav", "aac"]
        let fileManager = FileManager.default
        
        guard let enumerator = fileManager.enumerator(
            at: documentsURL,
            includingPropertiesForKeys: [.isRegularFileKey, .fileSizeKey, .contentModificationDateKey],
            options: [.skipsHiddenFiles, .skipsPackageDescendants]
        ) else { return songs }
        
        var targetURLs: [URL] = []
        while let fileURL = enumerator.nextObject() as? URL {
            targetURLs.append(fileURL)
        }
        
        for fileURL in targetURLs {
            guard supportedExtensions.contains(fileURL.pathExtension.lowercased()) else { continue }
            
            let asset = AVURLAsset(url: fileURL)
            let durationValue = try? await asset.load(.duration)
            let durationSeconds = CMTimeGetSeconds(durationValue ?? .zero)
            
            // Lọc bỏ file quá ngắn < 10 giây (tiếng chuông, thông báo)
            guard durationSeconds >= 10.0 else { continue }
            
            var title = fileURL.deletingPathExtension().lastPathComponent
            var artist = "Unknown Artist"
            var album = "Unknown Album"
            
            if let metadata = try? await asset.load(.commonMetadata) {
                for item in metadata {
                    guard let commonKey = item.commonKey?.rawValue, let value = try? await item.load(.stringValue) else { continue }
                    switch commonKey {
                    case "title": title = value
                    case "artist": artist = value
                    case "albumName": album = value
                    default: break
                    }
                }
            }
            
            let song = Song(
                id: fileURL.path,
                title: title,
                artist: artist,
                album: album,
                duration: durationSeconds,
                fileURL: fileURL,
                artworkURL: nil
            )
            songs.append(song)
        }
        
        return songs
    }
    
    // MARK: - 2. Scan from Native Apple Music Library (MPMediaQuery)
    public func scanNativeMediaLibrary() async -> [Song] {
        var songs: [Song] = []
        
        let status = MPMediaLibrary.authorizationStatus()
        guard status == .authorized else { return songs }
        
        let query = MPMediaQuery.songs()
        guard let items = query.items else { return songs }
        
        for item in items {
            // Chỉ lấy các bài có assetURL hợp lệ (loại trừ bài dính Apple Music DRM cloud)
            guard let url = item.assetURL else { continue }
            
            let song = Song(
                id: String(item.persistentID),
                title: item.title ?? "Unknown Title",
                artist: item.artist ?? "Unknown Artist",
                album: item.albumTitle ?? "Unknown Album",
                duration: item.playbackDuration,
                fileURL: url,
                artworkURL: nil
            )
            songs.append(song)
        }
        
        return songs
    }
}

