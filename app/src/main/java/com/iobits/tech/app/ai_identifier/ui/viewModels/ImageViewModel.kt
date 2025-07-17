package com.iobits.tech.app.ai_identifier.ui.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iobits.tech.app.ai_identifier.network.models.ImageData
import com.iobits.tech.app.ai_identifier.ui.repositories.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(private val repository: ImageRepository) : ViewModel() {

    private val _images = MutableLiveData<List<ImageData>>()
    val images: LiveData<List<ImageData>> = _images

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchImages(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.fetchImages(query)
                Log.d("SHOW_IMAGE_URL", "fetchImages: $response")
                _images.value = response.hits
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

}