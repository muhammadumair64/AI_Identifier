package com.iobits.tech.app.ai_identifier.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityOnBoardingBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.ui.adapters.MyPagerAdapter
import com.iobits.tech.app.ai_identifier.utils.hideSystemUI
import com.iobits.tech.app.ai_identifier.utils.setLightStatusBar
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener

class OnBoardingActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityOnBoardingBinding.inflate(layoutInflater)
    }
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v . setPadding (systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        setLightStatusBar(this, true)
//        hideSystemUI(this)

        initViews()
        showAd()
    }

    private fun showAd() {
        MyApplication.mInstance?.adsManager?.loadNativeAd(
            this,
            binding.adFrame,
            AdsManager.NativeAdType.MEDIA_SMALL,
            getString(R.string.ADMOB_NATIVE_WITH_MEDIA_V2),
            binding.shimmerLayout
        )
    }

    private fun initViews() {
        val pagerAdapter = MyPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.dotsIndicator.attachTo(binding.viewPager)
        binding.viewPager.currentItem = 0
        binding.viewPager.isUserInputEnabled = false
        changePosition()
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(p: Int) {
                position = p
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun changePosition(){

        binding.skipBtn.setSafeOnClickListener {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
            finish()
        }

        binding.nextBtn.setSafeOnClickListener {
            when(position){
                0->{
                    position++
                    binding.viewPager.setCurrentItem(position, true)
                }
                1->{
                    position++
                    binding.viewPager.setCurrentItem(position, true)
                }
                2->{
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )
                    finish()
                }
            }
        }

    }
}