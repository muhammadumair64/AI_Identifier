package com.iobits.tech.app.ai_identifier.ui.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityMainBinding
import com.iobits.tech.app.ai_identifier.databinding.BackPressDialogueBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.ui.adapters.HomeViewPagerAdapter
import com.iobits.tech.app.ai_identifier.utils.openRatingBottomSheet
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: HomeViewPagerAdapter
    private var backPressedTime: Long = 0
    private var backToast: Toast? = null
    private var showRattingCount = 1
    private val TAG = "MainActivity"

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
        MyApplication.mInstance?.preferenceManager?.put(
            PreferenceManager.Key.IS_APP_FIRST_RUN,
            false
        )
        if (MyApplication.getInstance().preferenceManager.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM, false)) {
            binding.premiumBtn.visibility = View.GONE
        }else startActivity(Intent(this, PremiumProActivity::class.java))
        binding.premiumBtn.setSafeOnClickListener {
            startActivity(Intent(this, PremiumProActivity::class.java))
        }
        AnalyticsManager.logEvent("start_main")
        initViewPager()
        showAd()
    }

    override fun onResume() {
        super.onResume()
        if (MyApplication.moveToHistory){
            MyApplication.moveToHistory = false
//            binding.bubbleNav.setSelected(1)
            setTitleViewPager(2)
        }

        if (MyApplication.mInstance?.preferenceManager?.getBoolean(PreferenceManager.Key.rateUsKey,false) == false) {

            if (MyApplication.scanSuccess) {
                MyApplication.scanSuccess = false
                openRatingBottomSheet(this)
            } else {
                if (MyApplication.canShowRatting) {
                    MyApplication.canShowRatting = false
                    showRattingCount += 1
                    if (showRattingCount >= 2) {
                        showRattingCount = 0
                        openRatingBottomSheet(this)
                    }
                }
            }
        }

    }

    private fun initViewPager() {
        adapter = HomeViewPagerAdapter(this)
        binding.viewpager.offscreenPageLimit = 6
        binding.viewpager.isUserInputEnabled = false
        binding.viewpager.adapter = this.adapter

        setTitleViewPager(1)
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
//                binding.bubbleNav.setSelected(position)
            }
        })

        binding.bottomNavBar.home.setSafeOnClickListener {
            setTitleViewPager(1)
        }

        binding.bottomNavBar.history.setSafeOnClickListener {
            setTitleViewPager(2)
        }

        binding.bottomNavBar.settings.setSafeOnClickListener {
            setTitleViewPager(3)
        }


    }

    private fun setTitleViewPager(id: Int){
        when(id){
            1-> {
                binding.viewpager.currentItem = 0
                binding.screenTitle.text = getString(R.string.scan_identify)
                binding.screenTitle.visibility = View.GONE
                resetNav()
                binding.bottomNavBar.apply {
                    homeSelectorBg.visibility = View.VISIBLE
                    iconHome.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.ic_home_selected
                        )
                    )
                }
            }
            2-> {
                binding.viewpager.currentItem = 1
                binding.screenTitle.text = getString(R.string.history)
                binding.screenTitle.visibility = View.VISIBLE
                resetNav()
                binding.bottomNavBar.apply {
                    historySelectedBg.visibility = View.VISIBLE
                    iconHistory.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.ic_history_selected
                        )
                    )
                }
            }
            3-> {
                binding.viewpager.currentItem = 2
                binding.screenTitle.text = getString(R.string.settings)
                binding.screenTitle.visibility = View.VISIBLE
                resetNav()
                binding.bottomNavBar.apply {
                    settingsSelectedBg.visibility = View.VISIBLE
                    iconSettings.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.ic_settings_selected
                        )
                    )
                }
            }
        }
    }

    private fun resetNav(){
        binding.bottomNavBar.apply {
            homeSelectorBg.visibility = View.GONE
            historySelectedBg.visibility = View.GONE
            settingsSelectedBg.visibility = View.GONE

            iconHome.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_home_unselected))
            iconHistory.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_history_unselected))
            iconSettings.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_settings_unselected))
        }
    }

    override fun onBackPressed() {
        showExitDialog()
//        if (backPressedTime + 2000 > System.currentTimeMillis()) {
//            backToast?.cancel()
//            moveTaskToBack(false)
//        } else {
//            backToast = Toast.makeText(
//                this,
//                getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT
//            )
//            backToast?.show()
//        }
//        backPressedTime = System.currentTimeMillis()
    }

    /**
     * Ad Show
     */

    private val adHandler = Handler(Looper.getMainLooper())
    private val adRunnable = object : Runnable {
        override fun run() {
            if (isAdShowing) {
                showAd()
                adHandler.postDelayed(this, 12_000)
            }
        }
    }

    private var isAdShowing = false

    override fun onStart() {
        super.onStart()
        if (isAdShowing) {
            adHandler.post(adRunnable)
        }
    }

    override fun onStop() {
        super.onStop()
        adHandler.removeCallbacks(adRunnable)
    }

    fun showAd() {
        // Your ad display logic here
        MyApplication.getInstance().adsManager.loadNativeAd(
            this,
            binding.adFrame,
            AdsManager.NativeAdType.MEDIA_SMALL_NEW,
            getString(R.string.ADMOB_NATIVE_WITHOUT_MEDIA_V2_HOME),
            binding.shimmerLayout
        )
        // For example, show a dialog or open ad view
        if (!isAdShowing) {
            isAdShowing = true
            adHandler.post(adRunnable)
        }
    }

    fun hideAd() {
        isAdShowing = false
        adHandler.removeCallbacks(adRunnable)
    }

    private fun showExitDialog() {
        val bindingExit = BackPressDialogueBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(bindingExit.root)
        // Optional: Match width with margin (already set in layout)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        MyApplication.getInstance().adsManager.loadNativeAd(
            this,
            bindingExit.adFrame,
            AdsManager.NativeAdType.MEDIA_SMALL_NEW,
            getString(R.string.ADMOB_NATIVE_WITHOUT_MEDIA_V2_HOME),
            bindingExit.shimmerLayout
        )

        bindingExit.yesTv.setSafeOnClickListener {
            dialog.dismiss()
            moveTaskToBack(false)

        }

        bindingExit.noTv.setSafeOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


}