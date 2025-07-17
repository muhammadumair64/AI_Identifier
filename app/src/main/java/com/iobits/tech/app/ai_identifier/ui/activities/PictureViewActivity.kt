package com.iobits.tech.app.ai_identifier.ui.activities

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.ImageSource
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityPictureViewBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.loadImageBitmapBigSize
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PictureViewActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPictureViewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AnalyticsManager.logEvent("in_picture_view")

        intent.extras?.let {
            val imageUrl = it.getString(Constants.IMAGE_URL_EXTRA, "")
            GlobalScope.launch {
                val bitMaps = getBitMaps(imageUrl)
                bitMaps?.let {
                    runOnUiThread {
                        binding.imageView.setImage(ImageSource.bitmap(it))
                    }
                }
            }
        }
        binding.backBtn.setSafeOnClickListener {
            onBackPressed()
        }

        showAd()

    }

    private suspend fun getBitMaps(imageUrl: String): Bitmap? {
        return loadImageBitmapBigSize(imageUrl)

    }

    private fun showAd() {
        MyApplication.mInstance?.adsManager?.loadNativeAd(
            this,
            binding.adFrame,
            AdsManager.NativeAdType.MEDIA_SMALL,
            getString(R.string.ADMOB_NATIVE_WITH_MEDIA_V2
            ),
            binding.shimmerLayout
        )
    }

}