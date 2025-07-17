package com.iobits.tech.app.ai_identifier.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityReadyToIdentifyBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener

class ReadyToIdentifyActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityReadyToIdentifyBinding.inflate(layoutInflater)
    }
    var detect: String? = Constants.WHAT_IS_THIS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        detect = intent.getStringExtra(Constants.DETECT)
        initViews()
        setImg()
        showAd()
    }

    private fun initViews() {

        binding.image.setImageDrawable(ContextCompat.getDrawable(this, setImg()))

        binding.backBtn.setSafeOnClickListener {
            onBackPressed()
        }

        binding.scanNowBtn.setSafeOnClickListener {
            val intent = if (detect == Constants.WHAT_IS_THIS) Intent(this, CameraLiveScanningActivity::class.java)
            else Intent(this, CameraActivity::class.java)
            intent.putExtra(Constants.DETECT, detect)
            startActivity(intent)
            finish()
        }

    }

    private fun setImg(): Int{
        return when(detect){
            Constants.WHAT_IS_THIS -> {
                R.drawable.objects_main_img
            }
            Constants.DOG -> {
                R.drawable.dog_img_main
            }
            Constants.CAT -> {
                R.drawable.cat_img_main
            }
            Constants.INSECT -> {
                R.drawable.insect_img_main
            }
            Constants.BIRD -> {
                R.drawable.bird_img_main
            }
            Constants.PLANT -> {
                R.drawable.plants_main_img
            }
            Constants.OBJECT -> {
                R.drawable.objects_main_img
            }
            Constants.MUSHROOM -> {
                R.drawable.mushrooms_main_img
            }
            Constants.ROCK -> {
                R.drawable.stones_main_img
            }
            Constants.ORIGIN -> {
                R.drawable.country_img_main
            }
            Constants.CELEBRITY -> {
                R.drawable.celebrity_img_main
            }

            else -> { R.drawable.objects_main_img}
        }
    }

    private fun showAd() {
        MyApplication.getInstance().adsManager.loadNativeAd(
            this,
            binding.adFrame,
            AdsManager.NativeAdType.MEDIA_SMALL_NEW,
            this.getString(R.string.ADMOB_NATIVE_WITHOUT_MEDIA_V2_R_IDENT),
            binding.shimmerLayout
        )
    }
}