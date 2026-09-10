# Keep AdMob working under R8 minification (the SDK ships most rules itself).
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**
