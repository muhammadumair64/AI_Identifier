package com.iobits.tech.app.ai_identifier.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.ActivityScanningBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.ui.fragments.bottomSheetFragment.NoResultFoundBottomSheetFragment
import com.iobits.tech.app.ai_identifier.ui.viewModels.ScanningViewModel
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.CustomOverlayView
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.getValue
import kotlin.math.max
import kotlin.math.min

class ScanningActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityScanningBinding.inflate(layoutInflater)
    }
    private val viewModelAI: ScanningViewModel by viewModels()

    private val TAG = "ScanningActivity"
    private var imageUri: String? = null
    private var job = Job()
    private var canStartScanningResult = true
    private var detect: String? = Constants.WHAT_IS_THIS
    private var loadingDialogue: AlertDialog? = null
    private var canShowNoResult = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        lifecycleScope.launch {
            delay(200)
            detect = intent.getStringExtra(Constants.DETECT)
            imageUri = intent.getStringExtra(Constants.IMAG_URI)
            AnalyticsManager.logEvent("scanning_screen")

            createLoadingDialogue()
            Handler(Looper.getMainLooper()).postDelayed({
                binding.loadingLottie.visibility = View.GONE
                binding.linearLayout9.visibility = View.VISIBLE
                binding.imgView.setImageURI(Uri.parse(imageUri))
                initMLKitBox(Uri.parse(imageUri))
                aiHandel()
            }, 3000)

            binding.backBtn.setSafeOnClickListener {
                onBackPressed()
            }

            binding.manualCrop.setSafeOnClickListener {
                AnalyticsManager.logEvent("manual_crop")
                imageUri?.let { assertFileStringUri -> initCropper(assertFileStringUri) }
            }

            binding.autoCrop.setSafeOnClickListener {
                AnalyticsManager.logEvent("auto_crop")
                initMLKit(Uri.parse(imageUri))
            }

            showAd()
            setTitleText()
        }
    }

    private fun setTitleText() {
        binding.titleScreen.text = when (detect) {
            Constants.WHAT_IS_THIS -> "Scanning"
            Constants.DOG -> "Scanning Dog"
            Constants.PLANT -> "Scanning Plant"
            Constants.CAT -> "Scanning Cat"
            Constants.INSECT -> "Scanning Insect"
            Constants.BIRD -> "Scanning Bird"
            Constants.OBJECT -> "Scanning Object"
            Constants.ROCK -> "Scanning Rock"
            Constants.MUSHROOM -> "Scanning Mushroom"
            Constants.CELEBRITY -> "Scanning Celebrity"
            Constants.ORIGIN -> "Scanning Country Flag"
            else -> {
                "Scanning"
            }
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

    override fun onBackPressed() {
        if (job.isActive) {
            job.cancel()
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        if (job.isActive) {
            job.cancel()
        }
        if (loadingDialogue?.isShowing == true) loadingDialogue?.dismiss()
        super.onDestroy()
    }

    //----------------------------------------------------------------- Manual Cropper ----------------------------------------------------------------//

    private fun initCropper(assertFileStringUri: String) {
        try {
            //Initializing uCropper
            val cropOutPUri = Uri.fromFile(createTempFile(packageName, ".jpg", cacheDir))
            val uCrop = UCrop.of(Uri.parse(assertFileStringUri), cropOutPUri)
            uCrop.useSourceImageAspectRatio()
            uCrop.withMaxResultSize(720, 720)
            uCrop.start(this)
        } catch (e: Exception) {
            Log.d(TAG, "ERROR ML : ${e.localizedMessage}")
        }
    }

    private fun handleCropResult(result: Intent) {
        val resultUri = UCrop.getOutput(result)
        if (resultUri != null) {
            moveNext(resultUri)
            Log.d(TAG, "moveNextCheck: 2")
        } else {
            Log.d(TAG, "handleCropResult: NOT FOUND")
        }
    }

    private fun handleCropError(result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) {
            Log.d(TAG, "handleCropError: ")
        } else {
            Log.d(TAG, "handleCropError: No")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: $requestCode")
        when (requestCode) {
            UCrop.REQUEST_CROP -> {
                if (data != null) handleCropResult(data)

            }

            UCrop.RESULT_ERROR -> {
                if (data != null) handleCropError(data)

            }
        }
    }

    //----------------------------------------------------------------- Auto Cropper ----------------------------------------------------------------//

    /**
     * Detect object and crop Image on tha boundingBox
     * Note: High chance to detect a larger or most prominent object
     */
    private fun initMLKit(resultUri: Uri) {
        // Multiple object detection in static images
        Log.d(TAG, "inside ML fun")
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()  // Optional
            .build()

        val objectDetector = ObjectDetection.getClient(options)

        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, resultUri)

            objectDetector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    try {
                        Log.d(TAG, "inside ML success $detectedObjects")
                        val imageBitmap =
                            MediaStore.Images.Media.getBitmap(contentResolver, resultUri)
                        if (imageBitmap != null) {
                            if (detectedObjects.isEmpty()) {
                                labelMLKitWithCropper(Uri.parse(imageUri))
                            }

                            for (detectedObject in detectedObjects) {
                                val boundingBox = detectedObject.boundingBox

                                // Calculate the zoom area based on the object's bounding box
                                val left = max(0, boundingBox.left)
                                val top = max(0, boundingBox.top)
                                val right = min(imageBitmap.width, boundingBox.right)
                                val bottom = min(imageBitmap.height, boundingBox.bottom)

                                // Crop the image for each detected object
                                val croppedBitmap = Bitmap.createBitmap(
                                    imageBitmap,
                                    left,
                                    top,
                                    right - left,
                                    bottom - top
                                )

                                labelMLKit(croppedBitmap)
                                if (loadingDialogue?.isShowing == false) loadingDialogue?.show()
                                break
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Error : ${e.localizedMessage}")
                    }
                }.addOnFailureListener { e ->
                    // Task failed with an exception
                    labelMLKitWithCropper(Uri.parse(imageUri))
                    Log.d(TAG, "Fail : ${e.localizedMessage}")
                }

        } catch (e: Exception) {
            Log.d(TAG, "ERROR ML : ${e.localizedMessage}")
        }
    }

    /**
     * Detect insect in the cropped image
     * by ML kit label detection
     */
    private fun
            labelMLKit(bitmap: Bitmap) {
        val image: InputImage
        try {
            image = InputImage.fromBitmap(bitmap, 0)
            // val options = ImageLabelerOptions.Builder()
            //     .setConfidenceThreshold(0.7f)
            //     .build()
            // val labeler = ImageLabeling.getClient(options)

            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    Log.d(TAG, "inside ML Label $labels")
                    // Task completed successfully
                    bitmapToUri(this, bitmap)?.let {
                        CoroutineScope(Dispatchers.IO + job).launch {
                            Log.d("TAG", "moveNextCheck: 1")
                            moveNext(it)
                        }
                    }

                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
//                    showDialogueTutorial(Uri.parse(imageUri))
                    CoroutineScope(Dispatchers.IO + job).launch {
                        Log.d(TAG, "place 1")
                        initCropper(Uri.parse(imageUri).toString())
                    }
                    Log.d(TAG, "ERROR ML : ${e.localizedMessage}")
                }

        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "ERROR ML : ${e.localizedMessage}")
        }
    }

    /**
     * Label detector in case of no Image cropped in Object detector
     */
    private fun labelMLKitWithCropper(resultUri: Uri) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, resultUri)
            val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)
            val centerX = image.width / 2
            val centerY = image.height / 2
            val cropSize = min(image.width, image.height) / 3 // Adjust as needed

            val left = max(0, centerX - cropSize)
            val top = max(0, centerY - cropSize)
            val right = min(imageBitmap.width, centerX + cropSize)
            val bottom = min(imageBitmap.height, centerY + cropSize)


            // Crop the image
            val croppedBitmap = Bitmap.createBitmap(
                imageBitmap,
                left,
                top,
                right - left,
                bottom - top
            )

            val uriImage = bitmapToUri(this, croppedBitmap)
            val imageForLabel = InputImage.fromFilePath(this, uriImage!!)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            labeler.process(imageForLabel)
                .addOnSuccessListener { labels ->
                    Log.d(TAG, "inside ML Label $labels")
                    // Task completed successfully

                    lifecycleScope.launch(Dispatchers.Main) {
                        CoroutineScope(Dispatchers.IO + job).launch {
                            Log.d(TAG, "place 2")
                            initCropper(uriImage.toString())
                        }
                    }
                }

                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...

                    //   showDialogueTutorial(uriImage)
                    CoroutineScope(Dispatchers.IO + job).launch {
                        Log.d(TAG, "place 3")
                        initCropper(uriImage.toString())
                    }
                    Log.d(TAG, "ERROR ML : ${e.localizedMessage}")
                }

        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "ERROR ML : ${e.localizedMessage}")
        }
    }

    private fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        val uri: Uri?
        try {
            // Specify the directory path where you want to save the image
            val filePath = File(context.getExternalFilesDir(null), "image.jpg")

            val outputStream = FileOutputStream(filePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            // Use the FileProvider to get the URI for the file
            uri = FileProvider.getUriForFile(
                context,
                "com.iobits.tech.app.ai_identifier.fileprovider",
                filePath
            )

        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return uri
    }

    //--------------------------------------------------------------------------ML kit box-----------------------------------------------------------//
    private fun initMLKitBox(resultUri: Uri) {
        // Multiple object detection in static images
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()  // Optional
            .build()

        val objectDetector = ObjectDetection.getClient(options)

        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, resultUri)

            objectDetector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)
                    if (imageBitmap != null && detectedObjects.isNotEmpty()) {
                        for (detectedObject in detectedObjects) {
                            val boundingBox = detectedObject.boundingBox

                            // Draw the square around the detected insect
                            displayDetectedInsectOverlay(boundingBox)

                            break // Only process the first detected object for simplicity
                        }
                    } else {
                        // Handle no detection case
                        Log.d(TAG, "No insect detected")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Fail : ${e.localizedMessage}")
                    labelMLKitWithCropper(Uri.parse(imageUri))
                }
        } catch (e: Exception) {
            Log.d(TAG, "ERROR ML : ${e.localizedMessage}")
        }
    }

    private fun displayDetectedInsectOverlay(boundingBox: Rect) {
        // Create an instance of the CustomOverlayView with the bounding box
        val overlayView = CustomOverlayView(this, boundingBox)
        binding.imageViewRL.addView(overlayView)
    }
    //-----------------------------------------------------------------------Move to next and Gemini----------------------------------------------------------//


    private fun moveNext(uri: Uri) {

        CoroutineScope(Dispatchers.IO + job).launch {
            imageUri = uri.toString()
            viewModelAI.hitGeminiApi(this@ScanningActivity, uri, detect ?: Constants.WHAT_IS_THIS)
            withContext(Dispatchers.Main) {
                if (loadingDialogue?.isShowing == false) loadingDialogue?.show()
            }
            Log.d(TAG, "IMAGE URL = $uri")
        }
    }

    private fun showDialogueNoResult() {
        if (loadingDialogue?.isShowing == true) loadingDialogue?.dismiss()
        if (canShowNoResult || !isFinishing) {
            canShowNoResult = false
            MyApplication.mInstance?.adsManager?.loadInterstitialAd(activity = this, onAdClick = {

                try {
                    val bottomSheetDialog =
                        NoResultFoundBottomSheetFragment().apply {
                            setOnDoneListener {
                                canShowNoResult = true
                                scanAgainClicked()
                            }
                        }
                    supportFragmentManager.let {
                        bottomSheetDialog.show(
                            it, bottomSheetDialog.tag
                        )
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Scan Again Please!", Toast.LENGTH_SHORT).show()
                    scanAgainClicked()
                }
            })
        }
    }

    private fun scanAgainClicked() {
        val intent = if (detect == Constants.WHAT_IS_THIS) Intent(this, CameraLiveScanningActivity::class.java)
        else Intent(this, CameraActivity::class.java)
        intent.putExtra(Constants.DETECT, detect)
        startActivity(intent)
        finish()
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

                        showDialogueNoResult()
                    }else if(uiState.contains("Error decoding image.", ignoreCase = true) ||
                        uiState.contains("Response canceled.", ignoreCase = true) ||
                        uiState.contains("Error generating response.", ignoreCase = true)||
                        uiState.contains("Response generation canceled.", ignoreCase = true)){
                        Log.d(TAG, "aiHandel: Error generating response.")
                        delay(3000)
                        showDialogueNoResult()
                    } else {
                        if (uiState.isNotEmpty()) {
                            startScanningResultActivity(uiState.toString())
                        }else showDialogueNoResult()
                    }

                }
            }
        }
    }

    private fun startScanningResultActivity(str: String) {
        if (str.contains("encrypted")) {
//            viewModel.callback?.onShowNoResultDialogue()
        } else {
            if (canStartScanningResult) {
                MyApplication.mInstance?.adsManager?.loadInterstitialAd(
                    activity = this,
                    onAdClick = {
                        setPremiumCount()
                        lifecycleScope.launch {
                            delay(100)
                            if (canStartScanningResult) {
                                canStartScanningResult = false
                                val intent = Intent(
                                    this@ScanningActivity,
                                    ScanningResultActivity::class.java
                                )
                                intent.putExtra("LABEL", str)
                                intent.putExtra(Constants.IMAG_URI, imageUri)
                                intent.putExtra(Constants.DETECT, detect)
                                startActivity(intent)
                                finish()
                            }
                        }

                    }
                )
            }
        }
    }

    private fun setPremiumCount(){
        when(detect){
            Constants.WHAT_IS_THIS -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.SCAN_ANYTHING_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.SCAN_ANYTHING_COUNT,5) - 1)

            Constants.DOG -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_DOG_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_DOG_COUNT,2) - 1)

            Constants.PLANT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_PLANT_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_PLANT_COUNT,2) - 1)

            Constants.CAT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_CAT_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_CAT_COUNT,2) - 1)

            Constants.INSECT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_INSECT_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_INSECT_COUNT,2) - 1)

            Constants.BIRD -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_BIRD_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_BIRD_COUNT,2) - 1)

            Constants.OBJECT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_OBJECT_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_OBJECT_COUNT,2) - 1)

            Constants.ROCK -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_ROCK_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_ROCK_COUNT,2) - 1)

            Constants.MUSHROOM -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_MUSHROOM_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_MUSHROOM_COUNT,2) - 1)

            Constants.CELEBRITY -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_CELEBRITY_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_CELEBRITY_COUNT,2) - 1)

            Constants.ORIGIN -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_COUNTRY_COUNT, MyApplication.getInstance().preferenceManager
                    .getInt(PreferenceManager.Key.ID_COUNTRY_COUNT,2) - 1)
        }
    }


    private fun createLoadingDialogue() {
        val alertCustomdialog: View =
            LayoutInflater.from(this).inflate(R.layout.dialogue_loading_scanning, null)
        //initialize alert builder.
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(alertCustomdialog);

        loadingDialogue = alert.create()
        loadingDialogue?.setCancelable(false)
        loadingDialogue?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }


}