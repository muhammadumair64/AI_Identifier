package com.iobits.tech.app.ai_identifier.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.FragmentSettingsBinding
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.manager.RewardAdManager
import com.iobits.tech.app.ai_identifier.ui.activities.PremiumProActivity
import com.iobits.tech.app.ai_identifier.ui.activities.ReadyToIdentifyActivity
import com.iobits.tech.app.ai_identifier.utils.AdsCounter
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil.showCustomDialog
import com.iobits.tech.app.ai_identifier.utils.openRatingBottomSheet
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.max

class SettingsFragment : Fragment() {

    private val binding by lazy {
        FragmentSettingsBinding.inflate(layoutInflater)
    }

    private var whatToDetect = Constants.OBJECT

    private  var tvHours: TextView? =  null
    private  var tvMinutes: TextView? = null
    private  var tvSeconds: TextView? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        initListeners()
        disCountCard()
        return binding.root
    }

    private fun initListeners() {

        if (MyApplication.getInstance().preferenceManager.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM, false)) {
            binding.csPro.visibility = View.GONE
            binding.premiumCard.visibility = View.GONE
            binding.dogCountCard.visibility = View.GONE
            binding.plantCountCard.visibility = View.GONE
            binding.insectCountCard.visibility = View.GONE
            binding.catCountCard.visibility = View.GONE
            binding.birdCountCard.visibility = View.GONE
        }

        binding.premiumCard.setSafeOnClickListener {
            startActivity(Intent(requireContext(), PremiumProActivity::class.java))
        }

        binding.dogId.setSafeOnClickListener {
            openScanning(PreferenceManager.Key.ID_DOG_COUNT){
                openCameraActivity(Constants.DOG)
            }
        }

        binding.plantId.setSafeOnClickListener {
            openScanning(PreferenceManager.Key.ID_PLANT_COUNT){
            openCameraActivity(Constants.PLANT)
            }
        }

        binding.insectId.setSafeOnClickListener {
            openScanning(PreferenceManager.Key.ID_INSECT_COUNT){
            openCameraActivity(Constants.INSECT)
            }
        }

        binding.catId.setSafeOnClickListener {
            openScanning(PreferenceManager.Key.ID_CAT_COUNT){
            openCameraActivity(Constants.CAT)
            }
        }

        binding.birdsId.setSafeOnClickListener {
            openScanning(PreferenceManager.Key.ID_BIRD_COUNT){
            openCameraActivity(Constants.BIRD)
            }
        }

        binding.rateUs.setSafeOnClickListener {
            openRatingBottomSheet(requireActivity())
        }

        binding.shareApp.setSafeOnClickListener {
            try {
                MyApplication.blockOpendAd = true
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Hey, Download amazing app \"${getString(R.string.app_name)}\"." +
                                "\nDownload App from link below" +
                                "\n\nhttps://play.google.com/store/apps/details?id=${requireActivity().packageName}"
                    )
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share via")
                startActivity(shareIntent)
            }catch (_: Exception){}
        }

        binding.privacyPolicy.setSafeOnClickListener {
            try {
                MyApplication.blockOpendAd = true
                val url = "https://oasisiobits.blogspot.com/2025/02/object-identifier-app.html"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }catch (_: Exception){}
        }

        binding.customerSupport.setSafeOnClickListener {
            if (MyApplication.mInstance?.preferenceManager?.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM, false) == true) {
                MyApplication.blockOpendAd = true
                val supportEmail =
                    "mazadvocate@gmail.com" // Replace with your support email address
                val subject = "Support/Feedback "+getString(R.string.app_name)
                val feedback = getString(R.string.app_name)
                requireContext().showEmailChooser(supportEmail, subject, feedback)
            }else startActivity(Intent(requireContext(), PremiumProActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        binding.dogFreeCount.text = setScanningCount(PreferenceManager.Key.ID_DOG_COUNT)
        binding.plantFreeCount.text = setScanningCount(PreferenceManager.Key.ID_PLANT_COUNT)
        binding.insectFreeCount.text = setScanningCount(PreferenceManager.Key.ID_INSECT_COUNT)
        binding.catFreeCount.text = setScanningCount(PreferenceManager.Key.ID_CAT_COUNT)
        binding.birdFreeCount.text = setScanningCount(PreferenceManager.Key.ID_BIRD_COUNT)
    }

    private fun openScanning(key: PreferenceManager.Key, openCam: ()-> Unit) {
        val count = MyApplication.mInstance?.preferenceManager?.getInt(key,1)
        if (MyApplication.getInstance().preferenceManager.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM, false)) {
            openCam.invoke()
        }else{
            if (count != null) {
                if(count > 0){
                    openCam.invoke()
                }else{
                    limitExceedCard(requireActivity(), onCloseClick = {}, onBuyPremium = {
                        startActivity(Intent(requireContext(), PremiumProActivity::class.java))
                    }, onSuccess = {
                        MyApplication.mInstance?.preferenceManager?.put(key,count + 1)
                        openCam.invoke()
                    })
                }
            }else{
                openCam.invoke()
            }
        }
    }

    private fun setScanningCount(key: PreferenceManager.Key): String {
        val rawCount = MyApplication.mInstance?.preferenceManager?.getInt(key, 2) ?: 0
        val count = max(0, rawCount)
        return "$count Free"
    }

    private fun openCameraActivity(whatToDetect: String){
        this.whatToDetect = whatToDetect
      startCam {
          openGalleryActivity()
      }
    }

    private fun Context.showEmailChooser(
        supportEmail: String,
        subject: String,
        text: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }

        try {
            val chooser = Intent.createChooser(intent, "Send Email")
            if (chooser.resolveActivity(packageManager) != null) {
                startActivity(chooser)
            } else {
                Toast.makeText(this, getString(R.string.no_email_client_found), Toast.LENGTH_SHORT).show()
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.no_email_client_found), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Permissions
     */
    private fun startCam(onPermissionGiven: ()-> Unit){
        if(isInternetAvailable(requireContext()))
            requestCameraPermissions(onPermissionGiven)
        else noInternetDialogue(onPermissionGiven)
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun requestCameraPermissions(onPermissionGiven: ()-> Unit) {
        var permissionsArray: Array<String>? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionsUtil.allPermissionsGranted13(requireContext())) {
                permissionsArray = PermissionsUtil.REQUIRED_PERMISSIONS_13
                permissionLauncherCam.launch(permissionsArray)
            }else{
                onPermissionGiven.invoke()
            }

        } else {
            if (!PermissionsUtil.allPermissionsGranted(requireContext())) {
                permissionsArray = PermissionsUtil.REQUIRED_PERMISSIONS
                permissionLauncherCam.launch(permissionsArray)
            }else{
                onPermissionGiven.invoke()
            }
        }

    }

    private fun noInternetDialogue (onPermissionGiven: ()-> Unit) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.no_internet_dialogue)
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.setCancelable(false)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        dialog.findViewById<View>(R.id.cancel).setSafeOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.Retry).setSafeOnClickListener {
            dialog.dismiss()
            if(
                isInternetAvailable(requireContext())
            ){
                requestCameraPermissions(onPermissionGiven)
            }else{
                noInternetDialogue(onPermissionGiven)
            }
        }


        try {
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private val permissionLauncherCam = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val grantedPermissions = permissions.filter { it.value }.map { it.key }
        val deniedPermissions = permissions.filterNot { it.value }.map { it.key }

        if (grantedPermissions.isNotEmpty()) {
            if (grantedPermissions.size == permissions.size){
                startCam {
                    openGalleryActivity()
                }
            }

        }
        if (deniedPermissions.isNotEmpty()) {
            if (deniedPermissions.all {
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        it
                    )
                }
            ) {
                showCustomDialog(requireContext())
            } else {
                startCam {
                    openGalleryActivity()
                }
            }
        }
    }

    private fun openGalleryActivity() {
        AdsCounter.showInterAds(requireActivity()) {
            AnalyticsManager.logEvent("cmra_stngs")
            val intent = Intent(requireContext(), ReadyToIdentifyActivity::class.java)
            intent.putExtra(Constants.DETECT, this.whatToDetect)
            startActivity(intent)
        }
    }
    private fun limitExceedCard(
        context: Activity,
        onCloseClick: () -> Unit = {},
        onBuyPremium: () -> Unit,
        onSuccess: () -> Unit
    ) {
        RewardAdManager.limitExceedCard(context = context,onCloseClick = onCloseClick, onBuyPremium = onBuyPremium,
            afterAdSuccess = onSuccess,
            onFailedAdSuccess = {}
        )
    }


    /**
     * Discount Card
     */

    private fun disCountCard(){

        binding.discountBanner.root.setSafeOnClickListener {
            AnalyticsManager.logEvent("purchas_trail")
            MyApplication.showPremiumForTrail = true
            startActivity(Intent(requireContext(), PremiumProActivity::class.java))
        }

        if(MyApplication.mInstance?.preferenceManager?.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM) == true){
            showFinishedState()
        } else {
            runDiscountTimer()
            setAnimation()

        }
    }

    private fun setAnimation() {
        val shake: Animation = AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.shake)
        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                withContext(Dispatchers.Main) {
                    binding.discountBanner?.next?.startAnimation(shake) // starts animation
                }
                delay(3000)
            }
        }
    }

    private fun runDiscountTimer(){
        tvHours = binding.discountBanner.tvHours
        tvMinutes = binding.discountBanner.tvMinutes
        tvSeconds = binding.discountBanner.tvSeconds

        val sharedPreferences = requireContext().getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
        val startTime = sharedPreferences.getLong("startTime", 0)
        val currentTime = System.currentTimeMillis()

        if (startTime == 0L) {
            // Save first-time start timestamp
            sharedPreferences.edit().putLong("startTime", currentTime).apply()
            startCountdown(12 * 60 * 60 * 1000) // Full 12-hour timer
        } else {
            // Calculate elapsed time
            val elapsedTime = currentTime - startTime
            val remainingTime = (12 * 60 * 60 * 1000) - elapsedTime

            if (remainingTime > 0) {
                startCountdown(remainingTime)
            } else {
                showFinishedState()
            }
        }
    }

    private fun startCountdown(timeInMillis: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                tvHours?.text = String.format("%02d", hours)
                tvMinutes?.text = String.format("%02d", minutes)
                tvSeconds?.text = String.format("%02d", seconds)
            }

            override fun onFinish() {
                showFinishedState()
            }
        }.start()
    }

    private fun showFinishedState() {
        tvHours?.text = "00"
        tvMinutes?.text = "00"
        tvSeconds?.text = "00"
        binding?.discountBanner?.root?.visibility = View.GONE
    }

}