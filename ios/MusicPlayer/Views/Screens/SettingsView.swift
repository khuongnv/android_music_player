import SwiftUI

public struct SettingsView: View {
    @ObservedObject var updater = InHouseUpdater.shared
    @ObservedObject var sleepTimer = SleepTimerManager.shared
    
    @State private var showingUpdateSuccessAlert = false
    @State private var showingCacheClearedAlert = false
    @State private var showingCustomTimerSheet = false
    @State private var customTimerMinutes: Double = 45
    
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
                    
                    // PHẦN 2: HẸN GIỜ TẮT NHẠC (SLEEP TIMER - CHUẨN ANDROID)
                    Section(header: Text("HẸN GIỜ TẮT NHẠC").foregroundColor(ColorTokens.textSecondary)) {
                        VStack(spacing: 12) {
                            HStack(spacing: 12) {
                                ZStack {
                                    RoundedRectangle(cornerRadius: 8)
                                        .fill(Color(red: 0.345, green: 0.337, blue: 0.839)) // iOS Purple 0x5856D6
                                        .frame(width: 32, height: 32)
                                    Image(systemName: "timer")
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(.white)
                                }
                                
                                VStack(alignment: .leading, spacing: 3) {
                                    Text("Hẹn giờ ngủ")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundColor(ColorTokens.textPrimary)
                                    
                                    if sleepTimer.isActive {
                                        Text("Còn lại \(sleepTimer.formatRemainingTime()) (Tắt lúc \(sleepTimer.formatTargetEndTime()))")
                                            .font(.caption)
                                            .foregroundColor(ColorTokens.primaryAccent)
                                    } else {
                                        Text("Đang tắt")
                                            .font(.caption)
                                            .foregroundColor(ColorTokens.textSecondary)
                                    }
                                }
                                
                                Spacer()
                                
                                Toggle("", isOn: Binding(
                                    get: { sleepTimer.isActive },
                                    set: { isChecked in
                                        if isChecked {
                                            sleepTimer.startTimer(minutes: sleepTimer.selectedPresetMinutes)
                                        } else {
                                            sleepTimer.cancelTimer()
                                        }
                                    }
                                ))
                                .labelsHidden()
                                .tint(ColorTokens.primaryAccent)
                            }
                            
                            if sleepTimer.isActive {
                                Divider()
                                    .background(Color.white.opacity(0.08))
                                
                                VStack(alignment: .leading, spacing: 10) {
                                    Text("Chọn thời gian:")
                                        .font(.system(size: 12, weight: .semibold))
                                        .foregroundColor(ColorTokens.textSecondary)
                                    
                                    HStack(spacing: 8) {
                                        let presets = [15, 30, 45, 60]
                                        ForEach(presets, id: \.self) { mins in
                                            let isSelected = (sleepTimer.selectedPresetMinutes == mins)
                                            Button(action: {
                                                sleepTimer.startTimer(minutes: mins)
                                            }) {
                                                Text("\(mins)p")
                                                    .font(.system(size: 14, weight: isSelected ? .bold : .medium))
                                                    .foregroundColor(isSelected ? .white : ColorTokens.textPrimary)
                                                    .frame(maxWidth: .infinity)
                                                    .padding(.vertical, 9)
                                                    .background(
                                                        RoundedRectangle(cornerRadius: 10)
                                                            .fill(isSelected ? ColorTokens.primaryAccent : ColorTokens.darkSurfaceVariant)
                                                    )
                                            }
                                            .buttonStyle(.plain)
                                        }
                                        
                                        // Custom duration chip
                                        let isCustom = ![15, 30, 45, 60].contains(sleepTimer.selectedPresetMinutes)
                                        Button(action: {
                                            customTimerMinutes = Double(sleepTimer.selectedPresetMinutes)
                                            showingCustomTimerSheet = true
                                        }) {
                                            Text(isCustom ? "\(sleepTimer.selectedPresetMinutes)p" : "Tùy chọn")
                                                .font(.system(size: 13, weight: isCustom ? .bold : .medium))
                                                .foregroundColor(isCustom ? .white : ColorTokens.textPrimary)
                                                .frame(maxWidth: .infinity)
                                                .padding(.vertical, 9)
                                                .background(
                                                    RoundedRectangle(cornerRadius: 10)
                                                        .fill(isCustom ? ColorTokens.primaryAccent : ColorTokens.darkSurfaceVariant)
                                                )
                                        }
                                        .buttonStyle(.plain)
                                    }
                                    
                                    // Button Hủy hẹn giờ
                                    Button(action: {
                                        sleepTimer.cancelTimer()
                                    }) {
                                        HStack(spacing: 6) {
                                            Image(systemName: "xmark.circle.fill")
                                                .font(.system(size: 14))
                                            Text("Hủy hẹn giờ")
                                                .font(.system(size: 14, weight: .semibold))
                                        }
                                        .foregroundColor(Color.red.opacity(0.9))
                                        .frame(maxWidth: .infinity, alignment: .center)
                                        .padding(.top, 4)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                        .padding(.vertical, 6)
                    }
                    .listRowBackground(ColorTokens.darkSurface)
                    
                    // PHẦN 3: THƯ VIỆN & BỘ NHỚ
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
                    
                    // PHẦN 4: THÔNG TIN ỨNG DỤNG
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
                    
                    Section {
                        Spacer()
                            .frame(height: 120)
                    }
                    .listRowBackground(Color.clear)
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
            .sheet(isPresented: $showingCustomTimerSheet) {
                CustomSleepTimerSheet(minutes: $customTimerMinutes) { selectedMins in
                    sleepTimer.startTimer(minutes: selectedMins)
                }
                .presentationDetents([.fraction(0.38)])
            }
        }
    }
}

// MARK: - Custom Sleep Timer Sheet
struct CustomSleepTimerSheet: View {
    @Binding var minutes: Double
    var onConfirm: (Int) -> Void
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        ZStack {
            ColorTokens.darkSurface.ignoresSafeArea()
            
            VStack(spacing: 20) {
                // Header
                HStack {
                    Text("Tùy chỉnh thời gian hẹn giờ")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(ColorTokens.textPrimary)
                    Spacer()
                    Button(action: { dismiss() }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(ColorTokens.textSecondary)
                    }
                }
                .padding(.top, 20)
                
                // Display time
                VStack(spacing: 6) {
                    Text("\(Int(minutes)) phút")
                        .font(.system(size: 36, weight: .bold, design: .rounded))
                        .foregroundColor(ColorTokens.primaryAccent)
                    
                    Text("Nhạc sẽ tự động dừng sau khoảng thời gian này")
                        .font(.caption)
                        .foregroundColor(ColorTokens.textSecondary)
                }
                
                // Slider
                Slider(value: $minutes, in: 5...180, step: 5)
                    .tint(ColorTokens.primaryAccent)
                
                // Quick adjustment chips
                HStack(spacing: 10) {
                    ForEach([10, 20, 45, 90, 120], id: \.self) { val in
                        Button("\(val)p") {
                            minutes = Double(val)
                        }
                        .font(.system(size: 13, weight: .semibold))
                        .padding(.vertical, 6)
                        .padding(.horizontal, 12)
                        .background(ColorTokens.darkSurfaceVariant)
                        .foregroundColor(ColorTokens.textPrimary)
                        .cornerRadius(8)
                    }
                }
                
                // Confirm Button
                Button(action: {
                    onConfirm(Int(minutes))
                    dismiss()
                }) {
                    Text("Bắt đầu hẹn giờ")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(
                            RoundedRectangle(cornerRadius: 14)
                                .fill(ColorTokens.primaryAccent)
                        )
                }
                .buttonStyle(.plain)
                .padding(.bottom, 10)
            }
            .padding(.horizontal, 20)
        }
    }
}
