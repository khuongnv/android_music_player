import Foundation
import UIKit
import SwiftUI

public struct GitHubReleaseInfo: Identifiable, Codable {
    public var id: String { tagName }
    public let tagName: String
    public let name: String
    public let body: String
    public let publishedAt: String
    public let manifestURL: String
    public let otaInstallURL: String
    
    public var version: String {
        tagName.replacingOccurrences(of: "ios-v", with: "")
               .replacingOccurrences(of: "v", with: "")
               .trimmingCharacters(in: .whitespaces)
    }
}

@MainActor
public class InHouseUpdater: ObservableObject {
    public static let shared = InHouseUpdater()
    
    public var repoOwner: String = "khuongnv"
    public var repoName: String = "android-music-player"
    
    @Published public private(set) var isChecking: Bool = false
    @Published public private(set) var availableUpdate: GitHubReleaseInfo?
    @Published public var showUpdateAlert: Bool = false
    @Published public var errorMessage: String?
    
    private init() {}
    
    // MARK: - Kiểm tra bản cập nhật từ GitHub Releases
    public func checkForUpdates(silent: Bool = false) async {
        isChecking = true
        errorMessage = nil
        defer { isChecking = false }
        
        let apiURLString = "https://api.github.com/repos/\(repoOwner)/\(repoName)/releases?per_page=10"
        guard let url = URL(string: apiURLString) else { return }
        
        var request = URLRequest(url: url)
        request.setValue("MusicPlayer-iOS-Updater", forHTTPHeaderField: "User-Agent")
        request.setValue("application/vnd.github.v3+json", forHTTPHeaderField: "Accept")
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                if !silent { errorMessage = "Không thể kết nối đến máy chủ GitHub Releases." }
                return
            }
            
            guard let jsonArray = try JSONSerialization.jsonObject(with: data) as? [[String: Any]] else {
                return
            }
            
            let currentVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
            
            for item in jsonArray {
                guard let tagName = item["tag_name"] as? String else { continue }
                
                // Chỉ nhận các bản release dành cho iOS (tiền tố ios-v)
                if !tagName.hasPrefix("ios-v") { continue }
                
                let cleanTag = tagName.replacingOccurrences(of: "ios-v", with: "").trimmingCharacters(in: .whitespaces)
                
                // So sánh SemVer
                if isNewerVersion(remoteVersion: cleanTag, currentVersion: currentVersion) {
                    var manifestUrl: String?
                    if let assets = item["assets"] as? [[String: Any]] {
                        for asset in assets {
                            if let name = asset["name"] as? String, name.lowercased() == "manifest.plist",
                               let downloadUrl = asset["browser_download_url"] as? String {
                                manifestUrl = downloadUrl
                                break
                            }
                        }
                    }
                    
                    // Fallback URL nếu chưa lấy được từ assets
                    if manifestUrl == nil {
                        manifestUrl = "https://github.com/\(repoOwner)/\(repoName)/releases/download/\(tagName)/manifest.plist"
                    }
                    
                    guard let finalManifest = manifestUrl else { continue }
                    let otaLink = "itms-services://?action=download-manifest&url=\(finalManifest)"
                    
                    let releaseInfo = GitHubReleaseInfo(
                        tagName: tagName,
                        name: item["name"] as? String ?? tagName,
                        body: item["body"] as? String ?? "Bản cập nhật tối ưu hiệu năng và vá lỗi.",
                        publishedAt: item["published_at"] as? String ?? "",
                        manifestURL: finalManifest,
                        otaInstallURL: otaLink
                    )
                    
                    self.availableUpdate = releaseInfo
                    self.showUpdateAlert = true
                    return
                }
            }
            
            if !silent {
                // Đã là phiên bản mới nhất
                self.availableUpdate = nil
            }
        } catch {
            if !silent {
                self.errorMessage = "Lỗi kiểm tra cập nhật: \(error.localizedDescription)"
            }
        }
    }
    
    // MARK: - Kích hoạt cài đặt qua giao thức Apple itms-services
    public func triggerInstall() {
        guard let update = availableUpdate, let url = URL(string: update.otaInstallURL) else { return }
        if UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }
    
    // MARK: - So sánh Semantic Versioning (X.Y.Z)
    private func isNewerVersion(remoteVersion: String, currentVersion: String) -> Bool {
        let remoteParts = remoteVersion.split(separator: ".").compactMap { Int($0) }
        let currentParts = currentVersion.split(separator: ".").compactMap { Int($0) }
        
        let count = max(remoteParts.count, currentParts.count)
        for i in 0..<count {
            let r = i < remoteParts.count ? remoteParts[i] : 0
            let c = i < currentParts.count ? currentParts[i] : 0
            if r > c { return true }
            if r < c { return false }
        }
        return false
    }
}
