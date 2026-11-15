import SwiftUI

public struct SongArtworkView: View {
    var artworkURL: URL?
    var size: CGFloat
    var cornerRadius: CGFloat = 8
    var iconSize: CGFloat = 20
    
    public init(
        artworkURL: URL?,
        size: CGFloat,
        cornerRadius: CGFloat = 8,
        iconSize: CGFloat = 20
    ) {
        self.artworkURL = artworkURL
        self.size = size
        self.cornerRadius = cornerRadius
        self.iconSize = iconSize
    }
    
    public var body: some View {
        Group {
            if let url = artworkURL, let img = UIImage(contentsOfFile: url.path) {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFill()
                    .frame(width: size, height: size)
                    .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
                    .overlay(
                        RoundedRectangle(cornerRadius: cornerRadius)
                            .stroke(Color.white.opacity(0.1), lineWidth: 0.5)
                    )
            } else {
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(ColorTokens.darkSurfaceVariant)
                    .frame(width: size, height: size)
                    .overlay(
                        Image(systemName: "music.note")
                            .font(.system(size: iconSize))
                            .foregroundColor(ColorTokens.primaryAccent)
                    )
            }
        }
        .frame(width: size, height: size)
    }
}
