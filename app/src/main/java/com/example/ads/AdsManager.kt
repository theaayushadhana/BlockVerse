package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Centralized Google AdMob Manager for Blockverse.
 *
 * Implements Google AdMob best practices:
 * - Home screen banner ad integration.
 * - Rewarded ads upon loss (Second chance revive / 2x coin multiplier) ensuring compliance and high eCPM.
 * - Interstitial ads upon victory (Sector victory, Duel win, High Score win) with smart frequency capping to prevent user irritation.
 * - Automatic background pre-loading to eliminate latency.
 * - Graceful fallback when offline or no fill, ensuring the gameplay flow is never blocked.
 */
object AdsManager {
    private const val TAG = "BlockverseAds"

    // Official Google AdMob Test Ad Unit IDs (replace with your production IDs before releasing)
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Frequency capping: minimum 45s between interstitial ads to protect user experience
    private const val INTERSTITIAL_COOLDOWN_MS = 45_000L
    private var lastInterstitialShownTime = 0L

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob initialized: $status")
                isInitialized = true
                preloadInterstitial(context)
                preloadRewarded(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message} (code: ${error.code})")
                }
            }
        )
    }

    fun preloadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message} (code: ${error.code})")
                }
            }
        )
    }

    /**
     * Shows an interstitial ad when user wins a sector, duel, or achieves a milestone.
     * Enforces frequency capping so users are not irritated.
     */
    fun showInterstitial(
        activity: Activity,
        forceShow: Boolean = false,
        onAdDismissedOrSkipped: () -> Unit
    ) {
        val now = System.currentTimeMillis()
        val elapsed = now - lastInterstitialShownTime

        if (!forceShow && elapsed < INTERSTITIAL_COOLDOWN_MS) {
            Log.d(TAG, "Skipping interstitial ad: cooldown active (${(INTERSTITIAL_COOLDOWN_MS - elapsed) / 1000}s remaining)")
            onAdDismissedOrSkipped()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    preloadInterstitial(activity)
                    onAdDismissedOrSkipped()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    preloadInterstitial(activity)
                    onAdDismissedOrSkipped()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad displayed")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready, proceeding without delay")
            preloadInterstitial(activity)
            onAdDismissedOrSkipped()
        }
    }

    fun isRewardedAdLoaded(): Boolean = rewardedAd != null

    /**
     * Shows a rewarded ad when user loses or requests a second chance / 2x coins.
     * Ensures high engagement and good earnings while providing high in-game value.
     */
    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: (RewardItem?) -> Unit,
        onAdClosed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var rewardReceived = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    rewardedAd = null
                    preloadRewarded(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    preloadRewarded(activity)
                    // Grant reward gracefully if ad failed to show through no fault of user
                    if (!rewardReceived) {
                        onUserEarnedReward(null)
                    }
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad displayed")
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                rewardReceived = true
                onUserEarnedReward(rewardItem)
            }
        } else {
            Log.w(TAG, "Rewarded ad not ready; granting reward gracefully")
            preloadRewarded(activity)
            onUserEarnedReward(null)
            onAdClosed()
        }
    }
}
