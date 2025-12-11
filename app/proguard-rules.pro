# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept., these are actually used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.prody.prashant.**$$serializer { *; }
-keepclassmembers class com.prody.prashant.** {
    *** Companion;
}
-keepclasseswithmembers class com.prody.prashant.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Room entities
-keep class com.prody.prashant.data.local.entities.** { *; }

# Keep Gemini AI
-keep class com.google.ai.client.generativeai.** { *; }

# Keep Compose
-dontwarn androidx.compose.**
