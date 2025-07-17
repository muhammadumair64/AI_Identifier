package com.iobits.tech.app.ai_identifier.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.URLUtil
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityPremiumProBinding
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PremiumProActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPremiumProBinding.inflate(layoutInflater)
    }
    
    private val TAG = "PremiumProActivity"
    
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
        setPrice()

        val itemSku = if (MyApplication.showPremiumForTrail){
            MyApplication.showPremiumForTrail = false
            setPriceTrail()
            binding.planText.text = getString(R.string.free_trail)
            binding.offerIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.free_trail_ic))
            binding.removeAdBuy.text = getString(R.string.claim_now)
            Constants.ITEM_SKU_PRO_USER_SUB_TRIAL
        }else {
            setPrice()
            binding.planText.text = getString(R.string.weekly_plan)
            binding.offerIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.offer_icon))
            binding.removeAdBuy.text = getString(R.string.start_now)
            Constants.ITEM_SKU_PRO_USER_SUB

        }

        binding.removeAdBuy.setSafeOnClickListener {
            AnalyticsManager.logEvent("purchasing_premium")
            MyApplication.mInstance?.billingManagerV5?.subscription(
                this, itemSku
            )
        }
        binding.crossBtn.setSafeOnClickListener {
            if (MyApplication.showInterOnPremium) {
                MyApplication.showInterOnPremium = false
                MyApplication.getInstance().adsManager.loadInterstitialAd(activity = this, isFromPro = true, onAdClick = {
                    finish()
                })
            }else {
                finish()
            }
        }

        binding.howToCancel.setSafeOnClickListener {
            showCancelSub()
        }

        binding.termOfServices.setSafeOnClickListener {
            termsOfServices()
        }

        binding.privacyPolicy.setSafeOnClickListener {
            privacyPolicy()
        }

        onShakeImage()
    }

    override fun onResume() {
        super.onResume()
        MyApplication.blockOpendAd = true
    }

    private fun setPrice() {
        try {
            if(Constants.afterDiscount == "" || Constants.afterDiscount.isEmpty()){
                    if(Constants.oneTimeProductPremiumPrice.isEmpty()){
                        binding.discountedPrice.text = "$3.99/Week"
                        binding.actualPrice.visibility = View.GONE
                        binding.offerIcon.visibility = View.GONE
                    }else{
                        binding.discountedPrice.text = "${Constants.oneTimeProductPremiumPrice}/Week"
                        binding.actualPrice.visibility = View.GONE
                        binding.offerIcon.visibility = View.GONE
                    }
            } else {
                binding.discountedPrice.text = "${Constants.oneTimeProductPremiumPrice}/Week for one week"
                binding.actualPrice.text = "then ${Constants.afterDiscount}/Week"
                binding.actualPrice.visibility = View.VISIBLE
                binding.offerIcon.visibility = View.VISIBLE
//                binding.cont.text = "Get  50% Off  in ${Constants.oneTimeProductPremiumPrice}/Week for two weeks, Then ${Constants.afterDiscount}/Week"
            }

        } catch (e: Exception) {
            Log.d("PREMIUM_PRO", "ERROR : ${e.localizedMessage}")
        }
    }

    private fun setPriceTrail() {
        try {
            if(Constants.afterDiscountTrail == "" || Constants.afterDiscountTrail.isEmpty()){
                    if(Constants.oneTimeProductPremiumPriceTrail.isEmpty()){
                        binding.discountedPrice.text = "$3.99/Week"
                        binding.actualPrice.visibility = View.GONE
                        binding.offerIcon.visibility = View.GONE
                    }else{
                        binding.discountedPrice.text = "${Constants.oneTimeProductPremiumPriceTrail}/Week"
                        binding.actualPrice.visibility = View.GONE
                        binding.offerIcon.visibility = View.GONE
                    }
            } else {
                binding.discountedPrice.text = "${Constants.oneTimeProductPremiumPriceTrail} for three days"
                binding.actualPrice.text = "then ${Constants.afterDiscountTrail}/Week"
                binding.actualPrice.visibility = View.VISIBLE
                binding.offerIcon.visibility = View.VISIBLE
//                binding.cont.text = "Get  50% Off  in ${Constants.oneTimeProductPremiumPrice}/Week for two weeks, Then ${Constants.afterDiscount}/Week"
            }

        } catch (e: Exception) {
            Log.d("PREMIUM_PRO", "ERROR : ${e.localizedMessage}")
        }
    }


    private fun showCancelSub() {
        try {
            if (URLUtil.isValidUrl(getString(R.string.cancel_subscription_url))) {
                val i = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.cancel_subscription_url))
                )
                MyApplication.blockOpendAd = true
                this.startActivity(i)
            }
        }catch (e: Exception){
            Log.d(TAG, "showCancelSub: ${e.localizedMessage}")
        }
    }

    private fun privacyPolicy(){
        try {
            MyApplication.blockOpendAd = true
            val url = "https://oasisiobits.blogspot.com/2025/02/object-identifier-app.html"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }catch (e: Exception){
            Log.d(TAG, "privacyPolicy: ${e.localizedMessage}")
        }
    }

    private fun termsOfServices(){
        try {
            MyApplication.blockOpendAd = true
            val url = "https://oasisiobits.blogspot.com/2025/02/object-identifier-app.html"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }catch (e: Exception){
            Log.d(TAG, "privacyPolicy: ${e.localizedMessage}")
        }
    }

    private fun onShakeImage() {
        val shake: Animation = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.shake
        )
        binding.removeAdBuy.startAnimation(shake) // starts animation
        lifecycleScope.launch {
            while (true) {
                delay(2000)
                withContext(Dispatchers.Main) {
                    val shakeAnim: Animation = AnimationUtils.loadAnimation(
                        applicationContext,
                        R.anim.shake
                    )
                    binding.removeAdBuy.startAnimation(shakeAnim) // starts animation
                }
            }
        }
    }

    override fun onDestroy() {
        MyApplication.showInterOnPremium = false
        super.onDestroy()
    }
}

