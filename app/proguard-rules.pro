# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Media3 classes
-keep class androidx.media3.** { *; }

# Keep Room entities
-keep class com.music.player.data.local.room.entity.** { *; }

# Hilt
-keepclassmembers class * {
    @dagger.hilt.android.** *;
}
