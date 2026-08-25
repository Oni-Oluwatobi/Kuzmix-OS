package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object SubjectSegmentationHelper {
    private const val TAG = "SubjectSegHelper"

    /**
     * Loads the wallpaper bitmap and isolates the foreground subject.
     * Returns null if segmentation fails or is unsupported.
     */
    suspend fun segmentWallpaper(context: Context, uriString: String?, defaultKey: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadWallpaperBitmap(context, uriString, defaultKey) ?: return@withContext null
            
            // Configure ML Kit Subject Segmenter
            val options = SubjectSegmenterOptions.Builder()
                .enableForegroundBitmap()
                .build()
            
            val segmenter = SubjectSegmentation.getClient(options)
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            // Run segmentation synchronously in our coroutine context
            val resultTask = segmenter.process(inputImage)
            val result = Tasks.await(resultTask)
            
            val foreground = result.foregroundBitmap
            if (foreground != null) {
                Log.d(TAG, "Subject segmentation successful! Isolated foreground bitmap obtained.")
                return@withContext foreground
            } else {
                Log.w(TAG, "Segmentation returned null foreground bitmap.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Segmentation failed or fell back gracefully: ${e.message}", e)
        }
        return@withContext null
    }

    fun loadWallpaperBitmap(context: Context, uriString: String?, defaultKey: String): Bitmap? {
        try {
            if (!uriString.isNullOrEmpty()) {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    return BitmapFactory.decodeStream(inputStream)
                }
            }
            
            // Fallback to default wallpaper key in resource drawable
            if (defaultKey == "sunset") {
                return BitmapFactory.decodeResource(context.resources, R.drawable.wallpaper)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading wallpaper bitmap: ${e.message}")
        }
        return null
    }
}
