package com.iobits.tech.app.ai_identifier.ui.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityViewAllBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.manager.RewardAdManager
import com.iobits.tech.app.ai_identifier.ui.adapters.AllObjectsRvAdapter
import com.iobits.tech.app.ai_identifier.ui.adapters.AnimalsRvAdapter
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.LocalArrayLists
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil.showCustomDialog
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener

class ViewAllActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityViewAllBinding.inflate(layoutInflater)
    }
    private var whatToDetect = "object"
    private var interCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        showAd()

    }

    private fun initViews() {

        val viewAllTypes = intent.getStringExtra(Constants.VIEW_ALL_TYPES)
        val adapter = if (viewAllTypes == Constants.ANIMALS) {
            binding.titleScreen.text = getString(R.string.all_animals)
            AnimalsRvAdapter(this, LocalArrayLists.animalsList(), {
                onClickMainRvItem(it)
            }) {
                RewardAdManager.limitExceedCard(this, onCloseClick = {}, onBuyPremium = {
                    startActivity(Intent(this, PremiumProActivity::class.java))
                },
                    afterAdSuccess = {
                        setPremiumCount(it)
                        onClickMainRvItem(it)
                    },
                    onFailedAdSuccess = {
                    })
            }
        } else {
            binding.titleScreen.text = getString(R.string.all_objects)
            AllObjectsRvAdapter(this, LocalArrayLists.objectsList(), {
                onClickMainRvItem(it)
            }) {
                RewardAdManager.limitExceedCard(this, onCloseClick = {}, onBuyPremium = {
                    startActivity(Intent(this, PremiumProActivity::class.java))
                },
                    afterAdSuccess = {
                        setPremiumCount(it)
                        onClickMainRvItem(it)
                    },
                    onFailedAdSuccess = {
                    })
            }
        }

        binding.backBtn.setSafeOnClickListener {
            onBackPressed()
        }

        binding.recyclerView.apply {
            this.adapter = adapter
            this.layoutManager = GridLayoutManager(this@ViewAllActivity, 2)
        }

    }

    private fun onClickMainRvItem(i: String) {
        Log.d("AI_RESPONCE", "onClickMainRvItem: $i")
        startCam {
            whatToDetect = i
            openCameraActivity()
        }
    }

    private fun startCam(onPermissionGiven: () -> Unit) {
        if (
            isInternetAvailable(this)
        ) {
            requestCameraPermissions(onPermissionGiven)
        } else noInternetDialogue(onPermissionGiven)
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun requestCameraPermissions(onPermissionGiven: () -> Unit) {
        var permissionsArray: Array<String>? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionsUtil.allPermissionsGranted13(this)) {
                permissionsArray = PermissionsUtil.REQUIRED_PERMISSIONS_13
                permissionLauncherCam.launch(permissionsArray)
            } else {
                onPermissionGiven.invoke()
            }

        } else {
            if (!PermissionsUtil.allPermissionsGranted(this)) {
                permissionsArray = PermissionsUtil.REQUIRED_PERMISSIONS
                permissionLauncherCam.launch(permissionsArray)
            } else {
                onPermissionGiven.invoke()
            }
        }
    }

    private val permissionLauncherCam =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val grantedPermissions = permissions.filter { it.value }.map { it.key }
            val deniedPermissions = permissions.filterNot { it.value }.map { it.key }

            if (grantedPermissions.isNotEmpty()) {
                if (grantedPermissions.size == permissions.size) {
                    startCam {
                        openCameraActivity()
                    }
                }

            }
            if (deniedPermissions.isNotEmpty()) {
                if (deniedPermissions.all {
                        !ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            it
                        )
                    }
                ) {
                    showCustomDialog(this)
                } else {
                    startCam {
                        openCameraActivity()
                    }
                }
            }
        }

    private fun noInternetDialogue(onPermissionGiven: () -> Unit) {
        val dialog = Dialog(this)
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
            if (
                isInternetAvailable(this)
            ) {
                requestCameraPermissions(onPermissionGiven)
            } else {
                noInternetDialogue(onPermissionGiven)
            }
        }


        try {
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun openCameraActivity() {
        AnalyticsManager.logEvent("o_cmra_home")
        showEnter {
            val intent = Intent(this, CameraLiveScanningActivity::class.java)
            intent.putExtra(Constants.DETECT, whatToDetect)
            startActivity(intent)
        }
    }

    private fun showEnter(onDone: ()-> Unit){
        interCount += 1
        if (interCount == 2){
            interCount = 0
            MyApplication.mInstance?.adsManager?.loadInterstitialAd(activity = this, onAdClick =  {
                onDone.invoke()
            })
        }else{
            onDone.invoke()
        }
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

}