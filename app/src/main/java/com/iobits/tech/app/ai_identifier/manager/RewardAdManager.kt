package com.iobits.tech.app.ai_identifier.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.MyApplication.Companion.showOpenAd
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener

object RewardAdManager {

    private var showRewardedInterstitialAd = true
    private var rewardAdCountDownTimer: CountDownTimer? = null

    fun showRewardedAd(context: Context, activity: Activity, onAdSuccess: () -> Unit, onFailed: ()-> Unit) {
        showOpenAd = false
        val rewardLoadingDialog = createLoadingDialog(context)
         rewardAdCountDownTimer = object : CountDownTimer(7000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                rewardLoadingDialog?.findViewById<TextView>(R.id.textView4)?.text= "Loading ad in ${millisUntilFinished / 1000} seconds ..."
            }

            override fun onFinish() {
                showRewardedInterstitialAd = false
                if (rewardLoadingDialog != null) {
                    dismissLoadingDialog(rewardLoadingDialog)
                }
                showOpenAd = true
                showToast(context, activity.getString(R.string.reward_ad_not))
                onFailed.invoke()
            }
        }

        rewardLoadingDialog?.show()
        if (rewardLoadingDialog?.window != null) {
            rewardLoadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        rewardAdCountDownTimer?.start()
        rewardLoadingDialog?.let { loadRewardedAd(context, activity, it, onAdSuccess, onFailed) }
    }

    private fun createLoadingDialog(context: Context): AlertDialog? {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_ad_loading, null)
        builder.setView(dialogView)
        return builder.setCancelable(false).create()
    }

    private fun loadRewardedAd(
        context: Context,
        activity: Activity,
        rewardLoadingDialog: AlertDialog,
        onAdSuccess: () -> Unit,
        onAdFailed: () -> Unit,
    ) {
        showRewardedInterstitialAd = true
        MyApplication.mInstance?.adsManager?.loadRewardVideoAd(activity, object : AdsManager.IRewardVideo {
            override fun onFailedToLoad() {
                dismissLoadingDialog(rewardLoadingDialog)
                cancelTimer()
                showOpenAd = true
                showToast(context, activity.getString(R.string.reward_ad_not))
                onAdFailed.invoke()
            }

            override fun onRewardVideoLoad() {
                if (showRewardedInterstitialAd) {
                    MyApplication.mInstance?.adsManager?.showRewardVideoAd(activity, this)
                    cancelTimer() // Cancel the timer as the ad is loading
                }
                dismissLoadingDialog(rewardLoadingDialog)
            }

            override fun onFailedToShow() {
                dismissLoadingDialog(rewardLoadingDialog)
                cancelTimer()
                showToast(context, activity.getString(R.string.reward_ad_not))
                showOpenAd = true
                onAdFailed()
            }

            override fun onRewardedSuccess() {
                dismissLoadingDialog(rewardLoadingDialog)
                cancelTimer()
                showOpenAd = true
                onAdSuccess()
            }
        })
    }

    private fun cancelTimer() {
        rewardAdCountDownTimer?.cancel()
        rewardAdCountDownTimer = null
    }

    private fun dismissLoadingDialog(dialog: AlertDialog) {
        if (dialog.isShowing) dialog.dismiss()
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Reward Ad Dilogs
    fun watchAdOrBuyPremium(
        context: Activity,
        onCloseClick: () -> Unit = {},
        onBuyPremium: () -> Unit,
        afterAdSuccess: () -> Unit,
        onFailedAdSuccess: () -> Unit = {},
    ) {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(context)
        val dialogView = context.layoutInflater.inflate(R.layout.buy_pro_or_watch_ad_dialog, null)
        val buyPremium = dialogView?.findViewById<MaterialCardView>(R.id.buyPremium)
        val watchAd = dialogView?.findViewById<MaterialCardView>(R.id.watchAdAndContinue)
        val closebtn = dialogView?.findViewById<ImageView>(R.id.closeBtn)
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        closebtn?.setSafeOnClickListener {
            alertDialog.dismiss()
            onCloseClick.invoke()
        }
        buyPremium?.setSafeOnClickListener {
            alertDialog.dismiss()
            onBuyPremium.invoke()
        }
        watchAd?.setSafeOnClickListener {
            alertDialog.dismiss()
            showRewardedAd(context, context, onAdSuccess = afterAdSuccess, onFailed = onFailedAdSuccess)
        }
    }

    fun limitExceedCard(
        context: Activity,
        onCloseClick: () -> Unit = {},
        onBuyPremium: () -> Unit,
        afterAdSuccess: () -> Unit = {},
        onFailedAdSuccess: () -> Unit,
    ) {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(context)
        val dialogView = context.layoutInflater.inflate(R.layout.limit_exceed_dialog, null)
        val buyPremium = dialogView?.findViewById<MaterialCardView>(R.id.buyPremium)
        val scanNewObject = dialogView?.findViewById<TextView>(R.id.scanDiffrent)
        val watchAd = dialogView?.findViewById<MaterialCardView>(R.id.watch_ad)
        val closebtn = dialogView?.findViewById<ImageView>(R.id.cross_btn)
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        closebtn?.setSafeOnClickListener {
            alertDialog.dismiss()
            onCloseClick.invoke()
        }
        buyPremium?.setSafeOnClickListener {
            alertDialog.dismiss()
            onBuyPremium.invoke()
        }
        scanNewObject?.setSafeOnClickListener {
            alertDialog.dismiss()
            onCloseClick.invoke()
        }
        watchAd?.setSafeOnClickListener {
            alertDialog.dismiss()
            showRewardedAd(context, context, onAdSuccess = afterAdSuccess, onFailed = onFailedAdSuccess)
        }
    }
}
