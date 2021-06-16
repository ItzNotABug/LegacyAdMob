package com.lazygeniouz.legacy_admob.rewarded

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewarded.*
import com.lazygeniouz.legacy_admob.listener.RewardedAdListenerCompat

/**
 * [RewardedAdCompat] uses new api from AdMob to
 * allow developers keep using the older format of the Rewarded API
 * where there was no static loading of the Ad.
 *
 * This is created so that there isn't too much of changes required in the project,
 * if there are many older implementations like **RewardedAd(context, adUnitId)**.
 *
 * **RewardedAd(context, adUnitId)** => **RewardedAdCompat(context, adUnitId)**
 *
 * @param context Context to load the Ad with the new api (Better to pass an Activity context)
 * @param adUnitId The Ad Unit Id for this Ad,
 * default is a Test Ad Unit Id ([RewardedAdCompat.rewardedTestAdUnitId])
 */
class RewardedAdCompat @JvmOverloads constructor(
    private val context: Context,
    private val adUnitId: String = rewardedTestAdUnitId
) {
    private var isAdLoading = false
    private var isImmersive = false
    private var rewardedAd: RewardedAd? = null
    private var rewardItem: RewardItem? = null
    private var responseInfo: ResponseInfo? = null
    private var paidEventListener: OnPaidEventListener? = null
    private var adListenerCompat: RewardedAdListenerCompat? = null

    private var metadataChangedListener: OnAdMetadataChangedListener? = null
    private var serverSideVerificationOptions: ServerSideVerificationOptions? = null

    /**
     * Using older api style to load the RewardedAd.
     *
     * Load a RewardedAd with default AdRequest or configure & pass a customised AdRequest
     *
     * Uses the new [RewardedAd.load]
     *
     * @param adRequest Default is an AdRequest object without any customisation
     */
    @RequiresPermission("android.permission.INTERNET")
    @JvmOverloads
    fun loadAd(adRequest: AdRequest = AdRequest.Builder().build()) {
        isAdLoading = true
        RewardedAd.load(context, adUnitId, adRequest, getRewardedAdLoadCallback())
    }

    /**
     * Load a RewardedAd with default [AdManagerAdRequest] or
     * configure & pass a customised AdManagerAdRequest
     *
     * @param adRequest Default is an AdManagerAdRequest object without any customisation
     */
    @RequiresPermission("android.permission.INTERNET")
    @JvmOverloads
    fun loadAdWithAdManager(
        adRequest: AdManagerAdRequest = AdManagerAdRequest.Builder().build()
    ) {
        isAdLoading = true
        RewardedAd.load(context, adUnitId, adRequest, getRewardedAdLoadCallback())
    }

    /**
     * Returns true if the Ad is currently loading
     */
    fun isLoading() = isAdLoading

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
    fun isLoaded() = rewardedAd != null

    /**
     * Show the InterstitialAd if it is loaded.
     *
     * If the [RewardedAdCompat] was initialised with an Activity context,
     * you can directly use [showAd] without passing an activity instance.
     *
     * However, if you initialised the [RewardedAdCompat] with a **Non Activity** context,
     * then you **must** pass an Activity to show the InterstitialAd as it is required by the new api.
     */
    @JvmOverloads
    fun showAd(activity: Activity? = null) {
        if (context is Activity) rewardedAd?.show(context, getOnUserEarnedRewardListener())
        else {
            if (activity != null) rewardedAd?.show(activity, getOnUserEarnedRewardListener())
            else Log.e(
                "RewardedAdCompat",
                "The initial Context was not an Activity & " +
                        "the instance passed to `showAd(activity)` is also null"
            )
        }
    }

    /**
     * Set an ad listener to the interstitial ad.
     *
     * @param rewardedAdListenerCompat Different listener to handle RewardedAd callbacks
     */
    fun setAdListener(rewardedAdListenerCompat: RewardedAdListenerCompat) {
        this.adListenerCompat = rewardedAdListenerCompat
    }

    /**
     * Set a paid event listener (new api)
     * @see [OnPaidEventListener]
     */
    fun setOnPaidEventListener(paidEventListener: OnPaidEventListener) {
        this.paidEventListener = paidEventListener
    }

    /**
     * Sets the server side verification options to the underlying api
     * @see RewardedAd.setServerSideVerificationOptions
     */
    fun setServerSideVerificationOptions(serverSideVerificationOptions: ServerSideVerificationOptions) {
        this.serverSideVerificationOptions = serverSideVerificationOptions
    }

    /**
     * Set the metadata changed listener to the underlying api
     * @see RewardedAd.setOnAdMetadataChangedListener
     */
    fun setOnAdMetadataChangedListener(metadataChangedListener: OnAdMetadataChangedListener) {
        this.metadataChangedListener = metadataChangedListener
    }

    /**
     * Return the current Ad Unit Id
     */
    fun getAdUnitId() = this.adUnitId

    /**
     * Returns the [RewardItem] from the loaded ad.
     *
     * Should only be called after the ad is loaded
     *
     * @see RewardedAd.getRewardItem
     */
    @Nullable
    fun getRewardItem() = this.rewardItem

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
     * @return [RewardedAd.getResponseInfo]
     */
    @Nullable
    fun getResponseInfo() = this.responseInfo

    /**
     * Return the server side verification options, if set.
     */
    @Nullable
    fun getServerSideVerificationOptions() = this.serverSideVerificationOptions

    /**
     * Return the metadata changed listener, if set.
     */
    @Nullable
    fun getOnMetaDataChangedListener() = this.metadataChangedListener

    private fun getOnUserEarnedRewardListener() =
        OnUserEarnedRewardListener { adListenerCompat?.onUserEarnedReward(it) }

    private fun getRewardedAdLoadCallback() = object : RewardedAdLoadCallback() {
        override fun onAdLoaded(ad: RewardedAd) {
            isAdLoading = false
            this@RewardedAdCompat.rewardedAd = ad
            rewardedAd!!.also {
                it.setImmersiveMode(isImmersive)
                this@RewardedAdCompat.apply {
                    responseInfo = it.responseInfo
                    metadataChangedListener?.let { listener ->
                        it.onAdMetadataChangedListener = listener
                    }
                    serverSideVerificationOptions?.let { options ->
                        it.setServerSideVerificationOptions(options)
                    }
                    paidEventListener?.let { listener -> it.onPaidEventListener = listener }
                }
                it.fullScreenContentCallback = getFullScreenCallback()
            }

            adListenerCompat?.onAdLoaded()
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            isAdLoading = false
            adListenerCompat?.onAdFailedToLoad(loadAdError)
        }
    }

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
        val rewardedTestAdUnitId = "ca-app-pub-3940256099942544/5224354917"
    }
}