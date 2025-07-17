package com.iobits.tech.app.ai_identifier.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.iobits.tech.app.ai_identifier.databinding.BuyProOrWatchAdDialogBinding
import com.iobits.tech.app.ai_identifier.manager.RewardAdManager
import com.iobits.tech.app.ai_identifier.ui.fragments.bottomSheetFragment.RatingBottomSheetFragment


fun Context.loadImageBitmapBigSize(url: String): Bitmap? {
    return try {
        Glide.with(this).asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .submit(720 //px
                , 720 //px
            )
            .get()
    } catch (e: Exception) {
        null
    }
}

fun setLightStatusBar(activity: Activity, boolean: Boolean = true) {
    if (boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API 30) and above
            activity.window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else
        // For Android 6.0 (API 23) to Android 10 (API 29)
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }else{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API 30) and above
            activity.window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else
        // For Android 6.0 (API 23) to Android 10 (API 29)
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }
}

fun hideSystemUI(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.insetsController?.let {
            it.hide(WindowInsets.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}


fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

/**
 * Ratting Bottom Sheet
 */

fun openRatingBottomSheet(activity: FragmentActivity) {
//    ableToShowRattingCounter += 1
    val bottomSheetFragment = RatingBottomSheetFragment()
    bottomSheetFragment.show(activity.supportFragmentManager, bottomSheetFragment.tag)
}

