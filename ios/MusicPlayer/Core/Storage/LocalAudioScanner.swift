import Foundation
import MediaPlayer
import AVFoundation
import UniformTypeIdentifiers

public class LocalAudioScanner {
    public static let shared = LocalAudioScanner()
    
    private init() {}
    
    // MARK: - 1. Cấp quyền truy cập Thư viện Apple Music của máy
    public func requestMediaPermissionIfNeeded() async -> Bool {
        let status = MPMediaLibrary.authorizationStatus()
        switch status {
        case .authorized:
            return true
        case .notDetermined:
            return await withCheckedContinuation { continuation in
                MPMediaLibrary.requestAuthorization { newStatus in
                    continuation.resume(returning: newStatus == .authorized)
                }
            }
        default:
            return false
        }
    }
    
    // MARK: - 2. Nhập file nhạc từ ứng dụng Tệp (Files / iCloud / Downloads)
    public func importAudioFiles(from urls: [URL]) async -> Int {
        guard let documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return 0
        }
        
        var importedCount = 0
        for sourceURL in urls {
            let shouldStopAccessing = sourceURL.startAccessingSecurityScopedResource()
            defer {
                if shouldStopAccessing {
                    sourceURL.stopAccessingSecurityScopedResource()
                }
            }
            
            let destURL = documentsURL.appendingPathComponent(sourceURL.lastPathComponent)
            do {
                if FileManager.default.fileExists(atPath: destURL.path) {
                    try FileManager.default.removeItem(at: destURL)
                }
                try FileManager.default.copyItem(at: sourceURL, to: destURL)
                importedCount += 1
            } catch {
                print("Lỗi sao chép tệp âm thanh: \(error)")
            }
        }
        return importedCount
    }
    
    // MARK: - 3. Quét tệp từ thư mục Documents (hỗ trợ kéo thả qua iTunes / Files)
    public func scanDocumentsDirectory() async -> [Song] {
        var songs: [Song] = []
        guard let documentsURL = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            return songs
        }
        
        let supportedExtensions = ["mp3", "m4a", "flac", "wav", "aac", "alac", "aiff", "ogg"]
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
            var durationSeconds = CMTimeGetSeconds(durationValue ?? .zero)
            if durationSeconds.isNaN || durationSeconds <= 0 {
                durationSeconds = 180.0
            }
            
            // Bỏ qua các hiệu ứng âm thanh quá ngắn (< 3 giây)
            if durationSeconds > 0 && durationSeconds < 3.0 { continue }
            if durationSeconds <= 0 { durationSeconds = 180.0 }
            
            var title = fileURL.deletingPathExtension().lastPathComponent
            var artist = "Không xác định"
            var album = "Tệp ngoại tuyến"
            
            if let metadata = try? await asset.load(.commonMetadata) {
                for item in metadata {
                    guard let commonKey = item.commonKey?.rawValue, let value = try? await item.load(.stringValue) else { continue }
                    switch commonKey {
                    case "title": if !value.isEmpty { title = value }
                    case "artist": if !value.isEmpty { artist = value }
                    case "albumName": if !value.isEmpty { album = value }
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
    
    // MARK: - 4. Quét nhạc từ Thư viện Apple Music của máy (MPMediaQuery)
    public func scanNativeMediaLibrary() async -> [Song] {
        var songs: [Song] = []
        
        let isAuthorized = await requestMediaPermissionIfNeeded()
        guard isAuthorized else {
            print("Chưa được cấp quyền truy cập Apple Media Library.")
            return songs
        }
        
        let query = MPMediaQuery.songs()
        guard let items = query.items else { return songs }
        
        for item in items {
            // Chỉ lấy các bài hát ngoại tuyến có assetURL phát được
            guard let url = item.assetURL else { continue }
            
            let song = Song(
                id: String(item.persistentID),
                title: item.title ?? "Không tên",
                artist: item.artist ?? "Không xác định",
                album: item.albumTitle ?? "Không xác định",
                duration: item.playbackDuration > 0 ? item.playbackDuration : 180.0,
                fileURL: url,
                artworkURL: nil
            )
            songs.append(song)
        }
        
        return songs
    }
}
