import SwiftUI

public struct PlaylistsView: View {
    @ObservedObject var storage = StorageManager.shared
    @State private var showingCreateAlert = false
    @State private var newPlaylistName = ""
    
    public var body: some View {
        ZStack {
            ColorTokens.amoledBlack.ignoresSafeArea()
            
            List {
                ForEach(storage.playlists) { playlist in
                    HStack(spacing: 14) {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(ColorTokens.darkSurfaceVariant)
                            .frame(width: 50, height: 50)
                            .overlay(
                                Image(systemName: "music.note.list")
                                    .font(.system(size: 22))
                                    .foregroundColor(ColorTokens.primaryAccent)
                            )
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text(playlist.name)
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(ColorTokens.textPrimary)
                            
                            Text("\(playlist.songIds.count) bài hát")
                                .font(.system(size: 13))
                                .foregroundColor(ColorTokens.textSecondary)
                        }
                        
                        Spacer()
                    }
                    .listRowBackground(ColorTokens.amoledBlack)
                }
                .onDelete { indexSet in
                    for index in indexSet {
                        let p = storage.playlists[index]
                        storage.deletePlaylist(id: p.id)
                    }
                }
                
                Spacer()
                    .frame(height: 70)
                    .listRowBackground(Color.clear)
            }
            .listStyle(.plain)
            .background(ColorTokens.amoledBlack)
        }
        .navigationTitle("Danh sách phát")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    newPlaylistName = ""
                    showingCreateAlert = true
                }) {
                    Image(systemName: "plus")
                        .foregroundColor(ColorTokens.primaryAccent)
                }
            }
        }
        .alert("Tạo danh sách phát mới", isPresented: $showingCreateAlert) {
            TextField("Tên danh sách", text: $newPlaylistName)
            Button("Hủy", role: .cancel) {}
            Button("Tạo") {
                if !newPlaylistName.trimmingCharacters(in: .whitespaces).isEmpty {
                    storage.createPlaylist(name: newPlaylistName)
                }
            }
        }
    }
}
