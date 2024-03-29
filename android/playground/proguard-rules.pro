# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/lixinke/Tool/android-eclipse/adt-bundle-mac-x86_64-20140702/sdk/tools/proguard/proguard-android.txt
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
##weex
-keep class org.apache.weex.bridge.**{*;}
-keep class org.apache.weex.dom.**{*;}
-keep class org.apache.weex.adapter.**{*;}
-keep class org.apache.weex.common.**{*;}
-keep class * implements org.apache.weex.IWXObject{*;}
-keep class org.apache.weex.ui.**{*;}
-keep class org.apache.weex.ui.component.**{*;}
-keep class org.apache.weex.utils.**{
    public <fields>;
    public <methods>;
    }
-keep class org.apache.weex.view.**{*;}
-keep class org.apache.weex.module.**{*;}
-keep public class * extends org.apache.weex.common.WXModule{*;}
