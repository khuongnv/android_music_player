# Android Offline Music Player

A premium, high-performance offline music player for Android, heavily inspired by the clean and elegant aesthetics of Apple Music. This application is designed to provide a seamless listening experience for your locally stored music files, with a focus on visual excellence and smooth interactions.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/language-Kotlin-purple.svg)

## 📱 Project Overview

The **Android Offline Music Player** is a local-only music player. It does not support streaming or online services, ensuring 100% privacy and full control over your offline library.

- **Objective**: Build a robust, visually stunning offline music player.
- **Design Philosophy**: Minimalist, modern, rounded corners, large album art, and smooth animations.
- **Target OS**: Android 8.0+ (API 26)

## ✨ Key Features

- **Local Library Sync**: Automatically scans and organizes music from your device storage.
- **Apple Music Inspired UI**: Premium look and feel with Jetpack Compose.
- **Flexible Sorting**: View your music by **Songs**, **Albums**, **Artists**, and **Playlists**.
- **Advance Playback Controls**:
    - Background playback with `MediaSessionService`.
    - Notification and Lockscreen controls.
    - Shuffle and Repeat modes (Off / One / All).
    - Smooth seeking functionality even when paused.
- **Mini Player**: A persistent player floating above the navigation bar for quick access.
- **State Persistence**: Remembers your last played song, position, and playback modes using Jetpack DataStore.
- **Light / Dark Mode**: Full support for system-wide theme switching.
- **Audio Focus Management**: Automatically pauses for incoming calls and resumes afterwards.

## 🛠 Technology Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Media Engine**: [AndroidX Media3 (ExoPlayer)](https://developer.android.com/guide/topics/media/media3)
- **Programming Language**: [Kotlin](https://kotlinlang.org/)
- **Data Persistence**: 
    - [Room Database](https://developer.android.com/training/data-storage/room) (for Playlists)
    - [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (for Player State)
- **Core API**: MediaStore API (for local file discovery)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)

## 🏗 Architecture

The project follows the **Clean Architecture** principles combined with the **MVVM** pattern:

- **UI Layer**: Jetpack Compose screens and ViewModels.
- **Domain Layer**: Models, Repository Interfaces, and Use Cases.
- **Data Layer**: Repository Implementations, MediaStore API handling, and Room Database.

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or newer.
- **JDK**: 17 or 21.
- **Android SDK**: API level 34.

### Installation
1. Clone the repository or download the source code.
2. Open the project in Android Studio.
3. Sync Gradle and wait for the dependencies to download.
4. Connect an Android device or start an emulator (API 26+).
5. Press **Run** (▷).

**Note**: You must grant **Music and Audio** permissions on the first launch for the app to function.

## 📖 Usage Guide

1. **Prepare Music**: Ensure you have audio files (`.mp3`, `.m4a`, etc.) in your device's `Music` or `Download` folders.
2. **Explore Library**: Use the **Library** tab to browse your collection.
3. **Play Music**: Tap any song to start playback. Use the **Mini Player** at the bottom to expand to the full **Now Playing** screen.
4. **Manage Playlists**: Create custom playlists in the Library tab to group your favorite tracks.

---

*Developed with ❤️ inspired by timeless design.*
