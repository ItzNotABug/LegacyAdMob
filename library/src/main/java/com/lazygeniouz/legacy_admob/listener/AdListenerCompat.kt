package com.lazygeniouz.legacy_admob.listener

import androidx.annotation.NonNull
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem

/**
 * Class that handles the events linked to the listener
 *
 * **Old: [AdListener](https://developers.google.com/android/reference/com/google/android/gms/ads/AdListener),
 * New: [AdListenerCompat]**
 */
abstract class AdListenerCompat {
    /**
     * Fired when the Ad is loaded
     */
    open fun onAdLoaded() {}

    /**
     * Fired when the Ad fails to load
     * @param error Contains details of the failure
     */
    open fun onAdFailedToLoad(error: LoadAdError) {}

    /**
     * Fired when the Ad opened / shown to the user
     */
    open fun onAdOpened() {}

    /**
     * Fired when the Ad is dismissed / closed by the user
     */
    open fun onAdClosed() {}

    /**
     * Fired when there was an error showing the Ad
     * @param adError Contains details of the failure
     */
    open fun onAdFailedToShow(adError: AdError) {}

    /**
     * Fired when the Ad logs an impression
     */
    open fun onAdImpression() {}
}

/**
 * Class that handles the events for Rewarded Ad Listener
 */
abstract class RewardedAdListenerCompat : AdListenerCompat() {

    /**
     * Fired when the user has successfully earned the reward by watching the RewardedAd
     * @param reward Contains the reward of the RewardedAd
     */
    open fun onUserEarnedReward(@NonNull reward: RewardItem) {}
}