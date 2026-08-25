package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder

class KuzmixVideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            startVideo()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            releasePlayer()
        }

        private fun startVideo() {
            releasePlayer()
            val sharedPref = getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
            val uriString = sharedPref.getString("live_wallpaper_video_uri", null) ?: return

            try {
                mediaPlayer = MediaPlayer().apply {
                    setSurface(surfaceHolder.surface)
                    setDataSource(applicationContext, Uri.parse(uriString))
                    isLooping = true
                    setVolume(0f, 0f)
                    setOnPreparedListener { mp ->
                        mp.start()
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("KuzmixVideoWallpaper", "Error playing video wallpaper", e)
            }
        }

        private fun releasePlayer() {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                Log.e("KuzmixVideoWallpaper", "Error releasing player", e)
            }
        }
    }
}
