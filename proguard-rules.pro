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

# If you keep the line number information, uncomment this to
# hide the original source file name.
-keepattributes *Annotation*
#保留部分泛型信息，必要!
-keepattributes Signature
#手动启用support keep注解
#http://tools.android.com/tech-docs/support-annotations
-dontskipnonpubliclibraryclassmembers
-keep,allowobfuscation @interface android.support.annotation.Keep

-keep @android.support.annotation.Keep class * {
*;
}

-keepclassmembers class * {
    @android.support.annotation.Keep *;
}
#手动启用Component注解
#http://tools.android.com/tech-docs/support-annotations
-keep,allowobfuscation @interface com.trend.lazyinject.annotation.Component

-keep,allowobfuscation @com.trend.lazyinject.annotation.Component class * {
*;
}

-keepclassmembers class * {
    @com.trend.lazyinject.annotation.Component *;
}

-dontwarn javassist.**