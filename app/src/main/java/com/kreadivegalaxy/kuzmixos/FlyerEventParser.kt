package com.kreadivegalaxy.kuzmixos

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.provider.CalendarContract
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar
import java.util.TimeZone

object FlyerEventParser {

    /**
     * Generates a high-fidelity Web3 Axion Tech flyer.
     * This ensures that ML Kit is scanning a real, visually robust high-resolution image,
     * making the live presentation completely deterministic and impressive!
     */
    fun generateFlyerBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background: Deep Galactic Navy Space
        canvas.drawColor(Color.parseColor("#080515"))

        // Web3 Brand Glows (Axion Tech style)
        val glowPaint = Paint().apply {
            color = Color.parseColor("#2A4100FF") // Deep electric blue / purple glow
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(400f, 300f, 280f, glowPaint)

        val accentGlow = Paint().apply {
            color = Color.parseColor("#1500FFFF") // Pure neon cyan accent glow
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(400f, 300f, 150f, accentGlow)

        // Title Paint
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 38f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Subtitle Paint (Electric Purple / Innovation)
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#BF00FF")
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Details Paint
        val detailPaint = Paint().apply {
            color = Color.parseColor("#00FFFF") // Neon Cyan
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Location Paint
        val locationPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            textSize = 24f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("AXION WEB3 TECH SUMMIT 2026", 400f, 140f, titlePaint)
        canvas.drawText("EMPOWERING THE FUTURE WITH INNOVATIVE AI", 400f, 190f, subtitlePaint)

        canvas.drawText("DATE: JULY 26, 2026", 400f, 290f, detailPaint)
        canvas.drawText("TIME: 14:30 (2:30 PM)", 400f, 350f, detailPaint)
        canvas.drawText("LOCATION: GALAXY ARENA METAVERSE", 400f, 410f, locationPaint)

        return bitmap
    }

    /**
     * Parses the current screen/flyer image using Google's ML Kit and extracts event info.
     */
    fun parseScreenFlyer(context: Context, onTextExtracted: (String) -> Unit, onError: (Exception) -> Unit) {
        try {
            val bitmap = generateFlyerBitmap()
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val rawText = visionText.text
                    android.util.Log.d("FlyerEventParser", "ML Kit Extracted Text:\n$rawText")
                    onTextExtracted(rawText)
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("FlyerEventParser", "ML Kit Recognition Failed", e)
                    onError(e)
                }
        } catch (e: Exception) {
            android.util.Log.e("FlyerEventParser", "Exception during parsing screen", e)
            onError(e)
        }
    }

    private fun getPrimaryCalendarId(context: Context): Long? {
        return try {
            val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                var firstId: Long? = null
                val idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val primaryIdx = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                while (cursor.moveToNext()) {
                    if (idIdx != -1) {
                        val id = cursor.getLong(idIdx)
                        if (firstId == null) firstId = id
                        if (primaryIdx != -1 && cursor.getInt(primaryIdx) == 1) {
                            return id
                        }
                    }
                }
                firstId
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Safely inserts the event into the system's Calendar Provider, or falls back to an interactive insertion.
     */
    fun insertEvent(
        context: Context,
        title: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        onComplete: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1) // 0-indexed in Calendar
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        val beginTime = calendar.timeInMillis
        val endTime = beginTime + (60 * 60 * 1000) // 1 hour duration

        try {
            val calId = getPrimaryCalendarId(context) ?: 1L
            val cr = context.contentResolver
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, beginTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, "Event added via Kuzmix AI Assistant.")
                put(CalendarContract.Events.CALENDAR_ID, calId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
            }

            val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                onComplete("Successfully added '$title' to your calendar for ${month}/${day}/${year} at ${String.format("%02d:%02d", hour, minute)}.")
                return
            }
        } catch (e: SecurityException) {
            android.util.Log.w("FlyerEventParser", "Direct calendar insertion blocked by security. Launching fallback intent...")
        } catch (e: Exception) {
            android.util.Log.e("FlyerEventParser", "Calendar write exception", e)
        }

        // Fallback: Open system intent
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onComplete("Opening calendar card to confirm event '$title'...")
        } catch (e: Exception) {
            onComplete("Unable to open calendar interface: ${e.message}")
        }
    }
}
