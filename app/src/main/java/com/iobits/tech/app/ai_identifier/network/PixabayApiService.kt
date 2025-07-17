package com.iobits.tech.app.ai_identifier.network

import com.iobits.tech.app.ai_identifier.network.models.ImageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApiService {
    @GET("api/")
    suspend fun getImages(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("image_type") imageType: String = "photo",
        @Query("category") category: String = "Plant"
    ): ImageResponse
}


