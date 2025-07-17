package com.iobits.tech.app.ai_identifier.network.models

data class ImageResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<ImageData>
)

data class ImageData(
    val id: Int,
    val pageURL: String,
    val tags: String,
    val previewURL: String,
    val webformatURL: String,
    val largeImageURL: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val imageSize: Int,
    val views: Int,
    val downloads: Int,
    val likes: Int,
    val comments: Int,
    val user: String,
    val userImageURL: String
)