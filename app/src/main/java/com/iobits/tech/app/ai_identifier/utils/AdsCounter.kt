package com.iobits.tech.app.ai_identifier.utils

import android.app.Activity
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R

object AdsCounter {
    var adsCounter = 0
    fun showAd(): Boolean {
        adsCounter++
        return adsCounter % 2 == 0
    }

    fun showInterAds(activity: Activity, action: () -> Unit) {
        if (showAd()) {
            MyApplication.getInstance().adsManager.loadInterstitialAd(activity) {
                Constants.showInterScanResult = false
                action.invoke()
            }
        } else {
            Constants.showInterScanResult = true
            action.invoke()
        }
    }
}
object AdsCounterScanning {

    var adsCounter = if (Constants.showInterScanResult) 1 else 0
    fun showAd(): Boolean {
        adsCounter++
        return adsCounter % 2 == 0
    }

    fun showInterAds(activity: Activity, action: () -> Unit) {
        if (showAd()) {
            if (Constants.showInterScanResult){
                MyApplication.getInstance().adsManager.loadInterstitialAd(activity) {
                action.invoke()
            }
            }else{
                Constants.showInterScanResult = true
                action.invoke()
            }
        } else {
            Constants.showInterScanResult = true
            action.invoke()
        }
    }
}