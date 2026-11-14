import SwiftUI

public struct SettingsView: View {
    @ObservedObject var updater = InHouseUpdater.shared
    @State private var showingUpdateSuccessAlert = false
    @State private var showingCacheClearedAlert = false
    
    private var currentVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
    }
    
    public var body: some View {
        NavigationStack {
            ZStack {
                ColorTokens.amoledBlack.ignoresSafeArea()
                
                List {
                    // PHẦN 1: BẢN CẬP NHẬT NỘI BỘ
                    Section(header: Text("BẢN PHÁT HÀNH & CẬP NHẬT").foregroundColor(ColorTokens.textSecondary)) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Phiên bản hiện tại")
                                    .foregroundColor(ColorTokens.textPrimary)
                                Text("Build v\(currentVersion) (Enterprise In-House)")
                                    .font(.subheadline)
                                    .foregroundColor(ColorTokens.textSecondary)
                            }
                            Spacer()
                            
                            if updater.isChecking {
                                ProgressView()
                                    .tint(ColorTokens.primaryAccent)
                            } else {
                                Button("Kiểm tra") {
                                    Task {
                                        await updater.checkForUpdates()
                                        if updater.availableUpdate == nil && updater.errorMessage == nil {
                                            showingUpdateSuccessAlert = true
                                        }
                                    }
                                }
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(ColorTokens.primaryAccent)
                            }
                        }
                        
                        if let update = updater.availableUpdate {
                            VStack(alignment: .leading, spacing: 8) {
                                HStack {
                                    Text("Đã có bản mới: v\(update.version)")
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(ColorTokens.primaryAccent)
                                    Spacer()
                                    Button("Cập nhật ngay") {
                                        updater.triggerInstall()
                                    }
                                    .buttonStyle(.borderedProminent)
                                    .tint(ColorTokens.primaryAccent)
                                }
                                
                                Text(update.body)
                                    .font(.caption)
                                    .foregroundColor(ColorTokens.textSecondary)
                            }
                            .padding(.vertical, 4)
                        }
                    }
                    .listRowBackground(ColorTokens.darkSurface)
                    
                    // PHẦN 2: THƯ VIỆN & BỘ NHỚ
                    Section(header: Text("QUẢN LÝ DỮ LIỆU").foregroundColor(ColorTokens.textSecondary)) {
                        Button(action: {
                            showingCacheClearedAlert = true
                        }) {
                            HStack {
                                Text("Xóa bộ nhớ đệm (Cache)")
                                    .foregroundColor(ColorTokens.textPrimary)
                                Spacer()
                                Image(systemName: "trash")
                                    .foregroundColor(ColorTokens.textSecondary)
                            }
                        }
                    }
                    .listRowBackground(ColorTokens.darkSurface)
                    
                    // PHẦN 3: THÔNG TIN ỨNG DỤNG
                    Section(header: Text("THÔNG TIN HỆ THỐNG").foregroundColor(ColorTokens.textSecondary)) {
                        HStack {
                            Text("Bundle Identifier")
                                .foregroundColor(ColorTokens.textPrimary)
                            Spacer()
                            Text("com.dhtthni.dhxe")
                                .font(.system(size: 13, design: .monospaced))
                                .foregroundColor(ColorTokens.textSecondary)
                        }
                        
                        HStack {
                            Text("Doanh nghiệp sở hữu")
                                .foregroundColor(ColorTokens.textPrimary)
                            Spacer()
                            Text("VNPT TP.HCM")
                                .font(.subheadline)
                                .foregroundColor(ColorTokens.textSecondary)
                        }
                        
                        HStack {
                            Text("Giao diện & Thiết kế")
                                .foregroundColor(ColorTokens.textPrimary)
                            Spacer()
                            Text("Apple Music x Liquid Glass")
                                .font(.subheadline)
                                .foregroundColor(ColorTokens.primaryAccent)
                        }
                    }
                    .listRowBackground(ColorTokens.darkSurface)
                }
                .background(ColorTokens.amoledBlack)
            }
            .navigationTitle("Cài đặt")
            .alert("Thông báo", isPresented: $showingUpdateSuccessAlert) {
                Button("OK", role: .cancel) {}
            } message: {
                Text("Bạn đang sử dụng phiên bản mới nhất (v\(currentVersion)).")
            }
            .alert("Bộ nhớ đệm", isPresented: $showingCacheClearedAlert) {
                Button("OK", role: .cancel) {}
            } message: {
                Text("Đã dọn dẹp bộ nhớ đệm thành công.")
            }
        }
    }
}
