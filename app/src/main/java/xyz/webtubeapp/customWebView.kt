package xyz.webtubeapp

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class customWebView : WebView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr) {
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        val preferences: SharedPreferences = this.context.getSharedPreferences("THEME",
            AppCompatActivity.MODE_PRIVATE)

        if (visibility != GONE && visibility != INVISIBLE || !this.url.toString().contains("youtube.com/watch?v=") || !preferences.getBoolean("BACKGROUND_MODE",true)) super.onWindowVisibilityChanged(
            visibility)
    }
}