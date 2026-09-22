package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class IconStyleId(val id: String, val displayName: String, val description: String) {
    KUZMIX("kuzmix", "Kuzmix", "Our signature icon pack. Bold, clean and modern."),
    DEFAULT("default", "Default", "Use the original application icons.");

    companion object {
        fun fromId(id: String): IconStyleId =
            entries.firstOrNull { it.id == id } ?: KUZMIX
    }
}

object IconPackageManager {
    private val PACKAGE_MAP = mapOf(
        "com.google.android.googlequicksearchbox" to "Google",
        "com.google.android.gm" to "Gmail",
        "com.google.android.youtube" to "YouTube",
        "com.google.android.apps.maps" to "Google-Maps",
        "com.android.chrome" to "Google-Chrome",
        "com.android.vending" to "Settings",
        "com.google.android.apps.photos" to "Photos",
        "com.google.android.apps.docs" to "Files",
        "com.android.settings" to "Settings",
        "com.google.android.apps.nbu.files" to "Files",
        "com.android.camera" to "Camera",
        "com.google.android.apps.gallery3d" to "Photos",
        "com.google.android.dialer" to "Phone",
        "com.google.android.apps.messaging" to "Messages",
        "com.android.phone" to "Phone",
        "com.android.mms" to "Messages",
        "com.samsung.android.dialer" to "Phone",
        "com.samsung.android.messaging" to "Messages",
        "com.samsung.android.camera" to "Camera",
        "com.sec.android.app.camera" to "Camera",
        "com.whatsapp" to "WhatsApp",
        "com.instagram.android" to "Instagram",
        "com.facebook.katana" to "Facebook",
        "com.facebook.orca" to "Messenger",
        "com.twitter.android" to "Twitter",
        "com.snapchat.android" to "Snapchat",
        "com.tiktok.android" to "TikTok",
        "com.spotify.music" to "Spotify",
        "com.netflix.mediaclient" to "Netflix",
        "com.discord" to "Discord",
        "com.reddit.frontpage" to "Reddit",
        "com.linkedin.android" to "LinkedIn",
        "com.pinterest" to "Pinterest",
        "org.telegram.messenger" to "Telegram",
        "com.twitch.android.app" to "Twitch",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.ubercab" to "Uber",
        "com.ubercab.eats" to "Uber-Eats",
        "com.lyft.android" to "Lyft",
        "com.amazon.mShop.android.shopping" to "Amazon",
        "com.walmart.android" to "Walmart",
        "com.doordash.android" to "DoorDash",
        "com.paypal.android.p2pmobile" to "PayPal",
        "com.cash.app" to "Cash-App",
        "com.starbucks.mobilecard" to "Starbucks",
        "com.chickfila.cfa" to "Chick-fil-A",
        "com.roblox.client" to "Roblox",
        "com.innersloth.spacemafia" to "Among-Us",
        "com.hulu.plus" to "Hulu",
        "com.disney.disneyplus" to "Disney+",
        "com.hbo.hbonow" to "HBO-Max",
        "com.zoom.videomeetings" to "Zoom",
        "com.google.android.apps.translate" to "Translate",
        "com.google.android.apps.fitness" to "Fitness",
        "com.apple.android.music" to "Apple-Music",
        "com.google.android.apps.books" to "Books",
        "com.google.android.calendar" to "Calendar",
        "com.google.android.deskclock" to "Clock",
        "com.google.android.contacts" to "Contacts",
        "com.google.android.apps.reminders" to "Reminders",
        "com.google.android.keep" to "Notes",
        "com.google.android.apps.photos.lite" to "Photos",
        "com.google.android.apps.magazines" to "News",
        "com.google.android.apps.walletnfcrel" to "Wallet",
        "com.google.android.apps.chromecast.app" to "Watch",
        "com.google.android.apps.weather" to "Weather",
        "com.trustwallet.client" to "Trust",
        "com.vkontakte.android" to "Facebook"
    )

    private val LABEL_TO_FILENAME = mapOf(
        "google" to "Google",
        "gmail" to "Gmail",
        "youtube" to "YouTube",
        "maps" to "Maps",
        "chrome" to "Google-Chrome",
        "play store" to "Settings",
        "photos" to "Photos",
        "drive" to "Files",
        "settings" to "Settings",
        "files" to "Files",
        "camera" to "Camera",
        "phone" to "Phone",
        "messages" to "Messages",
        "whatsapp" to "WhatsApp",
        "instagram" to "Instagram",
        "facebook" to "Facebook",
        "messenger" to "Messenger",
        "twitter" to "Twitter",
        "snapchat" to "Snapchat",
        "tiktok" to "TikTok",
        "spotify" to "Spotify",
        "netflix" to "Netflix",
        "discord" to "Discord",
        "reddit" to "Reddit",
        "linkedin" to "LinkedIn",
        "pinterest" to "Pinterest",
        "telegram" to "Telegram",
        "twitch" to "Twitch",
        "uber" to "Uber",
        "lyft" to "Lyft",
        "amazon" to "Amazon",
        "walmart" to "Walmart",
        "doordash" to "DoorDash",
        "paypal" to "PayPal",
        "cash app" to "Cash-App",
        "starbucks" to "Starbucks",
        "roblox" to "Roblox",
        "zoom" to "Zoom",
        "translate" to "Translate",
        "fitness" to "Fitness",
        "books" to "Books",
        "calendar" to "Calendar",
        "clock" to "Clock",
        "contacts" to "Contacts",
        "reminders" to "Reminders",
        "notes" to "Notes",
        "news" to "News",
        "wallet" to "Wallet",
        "weather" to "Weather",
        "calculator" to "Calculator",
        "safari" to "Safari",
        "mail" to "Mail",
        "shortcuts" to "Shortcuts",
        "health" to "Health",
        "podcasts" to "Podcasts",
        "tips" to "Tips",
        "facetime" to "Facetime"
    )

    fun resolveIconFilename(packageName: String, label: String): String? {
        PACKAGE_MAP[packageName]?.let { return it }
        val lowerLabel = label.lowercase().trim()
        LABEL_TO_FILENAME[lowerLabel]?.let { return it }
        for ((key, value) in LABEL_TO_FILENAME) {
            if (lowerLabel.contains(key)) return value
        }
        return null
    }
}

object IconResolver {
    private val iconCache = LruCache<String, Drawable>(200)
    private var currentStyle: IconStyleId = IconStyleId.KUZMIX

    fun setCurrentStyle(style: IconStyleId) {
        if (style != currentStyle) {
            iconCache.evictAll()
            currentStyle = style
        }
    }

    fun getCurrentStyle(): IconStyleId = currentStyle

    fun resolveIcon(
        context: Context,
        packageName: String,
        label: String,
        originalIcon: Drawable,
        style: IconStyleId = currentStyle
    ): Drawable {
        if (style == IconStyleId.DEFAULT) return originalIcon

        val cacheKey = "${packageName}:${style.id}"
        iconCache.get(cacheKey)?.let { return it }

        val filename = IconPackageManager.resolveIconFilename(packageName, label)
        if (filename != null) {
            val customIcon = loadCustomIcon(context, filename, style.id)
            if (customIcon != null) {
                iconCache.put(cacheKey, customIcon)
                return customIcon
            }
        }

        iconCache.put(cacheKey, originalIcon)
        return originalIcon
    }

    private fun loadCustomIcon(context: Context, filename: String, styleId: String): Drawable? {
        return try {
            val assetPath = "icon_packs/$styleId/$filename.png"
            val inputStream = context.assets.open(assetPath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap != null) BitmapDrawable(context.resources, bitmap) else null
        } catch (e: Exception) {
            null
        }
    }

    fun invalidateCache() {
        iconCache.evictAll()
    }

    fun getPreviewIcons(context: Context): List<Pair<String, Drawable>> {
        val previewPackages = listOf(
            "com.google.android.googlequicksearchbox" to "Google",
            "com.google.android.gm" to "Gmail",
            "com.google.android.youtube" to "YouTube",
            "com.google.android.apps.maps" to "Google-Maps",
            "com.android.vending" to "Play Store",
            "com.android.chrome" to "Chrome",
            "com.google.android.apps.photos" to "Photos",
            "com.google.android.apps.docs" to "Drive",
            "com.android.settings" to "Settings",
            "com.google.android.apps.nbu.files" to "Files",
            "com.android.camera" to "Camera",
            "com.google.android.apps.gallery3d" to "Gallery"
        )

        return previewPackages.mapNotNull { (pkg, label) ->
            val filename = IconPackageManager.resolveIconFilename(pkg, label)
            if (filename != null) {
                val icon = loadCustomIcon(context, filename, currentStyle.id)
                if (icon != null) label to icon else null
            } else null
        }
    }

    fun getAvailableIconCount(context: Context): Int {
        return try {
            context.assets.list("icon_packs/${currentStyle.id}")?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }
}

object IconStylePreferences {
    private const val PREFS_NAME = "KuzmixSettings"
    private const val KEY_ICON_STYLE = "selected_icon_style"

    fun getSelectedStyle(context: Context): IconStyleId {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_ICON_STYLE, null)
        return if (saved != null) IconStyleId.fromId(saved) else IconStyleId.KUZMIX
    }

    fun setSelectedStyle(context: Context, style: IconStyleId) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ICON_STYLE, style.id)
            .apply()
    }
}
