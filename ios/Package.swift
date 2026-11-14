// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "MusicPlayer",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(name: "MusicPlayer", targets: ["MusicPlayer"])
    ],
    targets: [
        .target(
            name: "MusicPlayer",
            path: "MusicPlayer"
        )
    ]
)
