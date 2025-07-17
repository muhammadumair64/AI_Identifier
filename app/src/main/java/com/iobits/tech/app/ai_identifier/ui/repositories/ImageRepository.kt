package com.iobits.tech.app.ai_identifier.ui.repositories

import com.iobits.tech.app.ai_identifier.network.PixabayApiService
import com.iobits.tech.app.ai_identifier.network.models.ImageResponse
import javax.inject.Inject
import javax.inject.Named

class ImageRepository @Inject constructor(@Named("pixabay") private val apiService: PixabayApiService) {

    suspend fun fetchImages(query: String): ImageResponse {
        return apiService.getImages(
            "48060551-30c9d1c2bbca49f38303c6818",
            query,
            "photo",
            "plants"
        )
    }
}