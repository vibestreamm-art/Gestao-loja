package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {
    private const val MAX_DIM = 900

    suspend fun savePickedPhotoToInternalStorage(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val resolver = context.contentResolver
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(sourceUri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, boundsOpts)
                }

                var sampleSize = 1
                while (boundsOpts.outWidth / sampleSize > MAX_DIM || boundsOpts.outHeight / sampleSize > MAX_DIM) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val bitmap = resolver.openInputStream(sourceUri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, decodeOpts)
                } ?: return@runCatching null

                val dir = File(context.filesDir, "product_photos").apply { mkdirs() }
                val destFile = File(dir, "prod_${System.currentTimeMillis()}.jpg")
                FileOutputStream(destFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
                }
                bitmap.recycle()
                Uri.fromFile(destFile).toString()
            }.getOrNull()
        }
    }
}
