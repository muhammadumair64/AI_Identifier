# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.iobits.tech.app.ai_identifier.database.**{*;}
-keep class com.bytedance.sdk.** { *; }
-dontwarn com.ironsource.**
-dontwarn com.ironsource.adapters.**
-keepclassmembers class com.ironsource.** { public *; }
-keep public class com.ironsource.**
-keep class com.ironsource.adapters.** { *;}
-dontwarn com.facebook.infer.annotation.**


# Keep all Retrofit model classes (adjust package as needed)
-keep class com.iobits.tech.app.ai_identifier.network.models.** { *; }

# Keep Gson annotations
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson data classes
-keep class com.google.gson.** { *; }

# Don't obfuscate Retrofit interfaces
-keep interface retrofit2.** { *; }

# Keep generic type info for Gson
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all model fields and names for Gson to parse properly
-keepclassmembers class com.iobits.tech.app.ai_identifier.network.models.** {
    <fields>;
}

-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
