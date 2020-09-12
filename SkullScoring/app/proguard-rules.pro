# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes *Annotation*,Signature,InnerClasses
-keepclassmembers enum * { *; }
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# skull scoring
-keep class com.sebastienbalard.skullscoring.** { *; }
-keepclassmembers class com.sebastienbalard.skullscoring.** { *; }
