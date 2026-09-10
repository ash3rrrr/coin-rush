package com.coinrush.game.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Loads and shows interstitial and rewarded ads.
 *
 * Uses Google's official TEST ad unit IDs. Before publishing to Google Play,
 * replace them with the real unit IDs from your AdMob account, and replace the
 * sample app ID in AndroidManifest.xml.
 */
class AdController(private val activity: Activity) {

    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null

    private val interstitialId = "ca-app-pub-3940256099942544/1033173712"
    private val rewardedId = "ca-app-pub-3940256099942544/5224354917"

    fun loadAll() {
        loadInterstitial()
        loadRewarded()
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            activity,
            interstitialId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            }
        )
    }

    private fun loadRewarded() {
        RewardedAd.load(
            activity,
            rewardedId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                }
            }
        )
    }

    fun showInterstitial() {
        val ad = interstitial ?: return
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = loadInterstitial()
            override fun onAdFailedToShowFullScreenContent(adError: AdError) = loadInterstitial()
        }
        ad.show(activity)
    }

    fun showRewarded(onUserEarnedReward: () -> Unit) {
        val ad = rewarded ?: return
        rewarded = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = loadRewarded()
            override fun onAdFailedToShowFullScreenContent(adError: AdError) = loadRewarded()
        }
        ad.show(activity) { onUserEarnedReward() }
    }

    /** Release full-screen ad callbacks when the hosting activity is destroyed. */
    fun destroy() {
        interstitial?.fullScreenContentCallback = null
        rewarded?.fullScreenContentCallback = null
        interstitial = null
        rewarded = null
    }
}
