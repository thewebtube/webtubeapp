package xyz.webtubeapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.WebView
import androidx.core.app.NotificationCompat

class BackgroundPlayHelper(var context: Context, var webView: WebView) {
    var sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)


    val isBackgroundPlayEnabled: Boolean
        get() = sp.getBoolean(PREF_BACKGROUND_PLAY_ENABLED, true)

    fun playInBackground() {
        //TODO: play in background
    }

    fun showBackgroundPlaybackNotification() {
        //TODO: Add a notification for when the video is playing in the background
    }

    fun hideBackgroundPlaybackNotification() {
        //TODO: Implement
    }

    companion object {
        const val PREF_BACKGROUND_PLAY_ENABLED = "backgroundPlayEnabled"
        private const val NOTIFICATION_ID = 1337 - 420 * 69
    }

}