package com.iobits.tech.app.ai_identifier.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.net.Uri

class ImageFetcher(private val context: Context) {

    fun getAllImages(): List<Uri> {
        val images = mutableListOf<Uri>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                images.add(contentUri)
            }
        }

        return images
    }
}
