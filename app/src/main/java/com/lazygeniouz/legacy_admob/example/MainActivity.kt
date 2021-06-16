package com.lazygeniouz.legacy_admob.example

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.lazygeniouz.legacy_admob.interstitial.InterstitialAdCompat
import com.lazygeniouz.legacy_admob.listener.AdListenerCompat
import com.lazygeniouz.legacy_admob.listener.RewardedAdListenerCompat
import com.lazygeniouz.legacy_admob.rewarded.RewardedAdCompat

class MainActivity : AppCompatActivity() {
    private lateinit var interstitialCompat: InterstitialAdCompat
    private lateinit var rewardedAdCompat: RewardedAdCompat

    private lateinit var state: TextView
    private lateinit var rewarded: Button
    private lateinit var interstitial: Button

    override fun onStart() = super.onStart().also { MobileAds.initialize(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpRewarded()
        setUpInterstitial()

        state = findViewById(R.id.state)
        rewarded = findViewById(R.id.rewarded)
        interstitial = findViewById(R.id.interstitial)

        rewarded.setOnClickListener {
            if (rewardedAdCompat.isLoading()) return@setOnClickListener
            interstitial.isEnabled = false
            state.text = "Status: Loading Rewarded Ad"
            rewardedAdCompat.loadAd()
        }

        interstitial.setOnClickListener {
            if (interstitialCompat.isLoading()) return@setOnClickListener
            rewarded.isEnabled = false
            state.text = "Status: Loading Interstitial Ad"
            interstitialCompat.loadAd()
        }
    }

    private fun setUpRewarded() {
        rewardedAdCompat = RewardedAdCompat(this)
        rewardedAdCompat.setAdListener(object : RewardedAdListenerCompat() {
            override fun onAdLoaded() {
                state.text = "Status: RewardedAd Loaded"
                interstitial.isEnabled = true
                rewardedAdCompat.showAd()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                state.text = "Status: RewardedAd Failed to Load, Error = $error"
                interstitial.isEnabled = true
            }

            override fun onUserEarnedReward(reward: RewardItem) {
                state.text = "Status: Reward Earned"
            }
        })
    }

    private fun setUpInterstitial() {
        interstitialCompat = InterstitialAdCompat(this).apply {
            setAdListener(object : AdListenerCompat() {
                override fun onAdLoaded() {
                    state.text = "Status: Interstitial Loaded"
                    rewarded.isEnabled = true
                    interstitialCompat.showAd()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    state.text = "Status: Interstitial Failed to Load, Error = $error"
                    rewarded.isEnabled = true
                }
            })
        }
    }
}