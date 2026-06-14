# OpenFlux ProGuard Rules

# Keep data classes for JSON serialization
-keep class com.openflux.app.model.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep JSON
-keep class org.json.** { *; }
