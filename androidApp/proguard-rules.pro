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

-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.android.hms.agent.**{*;}

# Bugly
-keep class com.tencent.feedback.** {*;}
-keep public interface com.tencent.feedback.eup.jni.NativeExceptionHandler{*;}
-keep public class com.tencent.feedback.eup.jni.NativeExceptionUpload{*;}
-keep public class com.tencent.bugly.crashreport.crash.jni.NativeExceptionHandler {
    *;
}

# Beacon 灯塔上报
-keep class com.tencent.beacon.** { *; }
-keep class com.tencent.qmsp.** {*;}

# Qimei
-keep class com.tencent**qimei.** { *;}

# IMSDK（native so 通过 JNI 反射加载，必须保留完整类名）
-keep class com.tencent.imsdk.** { *; }

# Mars XLog（native so 通过 JNI 绑定 external 方法，必须保留类名、方法签名和内部类）
-keep class com.tencent.mars.xlog.** { *; }

# ---- Missing class suppressions for R8 (runtime-optional dependencies) ----

# Android 隐藏 API
-dontwarn android.app.ActivityThread

# J2V8 引擎（Kuikly 渲染层可选依赖，实际运行时由 native so 提供）
-dontwarn com.eclipsesource.v8.**

# 腾讯 X5 WebView（Qimei SDK 可选引用）
-dontwarn com.tencent.smtt.**

# OkHttp 内部类（QQ 互联 SDK 引用）
-dontwarn okhttp3.internal.Version

# ---- KMM 序列化模型 keep 规则 ----

# 保留所有实现 IKmmKeep 接口的类及其成员（防止混淆导致反序列化失败）
-keep class * implements com.tencent.news.core.extension.IKmmKeep {
    *;
}

# 保留所有被 @kotlinx.serialization.Serializable 注解的类及其成员
-keep @kotlinx.serialization.Serializable class * {
    *;
}

# 保留 kotlinx.serialization 生成的 Companion 和 serializer 方法
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# 保留 @Serializable 类的 Companion 对象
-keepclasseswithmembers class **$$serializer {
    *** INSTANCE;
}

# Wire PB 协议类（类名用于 CMD 命令字推导，不能混淆）
-keep class * extends com.squareup.wire.kmm.Message {
    *;
}

# ThumbPlayer
-keep interface com.tencent.thumbplayer.api.** { *; }
-keep class com.tencent.thumbplayer.api.** { *; }
-keep interface com.tencent.thumbplayer.core.** { *; }
-keep class com.tencent.thumbplayer.core.** { *; }
-keep class com.tencent.thumbplayer.utils.TPLogUtil {*;}
-keep class com.tencet.beacon.** {*;}
-keep class com.tencet.tvkbeacon.** {*;}


-keep class com.tencent.mtt.hippy.** { *; }
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.android.hms.agent.**{*;}

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}


# qimei
-keep class com.tencent**qimei.** { *;}

# beacon
-keep class com.tencent.qmsp.oaid2.** {*;}
-keep class com.tencent.beacon.** { *;}
-keep class com.tencent.qmsp.sdk.** { *;}

# TPNS sdk相关：
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep class com.tencent.android.tpush.** {*;}
-keep class com.tencent.tpns.baseapi.** {*;}
-keep class com.tencent.tpns.mqttchannel.** {*;}
-keep class com.tencent.tpns.dataacquisition.** {*;}
# 华为
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep class com.huawei.agconnect.**{*;}
# 荣耀
-keep class com.hihonor.push.framework.aidl.**{*;}
-keep class com.hihonor.push.sdk.**{*;}
# 小米
-keep class com.xiaomi.**{*;}
-keep public class * extends com.xiaomi.mipush.sdk.PushMessageReceiver
# vivo
-dontwarn com.vivo.push.**P
-keep class com.vivo.push.**{*; }
-keep class com.vivo.vms.**{*; }
-keep class com.tencent.android.vivopush.VivoPushMessageReceiver{*;}
# oppo
-keep public class * extends android.app.Service
-keep class com.heytap.mcssdk.** {*;}
-keep class com.heytap.msp.push.** { *;}

# 国家网络身份认证
-keep class cn.wh.**{*;}
-keep class com.fort.andJni.**{*;}

# 图灵盾 SDK 使用
-keep class com.**.TNative$aa { public *; }
-keep class com.**.TNative$aa$bb { public *; }
-keep class com.**.TNative$bb { *; }
-keep class com.**.TNative$bb$I { *; }
-keepclassmembers public final class com.tencent.turingfd.sdk.** {
    public <init>(...);
}
-keep class com.tencent.wework.api.** {
   *;
}