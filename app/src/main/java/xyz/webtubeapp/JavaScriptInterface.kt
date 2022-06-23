package xyz.webtubeapp

import android.R
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener


class JavaScriptInterface(var mContext: Context) {
    @JavascriptInterface
    fun openSettings(): Int {
        val myIntent = Intent(
            mContext,
            SettingsView::class.java
        )

        mContext.startActivity(myIntent)

        return 2
    }
    @JavascriptInterface
    fun popup(url : String): Int{

        //TODO Toast.makeText(mContext, "test : $url", Toast.LENGTH_SHORT).show()


        return 0
    }
}
