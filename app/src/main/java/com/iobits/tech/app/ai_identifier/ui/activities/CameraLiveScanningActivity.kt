package com.iobits.tech.app.ai_identifier.ui.activities

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.common.util.concurrent.ListenableFuture
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityCameraLiveScanningBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.manager.RewardAdManager.limitExceedCard
import com.iobits.tech.app.ai_identifier.ui.viewModels.ScanningViewModel
import com.iobits.tech.app.ai_identifier.utils.AdsCounterScanning
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil.showCustomDialog
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class CameraLiveScanningActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityCameraLiveScanningBinding.inflate(layoutInflater)
    }

    lateinit var mediaPlayer: MediaPlayer
    private var imageCapture: ImageCapture? = null
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null

    private var destinationFolder: String = "DogDetector"
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashOnOff: Int? = 2
    private var isCameraRunning = false
    private var camera: Camera? = null
    private var currentZoomRatio = 1.0f
    private var isFlashOn = false
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var canStartScanningResult = true
    private var imageUri: String? = null
    private var imageUriToDisplay: String? = null
    var detect: String? = Constants.WHAT_IS_THIS
    private var isStopProcess = false
    private var couldStartToScan = false
    private var isAllowForGemini = true

    private val viewModelAI: ScanningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AnalyticsManager.logEvent("IN_CAMERA")

        flashOnOff = MyApplication.mInstance?.preferenceManager?.getInt(
            PreferenceManager.Key.CAMERA_FLASH,
            2
        )
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
         detect = intent.getStringExtra(Constants.DETECT)
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.camera_beep)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        initListeners()
        showAd()
        aiHandel()
        setPremiumCountText()
        lifecycleScope.launch {
            delay(5000) // wait 5 seconds first
            startCamFun()
        }

    }

    private fun showAd() {
        MyApplication.mInstance?.adsManager?.loadNativeAd(
            this,
            binding.adFrame,
            AdsManager.NativeAdType.MEDIA_SMALL_NEW,
            getString(R.string.ADMOB_NATIVE_WITHOUT_MEDIA_V2),
            binding.shimmerLayout
        )
    }

    private fun initListeners() {

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        // Get the camera ID for the back camera that has flash
        try {
            cameraId = cameraManager?.cameraIdList?.first { id ->
                cameraManager?.getCameraCharacteristics(id)
                    ?.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Log.d(TAG, "initListeners: ${e.localizedMessage}")
            e.printStackTrace()
        }

        // Set initial icon for flash
        updateFlashlightIcon(binding.flashLight)

        // Toggle flashlight on button click
        binding.flashLight.setSafeOnClickListener {
            toggleFlashlight(binding.flashLight)
        }

        binding.backBtn.setSafeOnClickListener {
            onBackPressed()
        }

        binding.changeCamBtn.setSafeOnClickListener{
            changeCamera()
        }

        binding.galleryBtn.setSafeOnClickListener {
            startCam{
                openGalleryActivity()
            }
        }

        binding.refreshBtn.setSafeOnClickListener {
            startCamFun()
        }

    }

    private fun startCamFun(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED){
                delay(500)
                Log.d(TAG, "startCamFun: $isAllowForGemini")
                if (isAllowForGemini) {
                    isAllowForGemini = false
                    if (!isStopProcess) startScanning {
                        binding.indicator.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@CameraLiveScanningActivity,
                                R.drawable.scanning_indicator
                            )
                        )
                        binding.refreshBtn.visibility = View.INVISIBLE
                        binding.scannedDataCard.visibility = View.INVISIBLE
                        binding.lottieAnimationView2.visibility = View.VISIBLE
                        binding.bottomOptions.visibility = View.GONE
                        clickPictureClick()
                    }
                }
            }
        }

    }


    private fun toggleFlashlight(flashBtn: ImageView) {
        try {
            isFlashOn = !isFlashOn
            Log.d(TAG, "setData FLASH: $isFlashOn")
            camera?.cameraControl?.enableTorch(isFlashOn)
//            cameraId?.let { cameraManager?.setTorchMode(it, isFlashOn) }
            updateFlashlightIcon(flashBtn)
        } catch (e: Exception) {
            Log.d(TAG, "initListeners: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    private fun updateFlashlightIcon(flashBtn: ImageView) {
        if (isFlashOn) {
            flashBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.flash_active_icon))
        } else {
            flashBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.flash_unactive_icon
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
        isCameraRunning = false
        isAllowForGemini = true
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
        }
    }

    private fun startCamera() {

        cameraProviderFuture?.addListener({
            val cameraProvider: ProcessCameraProvider? = cameraProviderFuture?.get()

            // Preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.viewFinder.surfaceProvider
                }

            // ImageAnalysis use case
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer {})
                }

            try {
                // Unbind all use cases before rebinding
                lifecycleScope.launch(Dispatchers.Main) {
                    cameraProvider?.unbindAll()
                    // Bind use cases to lifecycle
                    camera = cameraProvider?.bindToLifecycle(
                        this@CameraLiveScanningActivity, cameraSelector, preview, imageCapture, imageAnalyzer
                    )
                    currentZoomRatio = 1.0f
                    camera?.cameraControl?.setZoomRatio(currentZoomRatio)

                    // Set up pinch-to-zoom gesture
                    val zoomGesture = ScaleGestureDetector(
                        this@CameraLiveScanningActivity,
                        object : ScaleGestureDetector.OnScaleGestureListener {
                            override fun onScale(detector: ScaleGestureDetector): Boolean {
                                val scaleFactor = detector.scaleFactor
                                currentZoomRatio = (currentZoomRatio * scaleFactor).coerceIn(
                                    1.0f,
                                    camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
                                )
                                camera?.cameraControl?.setZoomRatio(currentZoomRatio)
                                return true
                            }

                            override fun onScaleBegin(detector: ScaleGestureDetector) = true
                            override fun onScaleEnd(detector: ScaleGestureDetector) {}
                        })

                    binding.viewFinder.setOnTouchListener { _, event ->
                        zoomGesture.onTouchEvent(event)
                        true
                    }

                    // Flashlight toggle on button click
                    binding.flashLight.setSafeOnClickListener {
                        isFlashOn = !isFlashOn
                        camera?.cameraControl?.enableTorch(isFlashOn)
                        updateFlashlightIcon(binding.flashLight)
                    }

                    isCameraRunning = true
                }

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        Log.d("CAMERA_FRAGMENT", "Camera Permission Granted")
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun allPermissionsGranted13() = REQUIRED_PERMISSIONS_13.all {
        Log.d("CAMERA_FRAGMENT", "Camera Permission Granted")
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED

    }

    private fun getOutputDirectory(): File {
        val mediaDir = this.externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else this.filesDir
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (allPermissionsGranted13()) {
                    startCamera()
                } else {

                    Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
//                finish()
                }

            } else {
                if (allPermissionsGranted()) {
                    startCamera()
                } else {
                    Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun clickPictureClick() {
        val timer = MyApplication.getInstance().preferenceManager.getString(
            PreferenceManager.Key.CAMERA_TIMER,
            "timerTvOff"
        )
        clickPicture(100L)
        Log.d(TAG, "clickPictureClick: $timer")
    }

    private fun clickPicture(timer: Long) {
        Log.d("CAMERA_CLICK", "clickPicture: ")
        checkTimerIsOnOrOFF()
        object : CountDownTimer(timer, 100) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {}
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()

        } catch (_: IllegalStateException) {
        }
    }

    override fun onResume() {
        super.onResume()
        binding.flashLight.visibility = View.VISIBLE
        binding.backBtn.visibility = View.VISIBLE
        binding.bottomOptions.visibility = View.GONE
        binding.lottieAnimationView2.visibility = View.VISIBLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (allPermissionsGranted13()) {
                if (!isCameraRunning) {
                    startCamera()
                }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
//                finish()
            }
        } else {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
//                finish()
            }
        }

        try {
            //if flash value is change than refresh camera
            if (flashOnOff != PreferenceManager.getInstance(this)
                    .getInt(PreferenceManager.Key.CAMERA_FLASH, 2)
            ) {
                cameraExecutor.shutdown()
                startCamera()
                flashOnOff = PreferenceManager.getInstance(this)
                    .getInt(PreferenceManager.Key.CAMERA_FLASH, 2)
            }

            val focus = PreferenceManager.getInstance(this)
                .getString(PreferenceManager.Key.CAMERA_FOCUS, "focusAutoTv")
            binding.viewFinder.controller?.isTapToFocusEnabled = !focus.equals("focusAutoTv")

            setUpDestinationFolder()
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                "something went wrong.. please try again",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpDestinationFolder() {
        val destinationFolder1 = PreferenceManager.getInstance(this).getString(PreferenceManager.Key.FOLDER_NAME, "AIDetector")
        Log.d("destinationFolder", "setUpDestinationFolder: $destinationFolder1")
        destinationFolder = destinationFolder1
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private val REQUIRED_PERMISSIONS_13 = arrayOf(
            Manifest.permission.CAMERA
        )
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()
            listener(luma)
            image.close()
        }
    }

    private fun changeCamera() {
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                startCamera()
            } else {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                startCamera()
            }
        }
    }

    private fun checkTimerIsOnOrOFF() {
        Log.d(TAG, "checkTimerIsOnOrOFF: start")
        val timer = PreferenceManager.getInstance(this)
            .getString(PreferenceManager.Key.CAMERA_TIMER, "timerTvOff")
        if (timer.equals("timerTvOff")) {
            binding.timerCountdown.visibility = View.GONE
            clickAndSavePicture()
        } else if (timer.equals("timerTv3Sec")) {
            binding.timerCountdown.visibility = View.VISIBLE
            binding.timerCountdown.text = "3"
            object : CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val time = millisUntilFinished / 1000
                    binding.timerCountdown.text = time.toString()
                }

                override fun onFinish() {
                    binding.timerCountdown.visibility = View.GONE
                    clickAndSavePicture()
                }
            }.start()
        } else if (timer.equals("timerTv5Sec")) {
            binding.timerCountdown.visibility = View.VISIBLE
            binding.timerCountdown.text = "5"
            object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val time = millisUntilFinished / 1000
                    binding.timerCountdown.text = time.toString()
                }

                override fun onFinish() {
                    binding.timerCountdown.visibility = View.GONE
                    clickAndSavePicture()
                }
            }.start()
        }
    }

    private fun clickAndSavePicture() {
        Log.d(TAG, "clickAndSavePicture: start")
        binding.progressBar.visibility = View.VISIBLE
        try {
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "clickAndSavePicture: ${e.localizedMessage}")
        }
        val sound =
            PreferenceManager.getInstance(this)
                .getString(PreferenceManager.Key.CAMERA_SOUND, "cameraSoundOff")
     
        if (PreferenceManager.getInstance(this)
                .getInt(PreferenceManager.Key.CAMERA_FLASH, 2) == 1
        ) {
            object : CountDownTimer(100, 100) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    binding.cameraClick.visibility = View.VISIBLE
                    Log.d("cameraSoundOn", "onFinish: " + sound.equals("cameraSoundOn"))
                    if (sound.equals("cameraSoundOn")) {
                        if (::mediaPlayer.isInitialized) {
                            if (!mediaPlayer.isPlaying) mediaPlayer.start()
                        }
                    }
                }
            }.start()
        } else {
            binding.cameraClick.visibility = View.VISIBLE
            Log.d("cameraSoundOn", "onFinish: " + sound.equals("cameraSoundOn"))
            if (sound.equals("cameraSoundOn")) {
                if (::mediaPlayer.isInitialized) {
                    if (!mediaPlayer.isPlaying) mediaPlayer.start()
                }
            }
        }

        //save click picture
        val photoFile = createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        if (isCameraRunning) {
            try {

                imageCapture?.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = Uri.fromFile(photoFile)
                            Log.d(TAG, "Photo saved: $savedUri")
                            lifecycleScope.launch(Dispatchers.Main) {
                                moveNext(savedUri)
                            }

                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                        }
                    }
                ) 
            }catch (e: Exception){
                Log.d(TAG, "clickAndSavePicture: ${e.localizedMessage}")
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = cacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
//            currentPhotoPath = absolutePath
        }
    }

    private fun startCam(onPermissionGiven: ()-> Unit){
        if(isInternetAvailable(this))
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
            if (!PermissionsUtil.allPermissionsGranted13Media(this)) {
                permissionsArray = PermissionsUtil.REQUIRED_PERMISSIONS_MEDIA_13
                permissionLauncherCam.launch(permissionsArray)
            }else{
                onPermissionGiven.invoke()
            }
        } else {
            if (!PermissionsUtil.allPermissionsGrantedStorage(this)) {
                permissionsArray = PermissionsUtil.REQUIRED_PERMISSIONS_STORAGE
                permissionLauncherCam.launch(permissionsArray)
            }else{
                onPermissionGiven.invoke()
            }
        }
    }

    private fun noInternetDialogue (onPermissionGiven: ()-> Unit) {
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
            if(
                isInternetAvailable(this)
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
                        this,
                        it
                    )
                }
            ) {
                showCustomDialog(this)
            } else {
                startCam {
                    openGalleryActivity()
                }
            }
        }
    }

    private fun openGalleryActivity() {
        val intent = Intent(this@CameraLiveScanningActivity, GalleryActivity::class.java)
        intent.putExtra(Constants.DETECT, detect)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    // Gemini To Get Data
    //-----------------------------------------------------------------------Move to next and Gemini----------------------------------------------------------//
    private fun moveNext(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            imageUri = uri.toString()
            viewModelAI.hitGeminiApi(this@CameraLiveScanningActivity, uri, detect ?: Constants.WHAT_IS_THIS)

            Log.d(TAG, "IMAGE URL = $uri")
        }
    }

    //------------------------------------------------------------------Gemini------------------------------------------------------------------//

    private fun aiHandel() {
        lifecycleScope.launch {
            viewModelAI.responseFlow.collect { uiState ->
                if (uiState.isNotEmpty()) {
                    if (uiState.contains("Invalid Image", ignoreCase = true) ||
                        uiState.contains("No $detect is visible in the image", ignoreCase = true)
                    ) {
                        Log.d(TAG, "aiHandel: don't move")
                        showNoResult()
                    }else if(uiState.contains("Error decoding image.", ignoreCase = true) ||
                        uiState.contains("Response canceled.", ignoreCase = true) ||
                        uiState.contains("Error generating response.", ignoreCase = true)||
                        uiState.contains("Response generation canceled.", ignoreCase = true)){
                        Log.d(TAG, "aiHandel: Error generating response.")
                        delay(3000)
                        showNoResult()
                    } else {
                        if (uiState.isNotEmpty()) {
                            setData(uiState.toString())
                        }else showNoResult()
                    }

                }
            }
        }
    }

    private fun setData(dataString: String) {
        Log.d(TAG, "setData: $dataString")
        couldStartToScan = true
        imageUriToDisplay = imageUri
        binding.scannedDataCard.setSafeOnClickListener {
            if (isFlashOn)toggleFlashlight(binding.flashLight)
            startScanningResultActivity(dataString.toString())
        }
        splitStringOnNumbers(dataString)
    }

    private fun showNoResult() {
        binding.lottieAnimationView2.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.indicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.scan_result_indicator))
        binding.scannedDataCard.visibility = View.VISIBLE
        binding.refreshBtn.visibility = View.VISIBLE
        binding.scannedImg.visibility = View.GONE
        binding.objectName.visibility = View.GONE
        binding.nextBtn.visibility = View.GONE
        binding.objectDescription.text = getString(R.string.no_data_found_for_this_object) + setNoDataText()
        couldStartToScan = true
        isAllowForGemini = true
        lifecycleScope.launch {
            delay(15000) // repeat every 15 seconds
            withContext(Dispatchers.Main) {
                startCamFun()
            }
        }
    }

    private fun startScanningResultActivity(str: String) {
        if (str.contains("encrypted")) {
//            viewModel.callback?.onShowNoResultDialogue()
        } else {
            if (canStartScanningResult) {
                isStopProcess = true
                showInterCheck{
                        lifecycleScope.launch {
                            delay(100)
                            if (canStartScanningResult) {
                                canStartScanningResult = false
                                val intent = Intent(
                                    this@CameraLiveScanningActivity,
                                    ScanningResultActivity::class.java
                                )
                                intent.putExtra("LABEL", str)
                                intent.putExtra(Constants.IMAG_URI, imageUriToDisplay)
                                intent.putExtra(Constants.DETECT, detect)
                                startActivity(intent)
                                finish()
                            }
                        }

                    }
            }
        }
    }

    private fun showInterCheck(onDismissed: ()-> Unit){
            AdsCounterScanning.showInterAds(this){
                onDismissed.invoke()
            }
    }

    private fun setNoDataText() : String{
        return when (detect) {
            Constants.WHAT_IS_THIS -> " this Object"
            Constants.DOG -> " Dog"
            Constants.PLANT -> " Plant"
            Constants.CAT -> " Cat"
            Constants.INSECT -> " Insect"
            Constants.BIRD -> " Bird"
            Constants.OBJECT -> " this Object"
            Constants.ROCK -> " Rock"
            Constants.MUSHROOM -> " Mushroom"
            Constants.CELEBRITY -> " Celebrity"
            Constants.ORIGIN -> " Country Flag"
            else -> {
                " this Object"
            }
        }
    }

    //---------------------------- Split String-----------------------------------------//
    private fun splitStringOnNumbers(input: String) {

        // Regex to match "**number:" pattern
        val regex = getRegexForNumberType(input)
        val matches = regex?.findAll(input)
        val numbers = mutableListOf<String>()
        val descriptions = mutableListOf<String>()
        var lastNumber: String? = null
        var lastIndex = 0

        if (matches != null) {
            for (match in matches) {
                val number = match.groupValues[1] // Extract number bullet
                val startIndex = match.range.first

                // Add the previous description if a number already exists
                if (lastNumber != null) {
                    val description = input.substring(lastIndex, startIndex).trim()
                    numbers.add(lastNumber)
                    descriptions.add(description)
                }

                lastNumber = number.trim()
                lastIndex = match.range.last + 1 // Move to the next section
            }
        }

        // Add the final number and description
        if (lastNumber != null && lastIndex < input.length) {
            val description = input.substring(lastIndex).trim()
            numbers.add(lastNumber)
            descriptions.add(description)
        }

        val name = descriptions.getOrNull(0)?.trim()
        binding.objectName.text = name?.trim()?.let { replaceStarWithSpace(it) }
        val detail = descriptions.getOrNull(1)?.trim()
        binding.objectDescription.text = detail?.trim()?.let { replaceStarWithSpace(it) }

        binding.lottieAnimationView2.visibility = View.GONE
        binding.indicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.scan_result_indicator))
        binding.scannedDataCard.visibility = View.VISIBLE
        binding.refreshBtn.visibility = View.VISIBLE
        binding.scannedImg.visibility = View.VISIBLE
        binding.objectName.visibility = View.VISIBLE
        binding.nextBtn.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        try {
            binding.scannedImg.setImageURI(Uri.parse(imageUriToDisplay))
        }catch (e: Exception){
            Log.d(TAG, "setData: ${e.localizedMessage}")
        }
//        setData(descriptions)
        // Log all extracted numbers and descriptions
        for (i in numbers.indices) {
            Log.d(TAG, "${numbers[i]}: ${descriptions.getOrNull(i)}")
        }
        isAllowForGemini = true
        setPremiumCount()
        setPremiumCountText()

        lifecycleScope.launch {
            delay(15000) // repeat every 15 seconds
            withContext(Dispatchers.Main) {
                startCamFun()
            }
        }
    }

    private fun getRegexForNumberType(input: String): Regex? {
        return when {
            """-\d+:""".toRegex().containsMatchIn(input) -> """-(\d+):""".toRegex() // Matches -<number>:
            """\d+:""".toRegex().containsMatchIn(input) -> """(\d+):""".toRegex()  // Matches <number>:
            else -> null
        }
    }
    private fun replaceStarWithSpace(input: String): String {
        return input.replace('*', ' ')
    }

    //--------------------------------Scan Limits--------------------------------------//
    private fun setPremiumCount(){
        when(detect){
            Constants.WHAT_IS_THIS -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.SCAN_ANYTHING_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.SCAN_ANYTHING_COUNT,5) - 1)

            Constants.DOG -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_DOG_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_DOG_COUNT,2) - 1)

            Constants.PLANT -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_PLANT_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_PLANT_COUNT,2) - 1)

            Constants.CAT -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_CAT_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_CAT_COUNT,2) - 1)

            Constants.INSECT -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_INSECT_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_INSECT_COUNT,2) - 1)

            Constants.BIRD -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_BIRD_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_BIRD_COUNT,2) - 1)

            Constants.OBJECT -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_OBJECT_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_OBJECT_COUNT,2) - 1)

            Constants.ROCK -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_ROCK_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_ROCK_COUNT,2) - 1)

            Constants.MUSHROOM -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_MUSHROOM_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_MUSHROOM_COUNT,2) - 1)

            Constants.CELEBRITY -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_CELEBRITY_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_CELEBRITY_COUNT,2) - 1)

            Constants.ORIGIN -> MyApplication.getInstance().preferenceManager
                .put(PreferenceManager.Key.ID_COUNTRY_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_COUNTRY_COUNT,2) - 1)
        }
    }
    private fun setPremiumCountText(){
        when(detect){
            Constants.WHAT_IS_THIS -> {
                binding.galleryBtn.visibility = View.GONE
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.SCAN_ANYTHING_COUNT, 5)
                    )
                    )
                }
            }

            Constants.DOG -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_DOG_COUNT, 2)
                    )
                    )
                }
            }

            Constants.PLANT -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_PLANT_COUNT, 2)
                    )
                    )
                }
            }

            Constants.CAT -> {
               binding.scanCount.text = buildString {
                   append(getString(R.string.scan_left))
                   append(" ")
                   append(
                       kotlin.math.max( 0,
                       MyApplication.getInstance().preferenceManager
                           .getInt(PreferenceManager.Key.ID_CAT_COUNT, 2)
                   )
                   )
               }
            }

            Constants.INSECT -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_INSECT_COUNT, 2)
                    )
                    )
                }
            }

            Constants.BIRD -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_BIRD_COUNT, 2)
                    )
                    )
                }
            }

            Constants.OBJECT -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_OBJECT_COUNT, 2)
                    )
                    )
                }
            }

            Constants.ROCK -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_ROCK_COUNT, 2)
                    )
                    )
                }
            }

            Constants.MUSHROOM -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_MUSHROOM_COUNT, 2)
                    )
                    )
                }
            }

            Constants.CELEBRITY -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_CELEBRITY_COUNT, 2)
                    )
                    )
                }
            }

            Constants.ORIGIN -> {
                binding.scanCount.text = buildString {
                    append(getString(R.string.scan_left))
                    append(" ")
                    append(
                        kotlin.math.max( 0,
                        MyApplication.getInstance().preferenceManager
                            .getInt(PreferenceManager.Key.ID_COUNTRY_COUNT, 2)
                    )
                    )
                }
            }
        }
    }


    private fun  startScanning(scanAgainScreen: () -> Unit){
        when (detect){
            Constants.WHAT_IS_THIS->{
                openScanning(PreferenceManager.Key.SCAN_ANYTHING_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.DOG->{
                openScanning(PreferenceManager.Key.ID_DOG_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.PLANT->{
                openScanning(PreferenceManager.Key.ID_PLANT_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.CAT->{
                openScanning(PreferenceManager.Key.ID_CAT_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.INSECT->{
                openScanning(PreferenceManager.Key.ID_INSECT_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.BIRD->{
                openScanning(PreferenceManager.Key.ID_BIRD_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.OBJECT->{
                openScanning(PreferenceManager.Key.ID_OBJECT_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.ROCK->{
                openScanning(PreferenceManager.Key.ID_ROCK_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.MUSHROOM->{
                openScanning(PreferenceManager.Key.ID_MUSHROOM_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.ORIGIN->{
                openScanning(PreferenceManager.Key.ID_COUNTRY_COUNT){
                    scanAgainScreen.invoke()
                }
            }
            Constants.CELEBRITY->{
                openScanning(PreferenceManager.Key.ID_CELEBRITY_COUNT){
                    scanAgainScreen.invoke()
                }
            }
        }
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
                    lifecycleScope.launch(Dispatchers.Main) {
                        limitExceedCard(
                            this@CameraLiveScanningActivity,
                            onCloseClick = {
                                finish()
                            },
                            onBuyPremium = {
                                startActivity(Intent(this@CameraLiveScanningActivity, PremiumProActivity::class.java))
                            },
                            afterAdSuccess = {
                                MyApplication.mInstance?.preferenceManager?.put(key,count + 1)
                                setPremiumCountText()
                                openCam.invoke()
                            },
                            onFailedAdSuccess = {
                               finish()
                            }
                            )
                    }

                }
            }else{
                openCam.invoke()
            }
        }
    }


}