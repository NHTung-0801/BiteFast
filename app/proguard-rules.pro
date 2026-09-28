# ==============================================================================
# BiteFast — Enterprise Production ProGuard / R8 Rules
# ==============================================================================

# --- 1. Optimization & Obfuscation Flags ---
-allowaccessmodification
-repackageclasses 'com.bitefast.obf'
-dontusemixedcaseclassnames
-verbose

# --- 2. Strip Logging in Release ---
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# --- 3. Native JNI & C++ Security ---
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.bitefast.core.common.security.NativeSecurity { *; }
-keep class com.bitefast.core.common.security.SecurityUtils { *; }

# --- 4. Room Database & SQLCipher ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
-dontwarn net.sqlcipher.**
-keep class net.sqlcipher.** { *; }
-keep interface net.sqlcipher.** { *; }

# --- 5. AndroidX Security Crypto, Keystore & Tink ---
-dontwarn androidx.security.crypto.**
-keep class androidx.security.crypto.** { *; }
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**

# --- 6. Kotlinx Serialization ---
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowoptimization class * {
    @kotlinx.serialization.Serializable class *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# --- 7. Retrofit 2 & OkHttp ---
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# --- 8. Dagger Hilt & Architecture Components ---
-dontwarn dagger.hilt.**
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends androidx.lifecycle.AndroidViewModel

# --- 9. Kotlin Coroutines & Flow ---
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --- 10. Jetpack Compose ---
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**
