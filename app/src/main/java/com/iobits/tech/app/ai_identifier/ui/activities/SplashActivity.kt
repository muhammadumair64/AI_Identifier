package com.iobits.tech.app.ai_identifier.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.manager.GoogleMobileAdsConsentManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"

    private lateinit var consentInformation: ConsentInformation
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch{
            delay(3000)
            showConsent()
        }
    }

    private fun showConsent() {
        // Log the Mobile Ads SDK version.
        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion())

        googleMobileAdsConsentManager =
            GoogleMobileAdsConsentManager.getInstance(applicationContext)
        googleMobileAdsConsentManager.gatherConsent(
            this
        ) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                Log.w(
                    TAG,
                    String.format(
                        "%s: %s",
                        consentError.getErrorCode(),
                        consentError.getMessage()
                    )
                )
            }
            if (googleMobileAdsConsentManager.canRequestAds()) {
                initializeMobileAdsSdk()
            }
            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                // Regenerate the options menu to include a privacy setting.
                invalidateOptionsMenu()
            }
        }

        // This sample attempts to load ads using consent obtained in the previous session.

        // This sample attempts to load ads using consent obtained in the previous session.
        if (googleMobileAdsConsentManager.canRequestAds()) {
            initializeMobileAdsSdk()
        }
    }

    private fun initializeMobileAdsSdk() {
        Log.d(TAG, "initializeMobileAdsSdk: ")
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Mobile Ads SDK.
        MyApplication.getInstance().adsManager.initSDK(this
        ) {
        }
        openMainActivity()
    }

    private fun openMainActivity() {
        MyApplication.showInterOnPremium = true
        val intent = if (MyApplication.mInstance?.preferenceManager?.getBoolean(
                PreferenceManager.Key.IS_APP_FIRST_RUN, true) == true) {
            Intent(this@SplashActivity, OnBoardingActivity::class.java)
        }else {
            Intent(this@SplashActivity, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }


}