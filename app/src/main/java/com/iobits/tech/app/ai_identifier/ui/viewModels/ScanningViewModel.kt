package com.iobits.tech.app.ai_identifier.ui.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.iobits.tech.app.ai_identifier.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ScanningViewModel @Inject constructor() : ViewModel() {

    private var responseJob: Job? = null

    // One-time response delivery to Activity
    private val _responseFlow = MutableSharedFlow<String>()
    val responseFlow = _responseFlow.asSharedFlow()
    val TAG = "CameraXBasic"

    // Public method to hit from Activity
    fun hitGeminiApi(context: Context, imgUri: Uri, detect: String) {
        try {
            val bitmap = BitmapFactory.decodeStream(context.contentResolver?.openInputStream(imgUri))
            startResponseTaskForBitmap(bitmap, detect)
        } catch (e: Exception) {
            Log.d(TAG, "hitGeminiApi: Exception for URI $imgUri: ${e.localizedMessage}")
            viewModelScope.launch {
                Log.d(TAG, "hitGeminiApi: Error decoding image.")
                _responseFlow.emit("Error decoding image.")
            }
        }
    }

    private fun startResponseTaskForBitmap(bitmap: Bitmap, detect: String) {
        val prompt = queryPrefix(detect)
        responseJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Calling Gemini API")
                val resizedImage = resizeBitmap(bitmap, 1024, 1024)
                val response = getResponseForImageAndPrompt(resizedImage, prompt)
                Log.d(TAG, "Response: $response")
                _responseFlow.emit(response)
            } catch (e: CancellationException) {
                _responseFlow.emit("Response canceled.")
                Log.d(TAG, "startResponseTaskForBitmap: Response canceled.")
            } catch (e: Exception) {
                _responseFlow.emit("Error generating response.")
                Log.d(TAG, "startResponseTaskForBitmap: Error generating response.")
            }
        }
    }

    private fun resizeBitmap(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val aspectRatio = image.width.toFloat() / image.height
        val width = if (image.width > maxWidth) maxWidth else image.width
        val height = (width / aspectRatio).toInt().coerceAtMost(maxHeight)
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private suspend fun getResponseForImageAndPrompt(image: Bitmap, prompt: String): String {
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = "AIzaSyDvy0gERhUj83LhC6EQZun3UCTno3fCjjY",
        )
        return withContext(Dispatchers.IO) {
            try {
                val inputContent = content {
                    image(image)
                    text(prompt)
                }
                val response = generativeModel.generateContent(inputContent)
                response.text ?: throw Exception("Response is null or empty")
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun queryPrefix(detect: String): String {
        return if (detect == Constants.CELEBRITY) {
            "Identify Celebrity if no data found about this celebrity return result unknown otherwise provide (don't place extra spaces and extra line breaks also don't use unknown in response):" +
                    "-1: Name (Only name )" +
                    "-2: Some detail about this particulate celebrity" +
                    "-3: Key Traits in bullet points (it must include interesting details with proper title and description bullet must be *)" +
                    "-4: Fun Fact (give some interesting fun fact detail not less then 3 lines)"
        } else {
            "Check $detect is in the image. If not a $detect in the image, then provide the result as Invalid Image(just give Invalid Image don't give any other string) and ignore other don't provide any other information and if it is a $detect then provide data don't give unknown in anywhere(don't place extra spaces and extra line breaks also just give data about one $detect and ignore rest of things):" +
                    " -1: Name (Only name don't give unknown)" +
                    " -2: Detail about this (in 10 lines  don't give unknown)" +
                    " -3: Key Traits in bullet points (it must include physical characteristics, diet if applicable for this and interesting details with proper title and description bullet must be * and don't give title of Key Traits  don't give unknown)" +
                    " -4: Fun Fact (give some interesting fun fact detail not less then 3 lines don't give me title of Fun Fact  don't give unknown)"
        }
    }

    fun cancelResponse() {
        responseJob?.cancel()
        responseJob = null
        viewModelScope.launch {
            _responseFlow.emit("Response generation canceled.")
        }
    }
}
