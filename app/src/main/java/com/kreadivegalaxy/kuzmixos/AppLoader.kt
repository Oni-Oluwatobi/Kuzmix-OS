package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppItem(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    val intent: Intent
)

class AppLoader(private val context: Context) {
    suspend fun loadApps(): List<AppItem> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            
            resolveInfos.mapNotNull { resolveInfo ->
                try {
                    val appIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    }
                    val label = try { resolveInfo.loadLabel(pm).toString() } catch (e: Throwable) { resolveInfo.activityInfo.packageName }
                    val originalIcon = (try { resolveInfo.loadIcon(pm) } catch (e: Throwable) { null })
                        ?: (try { androidx.core.content.ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon) } catch (e: Throwable) { null })
                        ?: android.graphics.drawable.ColorDrawable(android.graphics.Color.DKGRAY)
                    val icon = IconResolver.resolveIcon(
                        context = context,
                        packageName = resolveInfo.activityInfo.packageName,
                        label = label,
                        originalIcon = originalIcon
                    )
                    AppItem(
                        label = label,
                        packageName = resolveInfo.activityInfo.packageName,
                        icon = icon,
                        intent = appIntent
                    )
                } catch (e: Throwable) {
                    null
                }
            }.sortedBy { it.label.lowercase() }
        } catch (e: Throwable) {
            android.util.Log.e("KuzmixOS", "Failed to load apps: ${e.message}")
            emptyList()
        }
    }
}
