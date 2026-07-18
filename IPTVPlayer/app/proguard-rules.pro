# Standard R8/ProGuard hardening for release builds.
# Obfuscates class/method/field names and strips unused code so the APK
# is meaningfully harder to read after decompilation. Not a substitute for
# server-side auth on anything genuinely sensitive.

-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''

# Keep Media3/ExoPlayer + Leanback classes that are accessed via reflection
-keep class androidx.media3.** { *; }
-keep class androidx.leanback.** { *; }
-dontwarn androidx.media3.**

# Keep our data model (parsed via reflection-free Kotlin, but keep names
# stable for any future Gson/Moshi JSON parsing of playlist sources)
-keep class com.example.iptvplayer.model.** { *; }

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
