package com.iobits.tech.app.ai_identifier.manager

import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

interface RewardInterLoaded {
    fun onRewardedInterstitialLoaded(ad: RewardedInterstitialAd? = null)
}