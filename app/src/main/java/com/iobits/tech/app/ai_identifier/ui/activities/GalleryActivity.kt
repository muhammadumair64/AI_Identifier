package com.iobits.tech.app.ai_identifier.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.iobits.tech.app.ai_identifier.databinding.ActivityGalleryBinding
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.ui.adapters.ImagesAdapter
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.ImageFetcher
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GalleryActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGalleryBinding.inflate(layoutInflater)
    }

    private var imageFetcher: ImageFetcher? = null
    var from = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AnalyticsManager.logEvent("IN_GALLERY")

        lifecycleScope.launch {
            delay(200)
            binding.backCross.setSafeOnClickListener {
                scanAgainClicked()
            }
            imageFetcher = ImageFetcher(this@GalleryActivity)
            val imageUris = imageFetcher?.getAllImages()

            binding.rv.also {
                it.adapter = imageUris?.let { it1 ->
                    ImagesAdapter(it1, this@GalleryActivity){ imgUri ->
                        val detect = intent.getStringExtra(Constants.DETECT)
                        val intent = Intent(this@GalleryActivity, ScanningActivity::class.java)
                        intent.putExtra(Constants.IMAG_URI, imgUri)
                        intent.putExtra(Constants.DETECT, detect)
                        Log.d("CAMERA_IMAGE", " Camera image uri::  $imgUri")
                        startActivity(intent)
                        finish()
                    }
                }
                it.layoutManager = GridLayoutManager(this@GalleryActivity, 3)
            }
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        scanAgainClicked()
    }

    private fun scanAgainClicked() {
        val detect = intent.getStringExtra(Constants.DETECT)
        val intent = Intent(this, CameraLiveScanningActivity::class.java)
        intent.putExtra(Constants.DETECT, detect)
        startActivity(intent)
        finish()
    }

}