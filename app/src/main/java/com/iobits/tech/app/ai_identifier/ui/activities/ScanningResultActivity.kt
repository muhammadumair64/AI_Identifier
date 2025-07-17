package com.iobits.tech.app.ai_identifier.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.ShimmerFrameLayout
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.database.dataClasses.Fact
import com.iobits.tech.app.ai_identifier.databinding.ActivityScanningResultBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.network.models.ImageData
import com.iobits.tech.app.ai_identifier.ui.adapters.KeyTraitsRvAdapter
import com.iobits.tech.app.ai_identifier.ui.adapters.MoreImagesAdapter
import com.iobits.tech.app.ai_identifier.ui.fragments.bottomSheetFragment.NoResultFoundBottomSheetFragment
import com.iobits.tech.app.ai_identifier.ui.viewModels.CollectionViewModel
import com.iobits.tech.app.ai_identifier.ui.viewModels.ImageViewModel
import com.iobits.tech.app.ai_identifier.utils.Constants
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.manager.RewardAdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@AndroidEntryPoint
class ScanningResultActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityScanningResultBinding.inflate(layoutInflater)
    }

    private var imgUri = ""
    private var label: String = ""
    private var detect: String? = Constants.WHAT_IS_THIS
    private val historyViewModel: CollectionViewModel by viewModels()
    private val imageViewModel: ImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        MyApplication.canShowRatting = true

        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            withContext(Dispatchers.Main){
                binding.progressBar.visibility = View.GONE
                binding.content.visibility = View.VISIBLE
                initViews()
                showAd()
            }
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

    private fun initViews() {
        imgUri = intent.getStringExtra(Constants.IMAG_URI).toString()
        detect = intent.getStringExtra(Constants.DETECT)
        AnalyticsManager.logEvent("scan_result")
        if (MyApplication.mInstance?.preferenceManager?.getBoolean(
                PreferenceManager.Key.IS_APP_PREMIUM,
                false
            ) == false
        ) {
            if (!MyApplication.fromHistory) {
//            setPremiumCount()
            }
        }
        Log.d("PICTURE_URL", "initViews result sc: $imgUri")
        try {
            binding.imageView.setImageURI(Uri.parse(imgUri))
        } catch (e: Exception) {
            e.localizedMessage
        }

        intent.extras?.let {
            label = it.getString("LABEL", "")
            if (label == "") {
                showDialogueNoResult()
            } else {
                splitStringOnNumbers(label)
            }
        }

        binding.dateTime.text = getFormattedDateTime()

        binding.backIcon.setSafeOnClickListener {
            onBackPressed()
        }

        binding.scanAgain.setSafeOnClickListener {
            detect?.let { it1 -> startScanning(it1) }
        }
    }

    private fun getFormattedDateTime(): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    //---------------------------------------------------------------Split String Set Data-------------------------------------------------------------//

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

        setData(descriptions)
        // Log all extracted numbers and descriptions
        for (i in numbers.indices) {
            Log.d("PRINT_SPLIT_VALUE", "${numbers[i]}: ${descriptions.getOrNull(i)}")
        }
    }

    private fun setData(descriptions: MutableList<String>) {
        val name = descriptions.getOrNull(0)?.trim()
        name?.trim()?.let { hitApiGetPhoto(it) }
        binding.titleName.text = name?.trim()?.let { replaceStarWithSpace(it) }
        val detail = descriptions.getOrNull(1)?.trim()
        binding.detailTv.text = detail?.trim()?.let { replaceStarWithSpace(it) }

        if (MyApplication.fromHistory) {
            MyApplication.fromHistory = false
            binding.imageView.setSafeOnClickListener {
                startImageDetailsActivity(imgUri)
            }
        } else {
            MyApplication.scanSuccess = true
            val savedUri = uriToBitmap(this, imgUri.toUri())?.let { bitmapToUri(this, it) }
            binding.imageView.setSafeOnClickListener {
                startImageDetailsActivity(savedUri.toString())
            }
            name?.trim()?.let {
                detail?.trim()
                    ?.let { it1 ->
                        detect
                            ?.let { it2 ->
                                historyViewModel.insertItem(
                                    it,
                                    it1,
                                    savedUri.toString(),
                                    label,
                                    it2
                                )
                            }
                    }
            }
        }
        binding.saveBtn.setSafeOnClickListener {
            MyApplication.moveToHistory = true
            finish()
        }
        val keyTraits = descriptions.getOrNull(2)?.trim()
        val processedFacts: ArrayList<Fact>? = keyTraits?.let { processFacts(it) }
        binding.keyTraitsRv.also {
            it.adapter = processedFacts?.let { it1 -> KeyTraitsRvAdapter(it1) }
            it.layoutManager = LinearLayoutManager(this)
        }
        processedFacts?.forEach { fact ->
            Log.d("PRINT_SPLIT_VALUE", "setData: ${fact.title} == ${fact.description}")
        }
        val funFacts = descriptions.getOrNull(3)?.trim()
        binding.funFact.text = funFacts?.trim()
            ?.substringBeforeLast(", msgType=RECEIVER)], isLoading=false, errorMessage=null)")
            ?.let { replaceStarWithSpace(it) }

    }

    private fun splitTitleAndDescription(fact: String): Fact {
        val parts = fact.removePrefix("*").trim().split(": ", limit = 2)
        return if (parts.size == 2) {
            Fact(parts[0].trim(), parts[1].trim())
        } else {
            Fact(fact.trim(), "") // Handle cases where there's no colon
        }
    }

    private fun processFacts(factsString: String): ArrayList<Fact> {
        val factList = ArrayList<Fact>()

        // Split string into list using newline or any delimiter
        val facts = factsString.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        facts.forEach { factList.add(splitTitleAndDescription(it)) }
        return factList
    }

    private fun getRegexForNumberType(input: String): Regex? {
        return when {
            """-\d+:""".toRegex()
                .containsMatchIn(input) -> """-(\d+):""".toRegex() // Matches -<number>:
            """\d+:""".toRegex()
                .containsMatchIn(input) -> """(\d+):""".toRegex()  // Matches <number>:
            else -> null
        }
    }

    private fun replaceStarWithSpace(input: String): String {
        return input.replace('*', ' ')
    }

    //--------------------------------------------------------------More Images API PIXBAY---------------------------------------------------//

    private fun hitApiGetPhoto(queryName: String) {
        imageViewModel.images.observe(this) { images ->
            images.forEach { images ->
                Log.d(
                    "SHOW_IMAGE_URL",
                    "hitApiGetPhoto FOR $queryName: ${images.largeImageURL} \n ${images.tags}"
                )
            }
            Log.d("SHOW_IMAGE_URL", " hitApiGetPhoto ${images.isEmpty()}")
            if (images.isNotEmpty()) {
                val filterImageList = if (detect != Constants.WHAT_IS_THIS) detect?.let {
                    filterImagesByKeywords(
                        images,
                        it
                    )
                }
                else images
                Log.d("SHOW_IMAGE_URL", "hitApiGetPhoto: LIST_SIZE $filterImageList")
                if (filterImageList?.isNotEmpty() == true) {
                    binding.moreImgLay.visibility = View.VISIBLE
                    binding.moreImagesLoading.visibility = View.GONE
                    binding.img1Card.visibility = View.VISIBLE
                    binding.img2Card.visibility = View.VISIBLE
                    binding.moreImagesRv.visibility = View.VISIBLE
                    pickTwoRandomItems(filterImageList)
                    binding.moreImagesRv.also { rv ->
                        rv.adapter = MoreImagesAdapter(this, filterImageList) {
                            startImageDetailsActivity(it)
                        }
                        rv.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    }
                } else {
                    binding.moreImgLay.visibility = View.GONE
                    binding.img1Card.visibility = View.GONE
                    binding.img2Card.visibility = View.GONE
                }

            } else {
                binding.moreImgLay.visibility = View.GONE
                binding.img1Card.visibility = View.GONE
                binding.img2Card.visibility = View.GONE
            }

        }

        imageViewModel.isLoading.observe(this) { isLoading ->
            // Show/hide progress indicator
            if (isLoading) binding.moreImagesLoading.visibility = View.VISIBLE
            else binding.moreImagesLoading.visibility = View.GONE
            Log.d("SHOW_IMAGE_URL", "isLoading FOR $queryName: $isLoading")
        }

        imageViewModel.error.observe(this) { errorMessage ->
            Log.d("SHOW_IMAGE_URL", "error FOR $queryName: $errorMessage")
            binding.moreImagesLoading.visibility = View.GONE
            if (errorMessage != null) {
                binding.moreImgLay.visibility = View.GONE
                binding.img1Card.visibility = View.GONE
                binding.img2Card.visibility = View.GONE
            }
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        imageViewModel.fetchImages(queryName)
    }

    private fun pickTwoRandomItems(list: List<ImageData>) {
        when {
            list.isEmpty() -> {
                binding.img1Card.visibility = View.GONE
                binding.img2Card.visibility = View.GONE
            }          // Return empty list if no items exist
            list.size == 1 -> {
                listOf(list.first())
                setImage(list.first().largeImageURL, binding.img1, binding.shimmerLayout1)
                binding.img2Card.visibility = View.GONE
                binding.img1.setSafeOnClickListener {
                    startImageDetailsActivity(list.first().largeImageURL)
                }
            }   // Return the single item if only one exists
            else -> {
                val index1 = Random.nextInt(list.size)
                // Pick a second random index, ensuring it's different from index1
                var index2 = Random.nextInt(list.size)
                while (index2 == index1) {
                    index2 = Random.nextInt(list.size)
                }
                setImage(list[index1].largeImageURL, binding.img1, binding.shimmerLayout1)
                setImage(list[index2].largeImageURL, binding.img2, binding.shimmerLayout2)
                binding.img1.setSafeOnClickListener {
                    startImageDetailsActivity(list[index1].largeImageURL)
                }
                binding.img2.setSafeOnClickListener {
                    startImageDetailsActivity(list[index2].largeImageURL)
                }
            }
        }
    }

    private fun setImage(imgUrl: String, imageView: ImageView, shimmerLayout: ShimmerFrameLayout) {
        // Load the image with Glide
        Glide.with(this)
            .load(imgUrl)
            .apply(RequestOptions().placeholder(R.drawable.no_item_bg))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    // Stop shimmer on failure
                    shimmerLayout.stopShimmer()
                    shimmerLayout.hideShimmer()
                    return false // Pass the error to Glide
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    // Stop shimmer when the image is loaded
                    shimmerLayout.stopShimmer()
                    shimmerLayout.hideShimmer()
                    return false // Pass the resource to the ImageView
                }
            })
            .into(imageView)
    }

    private fun filterImagesByKeywords(
        imageList: List<ImageData>, // Your list of images
        keywords: String
    ): List<ImageData> {
        return imageList.filter { image ->
            image.tags.contains(keywords, ignoreCase = true)
        }
    }


    private fun showDialogueNoResult() {
        val bottomSheetDialog =
            NoResultFoundBottomSheetFragment().apply {
                setOnDoneListener {
                    finish()
                }
            }
        supportFragmentManager.let {
            bottomSheetDialog.show(
                it, bottomSheetDialog.tag
            )
        }
    }

    private fun startImageDetailsActivity(imageUrl: String?) {
        imageUrl?.let {
            val intent = Intent(this, PictureViewActivity::class.java)
            intent.putExtra(Constants.IMAGE_URL_EXTRA, imageUrl)
            startActivity(intent)
        }
    }

    private fun historySavedDialogue() {
        val alertCustomDialog: View =
            LayoutInflater.from(this).inflate(R.layout.dialogue_history_saved, null)
        //initialize alert builder.
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setView(alertCustomDialog);

        val loadingDialogue = alert.create()
        loadingDialogue.setCancelable(false)
        loadingDialogue.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialogue.show()
        alertCustomDialog.findViewById<TextView>(R.id.cancel).setSafeOnClickListener {
            loadingDialogue.dismiss()
        }
        alertCustomDialog.findViewById<TextView>(R.id.view_history).setSafeOnClickListener {
            MyApplication.moveToHistory = true
            loadingDialogue.dismiss()
            finish()
        }
    }

    private fun setPremiumCount() {
        when (detect) {
            Constants.WHAT_IS_THIS -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.SCAN_ANYTHING_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.SCAN_ANYTHING_COUNT, 5) - 1
                )

            Constants.DOG -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_DOG_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_DOG_COUNT, 1) - 1
                )

            Constants.PLANT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_PLANT_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_PLANT_COUNT, 1) - 1
                )

            Constants.CAT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_CAT_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_CAT_COUNT, 1) - 1
                )

            Constants.INSECT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_INSECT_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_INSECT_COUNT, 1) - 1
                )

            Constants.BIRD -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_BIRD_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_BIRD_COUNT, 1) - 1
                )

            Constants.OBJECT -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_OBJECT_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_OBJECT_COUNT, 1) - 1
                )

            Constants.ROCK -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_ROCK_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_ROCK_COUNT, 1) - 1
                )

            Constants.MUSHROOM -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_MUSHROOM_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_MUSHROOM_COUNT, 1) - 1
                )

            Constants.CELEBRITY -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_CELEBRITY_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_CELEBRITY_COUNT, 1) - 1
                )

            Constants.ORIGIN -> MyApplication.getInstance().preferenceManager
                .put(
                    PreferenceManager.Key.ID_COUNTRY_COUNT,
                    MyApplication.getInstance().preferenceManager
                        .getInt(PreferenceManager.Key.ID_COUNTRY_COUNT, 1) - 1
                )
        }
    }

    fun uriToBitmap(context: Context, imageUri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream).also {
                inputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        val uri: Uri?
        try {
            // Specify the directory path where you want to save the image
            val fileName = "history_${System.currentTimeMillis()}.jpg"
            val filePath = File(context.getExternalFilesDir(null), fileName)

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

    fun copyImageToAppHistory(context: Context, sourceUri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return null

            val historyDir = File(context.filesDir, "history_images")
            if (!historyDir.exists()) historyDir.mkdirs()

            val fileName = "history_${System.currentTimeMillis()}.jpg"
            val file = File(historyDir, fileName)

            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun startScanning(detectionOption: String) {
        when (detectionOption) {
            Constants.WHAT_IS_THIS -> {
                openScanning(PreferenceManager.Key.SCAN_ANYTHING_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.DOG -> {
                openScanning(PreferenceManager.Key.ID_DOG_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.PLANT -> {
                openScanning(PreferenceManager.Key.ID_PLANT_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.CAT -> {
                openScanning(PreferenceManager.Key.ID_CAT_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.INSECT -> {
                openScanning(PreferenceManager.Key.ID_INSECT_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.BIRD -> {
                openScanning(PreferenceManager.Key.ID_BIRD_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.OBJECT -> {
                openScanning(PreferenceManager.Key.ID_OBJECT_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.ROCK -> {
                openScanning(PreferenceManager.Key.ID_ROCK_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.MUSHROOM -> {
                openScanning(PreferenceManager.Key.ID_MUSHROOM_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.ORIGIN -> {
                openScanning(PreferenceManager.Key.ID_COUNTRY_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }

            Constants.CELEBRITY -> {
                openScanning(PreferenceManager.Key.ID_CELEBRITY_COUNT) {
                    scanAgainScreen(detectionOption)
                }
            }
        }
    }

    private fun scanAgainScreen(detectionOption: String) {
        val intent = if (detectionOption == Constants.WHAT_IS_THIS) Intent(this, CameraLiveScanningActivity::class.java)
        else Intent(this, CameraActivity::class.java)
        intent.putExtra(Constants.DETECT, detectionOption)
        startActivity(intent)
        finish()
    }

    private fun openScanning(key: PreferenceManager.Key, openCam: () -> Unit) {
        val count = MyApplication.mInstance?.preferenceManager?.getInt(key, 1)
        if (MyApplication.getInstance().preferenceManager.getBoolean(
                PreferenceManager.Key.IS_APP_PREMIUM,
                false
            )
        ) {
            openCam.invoke()
        } else {
            if (count != null) {
                if (count > 0) {
                    openCam.invoke()
                } else {

                    RewardAdManager.limitExceedCard(
                        this, onCloseClick = {finish()}, onBuyPremium = {
                        startActivity(Intent(this, PremiumProActivity::class.java))
                    }, afterAdSuccess = {
                            MyApplication.mInstance?.preferenceManager?.put(key,count+1)
                            openCam.invoke()
                        },
                        onFailedAdSuccess = {
                        }
                    )

                }
            } else {
                openCam.invoke()
            }
        }
    }


}