package com.lazygeniouz.legacy_admob.interstitial

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lazygeniouz.legacy_admob.listener.AdListenerCompat

/**
 * [InterstitialAdCompat] uses new api from AdMob to
 * allow developers keep using the older format of the Interstitial API
 * where there was no static loading of the Ad.
 *
 * This is created so that there isn't too much of changes required in the project,
 * if there are many older implementations like **InterstitialAd(context)**.
 *
 * **InterstitialAd(context)** => **InterstitialAdCompat(activity)**
 *
 * @param context Context to load the Ad with the new api (Better to pass an Activity context)
 */
class InterstitialAdCompat(private val context: Context) {
    private var isAdLoading = false
    private var isImmersive = false
    private var responseInfo: ResponseInfo? = null
    private var interstitialAd: InterstitialAd? = null
    private var adListenerCompat: AdListenerCompat? = null
    private var paidEventListener: OnPaidEventListener? = null

    private var adUnitId: String = interstitialTestAdUnitId

    /**
     * Using older api style to load the interstitial ad.
     *
     * Load a RewardedAd with default AdRequest or configure & pass a customised AdRequest
     *
     * Uses the new [InterstitialAd.load]
     *
     * @see InterstitialAd.load
     * @param adRequest Default is an AdRequest object without any customisation
     */
    @RequiresPermission("android.permission.INTERNET")
    @JvmOverloads
    fun load(adRequest: AdRequest = AdRequest.Builder().build()) {
        isAdLoading = true
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(newAd: InterstitialAd) {
                isAdLoading = false
                interstitialAd = newAd
                interstitialAd!!.apply {
                    setImmersiveMode(isImmersive)
                    paidEventListener?.let { this.onPaidEventListener = it }
                    this@InterstitialAdCompat.responseInfo = responseInfo
                    fullScreenContentCallback = getFullScreenCallback()
                }
                adListenerCompat?.onAdLoaded()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                isAdLoading = false
                adListenerCompat?.onAdFailedToLoad(loadAdError)
            }
        })
    }

    /**
     * Returns true if the Ad is currently loading
     */
    fun isLoading() = isAdLoading

    /**
     * Set [adUnitId] for InterstitialAd,
     *
     * default is Test Ad Unit Id
     */
    fun setAdUnitId(adUnit: String) {
        this.adUnitId = adUnit
    }

    /**
     * Set immersive mode for the InterstitialAd
     *
     * @param keepImmersive Boolean value to enable / disable immersive on fullscreen ad
     */
    fun setImmersive(keepImmersive: Boolean) {
        this.isImmersive = keepImmersive
    }

    /**
     * Return true if the Ad is loaded i.e. not null
     */
    fun isLoaded() = interstitialAd != null

    /**
     * Show the InterstitialAd if it is loaded.
     *
     * If the [InterstitialAdCompat] was initialised with an Activity context,
     * you can directly use [showAd] without passing an activity instance.
     *
     * However, if you initialised the [InterstitialAdCompat] with a **Non Activity** context,
     * then you **must** pass an Activity to show the InterstitialAd as it is required by the new api.
     */
    @JvmOverloads
    fun showAd(activity: Activity? = null) {
        if (context is Activity) interstitialAd?.show(context)
        else {
            if (activity != null) interstitialAd?.show(activity)
            else Log.e(
                "InterstitialAdCompat",
                "The initial Context was not an Activity & " +
                        "the instance passed to `showAd(activity)` is also null"
            )
        }
    }

    /**
     * Set an ad listener to the interstitial ad.
     */
    fun setAdListener(adListenerCompat: AdListenerCompat) {
        this.adListenerCompat = adListenerCompat
    }

    /**
     * Set a paid event listener (new api)
     * @see [OnPaidEventListener]
     */
    fun setOnPaidEventListener(paidEventListener: OnPaidEventListener) {
        this.paidEventListener = paidEventListener
    }

    /**
     * Return the current Ad Unit Id
     */
    fun getAdUnitId() = this.adUnitId

    /**
     * Return thr current ad listener attached to the interstitial ad
     */
    @Nullable
    fun getAdListener() = this.adListenerCompat

    /**
     * Return the current paid event listener attached to the interstitial ad
     */
    @Nullable
    fun getOnPaidEventListener() = this.paidEventListener

    /**
     * Should only be called after the Ad is loaded
     * @return [InterstitialAd.getResponseInfo]
     */
    @Nullable
    fun getResponseInfo() = this.responseInfo

    private fun getFullScreenCallback() = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            adListenerCompat?.onAdClosed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            adListenerCompat?.onAdFailedToShow(adError)
        }

        override fun onAdImpression() {
            adListenerCompat?.onAdImpression()
        }

        override fun onAdShowedFullScreenContent() {
            adListenerCompat?.onAdOpened()
        }

    }

    companion object {
        @JvmStatic
        val interstitialTestAdUnitId = "ca-app-pub-3940256099942544/1033173712"
    }
}