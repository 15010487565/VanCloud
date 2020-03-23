# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Program Files (x86)\Android\android-studio\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ProGuard configurations for NetworkBench Lens
#指定代码的压缩级别
-optimizationpasses 5
#包明不混合大小写
-dontusemixedcaseclassnames
#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses
 #优化  不优化输入的类文件
-dontoptimize
 #预校验
-dontpreverify
 #混淆时是否记录日志
-verbose
 # 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
##---------------Begin: proguard configuration for Gson  ----------
-keepattributes Signature
#保护注解
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
# 保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v4.app.Fragment
#忽略警告
-ignorewarnings
#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers @interface * {
    public ** value();
}
#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable
#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#保持枚举 enum 类不被混淆 如果混淆报错，建议直接使用上面的
-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
}
-keepclassmembers class ** {
    public void onEvent*(**);
}
-keep class android.support.v4.app.** { *; }
# 添加第三方jar包
-keepattributes Exceptions, Signature, InnerClasses
# End NetworkBench Lens
#for gson
-keep class com.google.**{*;}
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class * extends cn.douboshi.mobile.android.utils.volley.api.BaseItem { *; }
-keep class * extends com.vgtech.common.api.AbsApiData { *; }

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.vgtech.vancloud.api.** { *; }
-keep class com.vgtech.common.api.** { *; }
-keep class com.vgtech.vancloud.models.** { *; }
-keep class com.vgtech.vancloud.provider.** { *; }
-keep class com.vgtech.vancloud.ui.** { *; }
-keep class com.vgtech.vancloud.ui.chat.models.** { *; }
-keep class com.vgtech.videomodule.api.** { *; }
-keep class com.vgtech.videomodule.model.** { *; }
-keep class com.vgtech.vantop.moudle.** { *; }
-keep class com.vgtech.common.utils.wheel.WheelUtil { *; }
-keep class com.vgtech.common.view.DateFullDialogView { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

##---------------Begin: proguard configuration for fileselector ----------
-dontwarn zhou.tools.fileselector.**
-keep class zhou.tools.fileselector.** { *; }

-dontwarn com.walnutlabs.android.**
-keep class com.walnutlabs.android.** { *; }
##---------------Begin: proguard configuration for pulltorefresh ----------
-dontwarn com.handmark.pulltorefresh.**
-keep class com.handmark.pulltorefresh.** { *; }
-dontwarn me.leolin.shortcutbadger.**
-keep class me.leolin.shortcutbadger.** { *; }

-keep class com.umeng.analytics.** { *; }
-keep class org.aopalliance.** {*;}
-keep class com.android.support.** { *; }
-keep class com.mcxiaoke.volley.** { *; }
-keep class com.google.code.gson.** { *; }
-keep class com.jakewharton.** { *; }
-keep class com.belerweb.** { *; }
-keep class com.nostra13.universalimageloader.** { *; }
-keep class com.github.chrisbanes.photoview.** { *; }
-keep class ccom.makeramen.** { *; }
-keep class org.springframework.android.** { *; }
-keep class dnsjava.** { *; }
-keep class com.tencent.mm.** { *; }
-keep class com.json.simple.** { *; }
-keep class com.igexin.getuiext.** {*;}
-keep class com.igexin.** { *; }
-keep class com.activeandroid.** { *; }
-keep class com.baidu.** { *; }
-keep class vi.com.** {*;}
-keep class mapsdkvi.com.** {*;}
-dontwarn com.baidu.**
-keep class com.sina.** { *; }
-keep class android.support.v7.widget.** { *; }
-keep class com.novell.sasl.client.** { *; }
-keep class de.measite.smack.** { *; }
-keep class org.jivesoftware.** { *; }
-keep class org.apache.** { *; }
-keep class javax.inject.** {*;}
-keep class com.google.inject.** {*;}
-keep class com.google.inject.Binder
-keep class org.objectweb.asm.** {*;}
-keep class com.dropbox.client2.** {*;}
-keep class com.google.api.client.googleapis.** {*;}
-keep class com.google.api.client.googleapis.extensions.android.** {*;}
-keep class com.google.api.services.drive.** {*;}
-keep class com.google.api.client.** {*;}
-keep class com.google.api.client.extensions.android.** {*;}
-keep class com.google.api.client.json.gson.** {*;}
-keep class com.google.api.client.auth.** {*;}
-keep class org.json.simple.** {*;}
-keep class com.google.protobuf.** {*;}
-keep class com.google.api.client.extensions.android.** {*;}
-keep class com.zipow.** {*;}
-keep class org.webrtc.voiceengine.** {*;}
-keep class us.zoom.** {*;}


#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v4.app.Fragment
-keepclassmembers @interface * {
    public ** value();
}
-keep class roboguice.** { *; }
-dontwarn javax.inject.**
# 过滤R文件的混淆：
-keep class **.R$* {*;}

-keepclassmembers class * {
    @com.google.inject.Inject <init>(...);
}
-keepclassmembers class * {
    void *(**On*Event);
}
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 这1句是屏蔽警告
-ignorewarnings
-verbose
# Needed for RoboGuice, etc
-keepattributes SourceFile,LineNumberTable,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleFieldAnnotations
-keep public class com.google.inject.Inject
-keep,allowobfuscation public class com.google.inject.name.Named
-keep,allowobfuscation public class * implements com.google.inject.Provider
-keep,allowobfuscation @com.google.inject.Provides class *
-keep,allowobfuscation @com.google.inject.Provides class *
-keep,allowobfuscation @com.google.inject.ProvidedBy class *
-keep,allowobfuscation @com.google.inject.Singleton class *
-keep,allowobfuscation @com.google.inject.BindingAnnotation class *
-keep,allowobfuscation @com.google.inject.ScopeAnnotation class *

-keepclassmembers class * {
    @com.google.inject.Inject <init>(...);
}

-keepclassmembers class com.google.inject.Inject {
    public boolean optional();
}

-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.InjectView *;}

# Annotations
-keepclasseswithmembernames class * {
    public ** value();
}

-keepclassmembers class * implements java.lang.annotation.Annotation {
    ** *();
}
# Facebook
-dontwarn com.facebook.**
-keep class com.facebook.** {*;}
-keep interface com.facebook.** {*;}
-keep enum com.facebook.** {*;}
# Fresco
-keep class com.facebook.fresco.** {*;}
-keep interface com.facebook.fresco.** {*;}
-keep enum com.facebook.fresco.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keep class com.baidu.mobads.*.** { *; }

#eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(Java.lang.Throwable);
}
#避免混淆Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
#第三方相机
-keep class com.huantansheng.easyphotos.models.** { *; }

-dontwarn com.box.**
-dontwarn org,apache.**
-dontwarn org,apache.**
-keepattributes EnclosingMethod
#保持源码的行号、源文件信息不被混淆 方便在崩溃日志中查看
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
#腾讯X5
#-optimizationpasses 7
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-dontskipnonpubliclibraryclassmembers
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
#-overloadaggressively

# ------------------ Keep LineNumbers and properties ---------------- #
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
# --------------------------------------------------------------------------

# Addidional for x5.sdk classes for apps

-keep class com.tencent.smtt.export.external.**{
    *;
}

-keep class com.tencent.tbs.video.interfaces.IUserStateChangedListener {
	*;
}

-keep class com.tencent.smtt.sdk.CacheManager {
	public *;
}

-keep class com.tencent.smtt.sdk.CookieManager {
	public *;
}

-keep class com.tencent.smtt.sdk.WebHistoryItem {
	public *;
}

-keep class com.tencent.smtt.sdk.WebViewDatabase {
	public *;
}

-keep class com.tencent.smtt.sdk.WebBackForwardList {
	public *;
}

-keep public class com.tencent.smtt.sdk.WebView {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebView$HitTestResult {
	public static final <fields>;
	public java.lang.String getExtra();
	public int getType();
}

-keep public class com.tencent.smtt.sdk.WebView$WebViewTransport {
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebView$PictureListener {
	public <fields>;
	public <methods>;
}


-keepattributes InnerClasses

-keep public enum com.tencent.smtt.sdk.WebSettings$** {
    *;
}

-keep public enum com.tencent.smtt.sdk.QbSdk$** {
    *;
}

-keep public class com.tencent.smtt.sdk.WebSettings {
    public *;
}


-keepattributes Signature
-keep public class com.tencent.smtt.sdk.ValueCallback {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebViewClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.DownloadListener {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebChromeClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebChromeClient$FileChooserParams {
	public <fields>;
	public <methods>;
}

-keep class com.tencent.smtt.sdk.SystemWebChromeClient{
	public *;
}
# 1. extension interfaces should be apparent
-keep public class com.tencent.smtt.export.external.extension.interfaces.* {
	public protected *;
}

# 2. interfaces should be apparent
-keep public class com.tencent.smtt.export.external.interfaces.* {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.WebViewCallbackClient {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.WebStorage$QuotaUpdater {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebIconDatabase {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebStorage {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.DownloadListener {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.QbSdk {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.QbSdk$PreInitCallback {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.CookieSyncManager {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.Tbs* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.LogFileUtils {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.TbsLog {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.TbsLogClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.CookieSyncManager {
	public <fields>;
	public <methods>;
}

# Added for game demos
-keep public class com.tencent.smtt.sdk.TBSGamePlayer {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerClient* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerClientExtension {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerService* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.Apn {
	public <fields>;
	public <methods>;
}
-keep class com.tencent.smtt.** {
	*;
}
# end


-keep public class com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension {
	public <fields>;
	public <methods>;
}

-keep class MTT.ThirdAppInfoNew {
	*;
}

-keep class com.tencent.mtt.MttTraceEvent {
	*;
}

# Game related
-keep public class com.tencent.smtt.gamesdk.* {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.TBSGameBooter {
        public <fields>;
        public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGameBaseActivity {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.TBSGameBaseActivityProxy {
	public protected *;
}

-keep public class com.tencent.smtt.gamesdk.internal.TBSGameServiceClient {
	public *;
}
#忽略 libiary 混淆
-keep class com.vgtech.common.** { *; }

-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}