package com.iobits.tech.app.ai_identifier.ui.activities

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceFragment
import android.util.Log
import android.view.PixelCopy
import android.view.ScaleGestureDetector
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.ads.AdSize
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityCameraBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.utils.BitmapHelper
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil
import com.iobits.tech.app.ai_identifier.utils.PermissionsUtil.showCustomDialog
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListenerCam = (luma: Double) -> Unit

class CameraActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityCameraBinding.inflate(layoutInflater)
    }

    lateinit var mediaPlayer: MediaPlayer
    private var imageCapture: ImageCapture? = null
    private var destinationFolder: String = "DogDetector"
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashOnOff: Int? = 2
    private var isCameraRunning = false
    private lateinit var camera: Camera
    private var currentZoomRatio = 1.0f
    private var isFlashOn = false
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var scanCounts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AnalyticsManager.logEvent("In Camera")

//        if(MyApplication.mInstance?.preferenceManager?.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM, false) == true)
            binding.scanCount.visibility = View.GONE
//        scanCounts = MyApplication.getInstance().preferenceManager.getInt(PreferenceManager.Key.SCAN_COUNT, 10)
//        binding.scanCount.text = "$scanCounts scan left"

        flashOnOff = MyApplication.mInstance?.preferenceManager?.getInt(
            PreferenceManager.Key.CAMERA_FLASH,
            2
        )
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.camera_beep)
        initListeners()
        showAd()
    }

    private fun showAd() {
        MyApplication.mInstance?.adsManager?.loadNativeAd(
            this,
            binding.adFrame,
            AdsManager.NativeAdType.NOMEDIA_MEDIUM,
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

        binding.capture.setSafeOnClickListener {
//            if (MyApplication.getInstance().preferenceManager.getBoolean(PreferenceManager.Key.IS_APP_PREMIUM, false)){
                startCamFun()
//            }else{
//                if (scanCounts > 0) {
//                    startCamFun()
//                } else startActivity(Intent(this, PremiumProActivity::class.java))
//            }
        }

        binding.changeCamBtn.setSafeOnClickListener{
            changeCamera()
        }

        binding.galleryBtn.setSafeOnClickListener {
            startCam{
                openGalleryActivity()
            }
        }

    }

    private fun startCamFun(){
        binding.flashLight.visibility = View.GONE
        binding.backBtn.visibility = View.GONE
        binding.bottomOptions.visibility = View.GONE
        binding.lottieAnimationView2.visibility = View.GONE
        clickPictureClick()
    }


    private fun toggleFlashlight(flashBtn: ImageView) {
        try {
            isFlashOn = !isFlashOn
            cameraId?.let { cameraManager?.setTorchMode(it, isFlashOn) }
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
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
        }
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        Log.d("bottomNavigation", "onCreate: in take photo inside ")
        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d("FLASH_MODE_ON", "savedUri: $savedUri")
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.viewFinder.surfaceProvider
                }

            // ImageCapture use case with flash mode from preferences
            val imageCapture = ImageCapture.Builder()
                .setFlashMode(
                    PreferenceManager.getInstance(this)
                        .getInt(PreferenceManager.Key.CAMERA_FLASH, ImageCapture.FLASH_MODE_OFF)
                )
                .build()

            // ImageAnalysis use case
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer {})
                }

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to lifecycle
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
                currentZoomRatio = 1.0f
                camera.cameraControl.setZoomRatio(currentZoomRatio)

                // Set up pinch-to-zoom gesture
                val zoomGesture = ScaleGestureDetector(
                    this,
                    object : ScaleGestureDetector.OnScaleGestureListener {
                        override fun onScale(detector: ScaleGestureDetector): Boolean {
                            val scaleFactor = detector.scaleFactor
                            currentZoomRatio = (currentZoomRatio * scaleFactor).coerceIn(
                                1.0f,
                                camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                            )
                            camera.cameraControl.setZoomRatio(currentZoomRatio)
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
                    camera.cameraControl.enableTorch(isFlashOn)
                    updateFlashlightIcon(binding.flashLight)
                }

                isCameraRunning = true
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
//                finish()
                }

            }
        }
    }

    private fun clickPictureClick() {
        val timer = MyApplication.getInstance().preferenceManager.getString(
            PreferenceManager.Key.CAMERA_TIMER,
            "timerTvOff"
        )
        Log.d(TAG, "clickPictureClick: $timer")
        if (timer.equals("timerTvOff")) {
            clickPicture(1500L)
        } else if (timer.equals("timerTv3Sec")) {
            clickPicture(4500L)
        } else if (timer.equals("timerTv5Sec")) {
            clickPicture(6500L)
        }
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
        binding.bottomOptions.visibility = View.VISIBLE
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

    private class LuminosityAnalyzer(private val listener: LumaListenerCam) : ImageAnalysis.Analyzer {

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

    fun changeCamera() {
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
        //if flash is on than take picture through imagecapture otherwise just get bitmap
        Log.d(
            "CAMERA_FRAGMENT",
            "clickAndSavePicture: " + PreferenceManager.getInstance(this).getInt(
                PreferenceManager.Key.CAMERA_FLASH,
                2
            )
        )
        if (PreferenceManager.getInstance(this)
                .getInt(PreferenceManager.Key.CAMERA_FLASH, 2) == 1
        ) {
            takePhoto()
            object : CountDownTimer(800, 800) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    val bitmap = binding.viewFinder.bitmap
                    binding.cameraClick.setImageBitmap(bitmap)
                    binding.cameraClick.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.GONE

                    Log.d("cameraSoundOn", "onFinish: " + sound.equals("cameraSoundOn"))
                    if (sound.equals("cameraSoundOn")) {
                        if (::mediaPlayer.isInitialized) {
                            if (!mediaPlayer.isPlaying) mediaPlayer.start()
                        }
                    }
                }
            }.start()
        } else {
            val bitmap = binding.viewFinder.bitmap
            binding.cameraClick.setImageBitmap(bitmap)
            binding.cameraClick.visibility = View.VISIBLE
            binding.viewFinder.visibility = View.GONE
            Log.d("cameraSoundOn", "onFinish: " + sound.equals("cameraSoundOn"))
            if (sound.equals("cameraSoundOn")) {
                if (::mediaPlayer.isInitialized) {
                    if (!mediaPlayer.isPlaying) mediaPlayer.start()
                }

            }
        }

        //save click picture
        CoroutineScope(Dispatchers.Main).launch {
            delay(900)
            binding.progressBar.visibility = View.GONE
            delay(300)
            binding.cameraWrapper.let { it ->
                try {
                    captureView(it, window) {
                        BitmapHelper.getInstance().bitmap = it
                        // your new bitmap with overlay is here and you can save it to file just like any other bitmaps.
                        binding.cameraClick.visibility = View.GONE
                        binding.viewFinder.visibility = View.VISIBLE

                        var uri: Uri? = null
                        try {
                            val file = createImageFile()
                            val stream = FileOutputStream(file)
                            it.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            stream.flush()
                            stream.close()
                            // see docs https://developer.android.com/reference/android/net/Uri#fromFile(java.io.File)
                            uri = Uri.fromFile(file)

                        } catch (e: Exception) {
                            Log.d(TAG, "EXCEPTION_BITMAP_TO_FILE: " + e.localizedMessage)
                        }
                        Log.d("CAMERA_IMAGE", " Camera image uri::  $uri  ## $it")

                        if (uri != null) {
                            val detect = intent.getStringExtra(Constants.DETECT)
                            val intent = Intent(this@CameraActivity, ScanningActivity::class.java)
                            intent.putExtra(Constants.IMAG_URI, uri.toString())
                            intent.putExtra(Constants.DETECT, detect)
                            Log.d("CAMERA_IMAGE", " Camera image uri::  $uri")
                            startActivity(intent)
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "clickandSavePicture: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("onActivityResult", "onActivityResult main acitvity : $requestCode")
//        if (requestCode == Constants.RESULT_MAIN_POST) {
//            (context as MainActivity).openExploreFragment()
    }

    private fun captureView(view: View, window: Window, bitmapCallback: (Bitmap) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Above Android O, use PixelCopy
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val location = IntArray(2)
            view.getLocationInWindow(location)
            PixelCopy.request(
                window,
                Rect(
                    location[0],
                    location[1],
                    location[0] + view.width,
                    location[1] + view.height
                ),
                bitmap,
                {
                    if (it == PixelCopy.SUCCESS) {
                        bitmapCallback.invoke(bitmap)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } else {
            val tBitmap = Bitmap.createBitmap(
                view.width, view.height, Bitmap.Config.RGB_565
            )
            val canvas = Canvas(tBitmap)
            view.draw(canvas)
            canvas.setBitmap(null)
            bitmapCallback.invoke(tBitmap)
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
        val detect = intent.getStringExtra(Constants.DETECT)
        val intent = Intent(this@CameraActivity, GalleryActivity::class.java)
        intent.putExtra(Constants.DETECT, detect)
        startActivity(intent)
        finish()
    }

}