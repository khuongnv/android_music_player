#!/bin/bash
set -e

export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"

echo "=========================================="
echo "MUSIC PLAYER - REMOTE BUILD & CODE SIGN"
echo "=========================================="

WORKDIR="$HOME/musicplayer-build/ios"
cd "$WORKDIR"

echo ">>> 1. Mở khóa Keychain..."
security unlock-keychain -p "1230" "$HOME/Library/Keychains/login.keychain-db" 2>/dev/null || true
security set-key-partition-list -S apple-tool:,apple:,codesign: -s -k "1230" "$HOME/Library/Keychains/login.keychain-db" 2>/dev/null || true

echo ">>> 2. Chuẩn bị thư mục build..."
rm -rf build
mkdir -p build/ipa/Payload/MusicPlayer.app

echo ">>> 3. Tìm kiếm file mã nguồn Swift..."
SWIFT_FILES=$(find MusicPlayer -name "*.swift" -type f | tr '\n' ' ')
echo "Đã tìm thấy các file Swift."

echo ">>> 4. Biên dịch Swift cho arm64-apple-ios16.0..."
xcrun --sdk iphoneos swiftc \
  -target arm64-apple-ios16.0 \
  -O \
  -parse-as-library \
  $SWIFT_FILES \
  -o build/ipa/Payload/MusicPlayer.app/MusicPlayer

echo "Biên dịch binary thành công."

echo ">>> 4b. Biên dịch Asset Catalog (AppIcon -> Assets.car)..."
if [ -d "MusicPlayer/Resources/Assets.xcassets" ]; then
  xcrun actool MusicPlayer/Resources/Assets.xcassets \
    --compile build/ipa/Payload/MusicPlayer.app \
    --platform iphoneos \
    --minimum-deployment-target 16.0 \
    --app-icon AppIcon \
    --output-partial-info-plist build/assets-info.plist || true
  echo "Asset catalog compiled."
fi

# Sao chép các icon PNG trực tiếp vào bundle root để đảm bảo 100% hiển thị
if [ -d "MusicPlayer/Resources/Assets.xcassets/AppIcon.appiconset" ]; then
  cp MusicPlayer/Resources/Assets.xcassets/AppIcon.appiconset/*.png build/ipa/Payload/MusicPlayer.app/ 2>/dev/null || true
fi

echo ">>> 4c. Biên dịch LaunchScreen.storyboard (Fix Letterbox & Tỷ lệ màn hình)..."
if [ -f "MusicPlayer/Resources/LaunchScreen.storyboard" ]; then
  xcrun ibtool --compile build/ipa/Payload/MusicPlayer.app/LaunchScreen.storyboardc \
    MusicPlayer/Resources/LaunchScreen.storyboard || true
  echo "LaunchScreen.storyboard compiled."
fi

echo ">>> 5. Cấu hình Info.plist & embedded.mobileprovision..."
cp MusicPlayer/Resources/Info.plist build/ipa/Payload/MusicPlayer.app/Info.plist
plutil -replace CFBundleExecutable -string "MusicPlayer" build/ipa/Payload/MusicPlayer.app/Info.plist
plutil -replace CFBundleIdentifier -string "com.dhtthni.dhxe" build/ipa/Payload/MusicPlayer.app/Info.plist
plutil -replace CFBundleDisplayName -string "Music" build/ipa/Payload/MusicPlayer.app/Info.plist
plutil -replace CFBundleName -string "Music" build/ipa/Payload/MusicPlayer.app/Info.plist

PROV_SRC="$HOME/musicplayer-build/certs/309d9c0d-3f81-40ab-919f-e9c4a6405773.mobileprovision"
if [ ! -f "$PROV_SRC" ]; then
  PROV_SRC=$(find "$HOME/musicplayer-build/certs" -name "*.mobileprovision" | head -n 1)
fi
cp "$PROV_SRC" build/ipa/Payload/MusicPlayer.app/embedded.mobileprovision

echo ">>> 6. Trích xuất Entitlements từ Provisioning Profile..."
security cms -D -i build/ipa/Payload/MusicPlayer.app/embedded.mobileprovision | plutil -extract Entitlements xml1 -o build/entitlements.plist -

echo ">>> 7. Thực hiện Ký số Enterprise (VNPT)..."
codesign --force \
  --sign "55EC3964ACBC2DCA8D9C621C07EFA94AF764600F" \
  --entitlements build/entitlements.plist \
  --timestamp=none \
  --generate-entitlement-der \
  build/ipa/Payload/MusicPlayer.app

echo ">>> 8. Đóng gói IPA..."
cd build/ipa
zip -qr MusicPlayer.ipa Payload
cd "$WORKDIR"

echo ">>> 9. Kiểm tra tính hợp lệ của IPA & Chữ ký..."
codesign -vv -d build/ipa/Payload/MusicPlayer.app
ls -lh build/ipa/MusicPlayer.ipa
shasum -a 256 build/ipa/MusicPlayer.ipa

echo "=========================================="
echo "BUILD & KÝ SỐ THÀNH CÔNG!"
echo "=========================================="
