package com.iobits.tech.app.ai_identifier.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.FragmentHistoryBinding
import com.iobits.tech.app.ai_identifier.manager.AdsManager
import com.iobits.tech.app.ai_identifier.manager.AnalyticsManager
import com.iobits.tech.app.ai_identifier.ui.activities.ScanningResultActivity
import com.iobits.tech.app.ai_identifier.ui.adapters.HistoryRvAdapter
import com.iobits.tech.app.ai_identifier.ui.viewModels.CollectionViewModel
import com.iobits.tech.app.ai_identifier.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private val binding by lazy {
        FragmentHistoryBinding.inflate(layoutInflater)
    }
    private val viewModel: CollectionViewModel by viewModels<CollectionViewModel>()
    private var showAdCounter = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        initViews()
//        showAd()
        return binding.root
    }

    private fun initViews() {

        viewModel.allItems.observe(requireActivity()) { allItem ->
            if (allItem.isEmpty()) {
                binding.historyRv.visibility = View.GONE
                binding.noResultView.visibility = View.VISIBLE
            } else {
                binding.historyRv.visibility = View.VISIBLE
                binding.noResultView.visibility = View.GONE

                binding.historyRv.apply {
                    this.adapter = HistoryRvAdapter(
                        requireContext(), allItem,
                        { imageUri, stringToSplit, detect ->
                            showEnter {
                                MyApplication.fromHistory = true
                                AnalyticsManager.logEvent("hstry_click")
                                val intent =
                                    Intent(requireContext(), ScanningResultActivity::class.java)
                                intent.putExtra("LABEL", stringToSplit)
                                intent.putExtra(Constants.IMAG_URI, imageUri)
                                intent.putExtra(Constants.DETECT, detect)
                                startActivity(intent)
                            }
                        }, {
                            viewModel.deleteItem(it)
                        }) { imgRl, title ->
                        shareImageFromUrl(imgRl, title)
                    }

                    this.layoutManager = GridLayoutManager(requireContext(), 2)
                }

//                // Define the bottom padding in pixels
//                val bottomPadding = 250
//                // Add scroll listener
//                binding.historyRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                        super.onScrolled(recyclerView, dx, dy)
//
//                        val layoutManager = recyclerView.layoutManager as GridLayoutManager
//                        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
//                        val totalItemCount = layoutManager.itemCount
//
//                        // Check if the last item is visible
//                        if (lastVisiblePosition == totalItemCount - 1) {
//                            // Add bottom padding
//                            recyclerView.setPadding(
//                                recyclerView.paddingLeft,
//                                recyclerView.paddingTop,
//                                recyclerView.paddingRight,
//                                bottomPadding
//                            )
//                        } else {
//                            // Remove bottom padding
//                            recyclerView.setPadding(
//                                recyclerView.paddingLeft,
//                                recyclerView.paddingTop,
//                                recyclerView.paddingRight,
//                                0
//                            )
//                        }
//                    }
//                })

            }
        }
    }

    private fun showEnter(onDone: () -> Unit) {
        Log.d("isFromPro", "showEnter: history $showAdCounter")
        showAdCounter += 1
        if (showAdCounter >= 2) {
            showAdCounter = 0
            MyApplication.mInstance?.adsManager?.loadInterstitialAd(
                activity = requireActivity(),
                onAdClick = {
                    onDone.invoke()
                })
        } else {
            onDone.invoke()
        }
    }

    private fun shareImageFromUrl(imageUrl: String, title: String) {
        try {

            MyApplication.blockOpendAd = true
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey, Download amazing app \"${getString(R.string.app_name)}\"." +
                        "\nDownload App from link below" +
                        "\n\nhttps://play.google.com/store/apps/details?id=${requireActivity().packageName}" +
                        "\n\n\n\n $title"
            )
            sendIntent.type = "image/*"
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUrl))
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(sendIntent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ShareImage", "Exception: ${e.message}")
        }

    }

    private fun getLocalBitmapUri(bmp: Bitmap): Uri? {
        var bmpUri: Uri? = null
        try {
            val cacheDir = File(requireContext().cacheDir, "images") // Use cache directory
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val file = File(
                cacheDir,
                "share_image_${System.currentTimeMillis()}.png"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()

            bmpUri = FileProvider.getUriForFile(
                requireContext(),
                "com.iobits.tech.app.ai_identifier.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bmpUri
    }

    private fun showAd() {
        MyApplication.getInstance().adsManager.loadNativeAd(
            requireActivity(),
            binding.adFrame,
            AdsManager.NativeAdType.MEDIA_SMALL_NEW,
            this.getString(R.string.ADMOB_NATIVE_WITHOUT_MEDIA_V2_HISTORY),
            binding.shimmerLayout
        )
    }

}