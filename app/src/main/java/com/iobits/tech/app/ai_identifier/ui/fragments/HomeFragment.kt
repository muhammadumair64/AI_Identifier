package com.iobits.tech.app.ai_identifier.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.FragmentHomeBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.manager.RewardAdManager
import com.iobits.tech.app.ai_identifier.ui.activities.PremiumProActivity
import com.iobits.tech.app.ai_identifier.ui.activities.ReadyToIdentifyActivity
import com.iobits.tech.app.ai_identifier.ui.activities.ViewAllActivity
import com.iobits.tech.app.ai_identifier.ui.adapters.AllObjectsRvAdapter
import com.iobits.tech.app.ai_identifier.ui.adapters.AnimalsRvAdapter
import com.iobits.tech.app.ai_identifier.ui.adapters.OtherToolsRvAdapter
import com.iobits.tech.app.ai_identifier.utils.AdsCounter
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.LocalArrayLists
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil.showCustomDialog
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.math.max


class HomeFragment : Fragment() {

    private val binding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    private var whatToDetect = Constants.WHAT_IS_THIS
    private var interCount = 0

    private  var tvHours: TextView? =  null
    private  var tvMinutes: TextView? = null
    private  var tvSeconds: TextView? = null

    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        onShakeImage()
        onShakeCardView()
        initViews()
        initListeners()
        setPrice()
//        showAd()
        return binding.root
    }

    private fun initListeners() {
        binding.mainCard.setSafeOnClickListener{
            whatToDetect = Constants.WHAT_IS_THIS
            val count = MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.SCAN_ANYTHING_COUNT,5)
            if (MyApplication.getInstance().preferenceManager.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM, false)) {
                startCam {
                    openCameraActivity()
                }
            }else{
                if (count != null) {
                    if(count > 0){
                        startCam {
                            whatToDetect = Constants.WHAT_IS_THIS
                            openCameraActivity()
                        }
                    }else{
                        limitExceedCard(requireActivity(),
                            onBuyPremium = {
                                startActivity(Intent(requireContext(), PremiumProActivity::class.java))
                            }, onCloseClick = {}, onSuccess = {
                                startCam {
                                    whatToDetect = Constants.WHAT_IS_THIS
                                    setPremiumCount(whatToDetect)
                                    openCameraActivity()
                                }
                            }
                            )
                    }
                }else{
                    startCam {
                        whatToDetect = Constants.WHAT_IS_THIS
                        openCameraActivity()
                    }
                }
            }
        }

        binding.discountBanner.root.setSafeOnClickListener {
            AnalyticsManager.logEvent("purchas_trail")
            MyApplication.showPremiumForTrail = true
            startActivity(Intent(requireContext(), PremiumProActivity::class.java))
        }

        binding.trailCard.setSafeOnClickListener {
            AnalyticsManager.logEvent("purchas_trail")
            MyApplication.mInstance?.billingManagerV5?.subscription(
                requireActivity(), Constants.ITEM_SKU_PRO_USER_SUB_TRIAL
            )
        }

        binding.viewAllAnimals.setSafeOnClickListener {
            val intent = Intent(requireContext(), ViewAllActivity::class.java)
            intent.putExtra(Constants.VIEW_ALL_TYPES, Constants.ANIMALS)
            startActivity(intent)
        }

        binding.viewAllObjects.setSafeOnClickListener {
            val intent = Intent(requireContext(), ViewAllActivity::class.java)
            intent.putExtra(Constants.VIEW_ALL_TYPES, Constants.OBJECTS)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        interCount = 0
        initViews()
        if (MyApplication.mInstance?.preferenceManager?.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM,false) == true){
            binding.freeCountCard.visibility = View.GONE
            binding.trailCard.visibility = View.GONE
        }else{
            val count = MyApplication.mInstance?.preferenceManager?.getInt(PreferenceManager.Key.SCAN_ANYTHING_COUNT,5)
            val countN = count?.let { max(0, it) }
            binding.scanFreeCount.text = "$countN Free"
        }
    }

    private fun initViews() {
        if(MyApplication.mInstance?.preferenceManager?.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM) == true){
            showFinishedState()
        } else {
            runDiscountTimer()
            setAnimation()

        }
        val adapter = AnimalsRvAdapter(requireContext(), LocalArrayLists.animalsList(),{
            onClickMainRvItem(it)
        }){
            limitExceedCard(requireActivity(), onCloseClick = {}, onBuyPremium = {
                startActivity(Intent(requireContext(), PremiumProActivity::class.java),
                    )
            }, onSuccess = {
                setPremiumCount(it)
                onClickMainRvItem(it)
            }
                )
        }
        binding.animalsRv.apply {
            this.adapter = adapter
            this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        val adapterObjects = AllObjectsRvAdapter(requireContext(), LocalArrayLists.objectsList(),{
            onClickMainRvItem(it)
        }){
            limitExceedCard(requireActivity(), onCloseClick = {}, onBuyPremium = {
                startActivity(Intent(requireContext(), PremiumProActivity::class.java))
            }, onSuccess = {
                setPremiumCount(it)
                onClickMainRvItem(it)
            }
                )
        }
        binding.objectsRv.apply {
            this.adapter = adapterObjects
            this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        val adapterOther = OtherToolsRvAdapter(requireContext(), LocalArrayLists.otherToolsList(),{
            onClickMainRvItem(it)
        }){
            limitExceedCard(requireActivity(), onCloseClick = {}, onBuyPremium = {
                startActivity(Intent(requireContext(), PremiumProActivity::class.java))
            }, onSuccess = {
                setPremiumCount(it)
                onClickMainRvItem(it)
            })
        }
        binding.otherToolsRv.apply {
            this.adapter = adapterOther
            this.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

//        binding.mainRv.apply {
//            this.adapter = adapter
//            this.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
//        }
//        // Define the bottom padding in pixels
//        val bottomPadding = 250
//        // Add scroll listener
//        binding.mainRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
//                val visibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
//                val lastVisiblePosition = visibleItemPositions.maxOrNull() ?: 0
//                val totalItemCount = layoutManager.itemCount
//
//                // Check if the last item is visible
//                if (lastVisiblePosition == totalItemCount - 1) {
//                    // Add bottom padding
//                    recyclerView.setPadding(
//                        recyclerView.paddingLeft,
//                        recyclerView.paddingTop,
//                        recyclerView.paddingRight,
//                        bottomPadding
//                    )
//                } else {
//                    // Remove bottom padding
//                    recyclerView.setPadding(
//                        recyclerView.paddingLeft,
//                        recyclerView.paddingTop,
//                        recyclerView.paddingRight,
//                        0
//                    )
//                }
//            }
//        })
    }

    private fun onClickMainRvItem(i: String) {
        Log.d("AI_RESPONCE", "onClickMainRvItem: $i")
        whatToDetect = i
        startCam {
            openCameraActivity()
        }
    }

    private fun startCam(onPermissionGiven: ()-> Unit){
        if(
            isInternetAvailable(requireContext())
        ){
            requestCameraPermissions(onPermissionGiven)
        }else noInternetDialogue(onPermissionGiven)
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

    private val permissionLauncherCam = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val grantedPermissions = permissions.filter { it.value }.map { it.key }
        val deniedPermissions = permissions.filterNot { it.value }.map { it.key }

        if (grantedPermissions.isNotEmpty()) {
            if (grantedPermissions.size == permissions.size){
                startCam {
                    openCameraActivity()
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
                    openCameraActivity()
                }
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

    private fun openCameraActivity(){
        AdsCounter.showInterAds(requireActivity()) {
            AnalyticsManager.logEvent("cmra_home")
            val intent = Intent(requireContext(), ReadyToIdentifyActivity::class.java)
            intent.putExtra(Constants.DETECT, whatToDetect)
            startActivity(intent)
        }
    }

    private fun onShakeImage() {
        val shake: Animation = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.shake
        )
        binding.claimNowBtn.startAnimation(shake) // starts animation
        lifecycleScope.launch {
            while (true) {
                delay(2000)
                withContext(Dispatchers.Main) {
                    val shakeAnim: Animation = AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                    binding.claimNowBtn.startAnimation(shakeAnim) // starts animation
                }
            }
        }
    }

    private fun onShakeCardView() {
        val shake: Animation = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.shake
        )
        binding.cardView.startAnimation(shake) // starts animation
        lifecycleScope.launch {
            while (true) {
                delay(2000)
                withContext(Dispatchers.Main) {
                    val shakeAnim: Animation = AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.shake
                    )
                    binding.cardView.startAnimation(shakeAnim) // starts animation
                }
            }
        }
    }

    private fun setPrice() {
        try {
            if(Constants.afterDiscount == "" || Constants.afterDiscount.isEmpty()){
                if(Constants.oneTimeProductPremiumPrice.isEmpty()){
                    binding.priceTxt.text = "$3.99/Week"

                }else{
                    binding.priceTxt.text = "${Constants.oneTimeProductPremiumPrice}/Week"
                }
            } else {
                binding.priceTxt.text = "then ${Constants.afterDiscount}/Week"
//                binding.cont.text = "Get  50% Off  in ${Constants.oneTimeProductPremiumPrice}/Week for two weeks, Then ${Constants.afterDiscount}/Week"
            }

        } catch (e: Exception) {
            Log.d("PREMIUM_PRO", "ERROR : ${e.localizedMessage}")
        }
    }

    private fun showAd() {
        MyApplication.getInstance().adsManager.loadNativeAd(
            requireActivity(),
            binding.adFrame,
            AdsManager.NativeAdType.MEDIA_SMALL_NEW,
            getString(R.string.ADMOB_NATIVE_WITHOUT_MEDIA_V2_HOME),
            binding.shimmerLayout
        )
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

    private fun setPremiumCount(detect: String){
        when(detect){
            Constants.WHAT_IS_THIS -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.SCAN_ANYTHING_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.SCAN_ANYTHING_COUNT,5) + 1)

            Constants.DOG -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_DOG_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_DOG_COUNT,2) + 1)

            Constants.PLANT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_PLANT_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_PLANT_COUNT,2) + 1)

            Constants.CAT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_CAT_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_CAT_COUNT,2) + 1)

            Constants.INSECT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_INSECT_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_INSECT_COUNT,2) + 1)

            Constants.BIRD -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_BIRD_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_BIRD_COUNT,2) + 1)

            Constants.OBJECT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_OBJECT_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_OBJECT_COUNT,2) + 1)

            Constants.ROCK -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_ROCK_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_ROCK_COUNT,2) + 1)

            Constants.MUSHROOM -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_MUSHROOM_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_MUSHROOM_COUNT,2) + 1)

            Constants.CELEBRITY -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_CELEBRITY_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_CELEBRITY_COUNT,2) + 1)

            Constants.ORIGIN -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_COUNTRY_COUNT, MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_COUNTRY_COUNT,2) + 1)
        }
    }

    // Discounted Banner

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