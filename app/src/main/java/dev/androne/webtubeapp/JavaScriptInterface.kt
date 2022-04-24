package xyz.webtubeapp

import android.content.Context
import android.content.Intent
import android.webkit.JavascriptInterface

class JavaScriptInterface(var mContext: Context) {
    @JavascriptInterface
    fun openSettings(): Int {
        val myIntent = Intent(
            mContext,
            Settings::class.java
        )

        mContext.startActivity(myIntent)

        return 2
    }
}