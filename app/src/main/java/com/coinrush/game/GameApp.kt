package com.coinrush.game

import android.app.Application
import com.google.android.gms.ads.MobileAds

class GameApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the Google Mobile Ads SDK on a background thread.
        MobileAds.initialize(this) {}
    }
}
