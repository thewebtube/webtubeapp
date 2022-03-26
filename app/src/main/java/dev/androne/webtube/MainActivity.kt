package dev.androne.webtube

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.LayoutInflater
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.Duration
import com.github.javiersantos.appupdater.enums.UpdateFrom


open class MainActivity : AppCompatActivity() {
    private val data: Uri? = intent?.data

    private var webView: WebView? = null
    private var customViewContainer: FrameLayout? = null
    private var customViewCallback: CustomViewCallback? = null
    private var mCustomView: View? = null
    private var mWebChromeClient: myWebChromeClient? = null
    private var mWebViewClient: myWebViewClient? = null
    private var urlFinished = ""
    private var jsc: JSController? = null
    private var pipMode: Boolean = false

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        customViewContainer = findViewById<View>(R.id.customViewContainer) as FrameLayout
        val appUpdater = AppUpdater(this)
            .setDisplay(Display.SNACKBAR)
            .setDisplay(Display.NOTIFICATION)
            .setDuration(Duration.INDEFINITE)
            .setUpdateFrom(UpdateFrom.JSON)
            .setUpdateJSON("https://raw.githubusercontent.com/thewebtube/webtube/main/update.json")
        appUpdater.start()
        webView = findViewById<View>(R.id.webView) as WebView
        mWebViewClient = myWebViewClient()
        webView!!.webViewClient = mWebViewClient!!
        mWebChromeClient = myWebChromeClient()
        webView!!.webChromeClient = mWebChromeClient
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.setAppCacheEnabled(true)
        webView!!.settings.builtInZoomControls = false
        webView!!.settings.saveFormData = true


        if (this.data?.host?.endsWith("youtube.com") == true) {
            webView!!.loadUrl(this.data.encodedPath.toString())
        } else {
            if (savedInstanceState != null)
                webView!!.restoreState(savedInstanceState)
            else
                webView!!.loadUrl("https://m.youtube.com")

        }
        jsc = JSController(webView!!)
    }

    private fun inCustomView(): Boolean {
        return mCustomView != null
    }

    private fun hideCustomView() {
        mWebChromeClient!!.onHideCustomView()
    }

    override fun onPause() {
        val sharedPref = this?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("lastURL", webView!!.url.toString())
            apply()
        }
        if (!isVideoView()) {
            webView!!.onPause() // don't send to youtube (to play in background)
        }
        super.onPause() //To change body of overridden methods use File | Settings | File Templates.

    }

    override fun onResume() {
        super.onResume() //To change body of overridden methods use File | Settings | File Templates
        val sharedPref = this?.getPreferences(Context.MODE_PRIVATE) ?: return
        val url = sharedPref.getString("lastURL", null)
        if (url != null){

            val path = Uri.parse(url).path
            if (path != "/" && path != "") {
                webView?.loadUrl(url)
                jsc = JSController(webView!!)
            }

        }
        if (!isVideoView()) {
            webView!!.onResume()
        }
    }

    override fun onStop() {
        super.onStop() //To change body of overridden methods use File | Settings | File Templates.
        if (inCustomView()) {
            hideCustomView()
        }
        finish()
    }

    override fun onUserLeaveHint() {

        if (isVideoView()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setPictureInPictureParams(
                    PictureInPictureParams.Builder()
                        .setAutoEnterEnabled(true)
                        .build()
                )
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    enterPictureInPictureMode()
                }
            }
        }


    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        pipMode = isInPictureInPictureMode

        if (isInPictureInPictureMode) {
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            jsc?.exec("enterFullScreen")
        } else {
            jsc?.exec("exitFullScreen")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (inCustomView()) {
                hideCustomView()
                return true
            }
            if (mCustomView == null && webView!!.canGoBack()) {
                webView!!.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    internal inner class myWebChromeClient : WebChromeClient() {
        private var mVideoProgressView: View? = null
        override fun onShowCustomView(
            view: View,
            requestedOrientation: Int,
            callback: CustomViewCallback
        ) {
            onShowCustomView(
                view,
                callback
            ) //To change body of overridden methods use File | Settings | File Templates.
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {

            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden()
                return
            }
            mCustomView = view
            webView!!.visibility = View.GONE
            customViewContainer!!.visibility = View.VISIBLE
            customViewContainer!!.addView(view)
            customViewCallback = callback
            if (!pipMode) {
                hideSystemUI()
            }
        }

        @SuppressLint("InflateParams")
        override fun getVideoLoadingProgressView(): View? {
            if (mVideoProgressView == null) {
                val inflater = LayoutInflater.from(this@MainActivity)
                mVideoProgressView = inflater.inflate(R.layout.video_progress, null)
            }
            return mVideoProgressView
        }

        override fun onHideCustomView() {
            super.onHideCustomView() //To change body of overridden methods use File | Settings | File Templates.
            if (mCustomView == null) return
            webView!!.visibility = View.VISIBLE
            customViewContainer!!.visibility = View.GONE

            // Hide the custom view.
            mCustomView!!.visibility = View.GONE

            // Remove the custom view from its container.
            customViewContainer!!.removeView(mCustomView)
            customViewCallback!!.onCustomViewHidden()
            mCustomView = null

            if (!pipMode) {
                showSystemUI()
            }
        }
    }

    internal inner class myWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val host = Uri.parse(url).host.toString()
            Uri.parse(url).path.toString()
            if (host == "m.youtube.com" || host == "youtube.com" || host.contains("accounts")) { // for google login
                return false
            }

            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                startActivity(this) //Here's the problem!
            }
            //When I hover my mouse over 'this', a popup appears with the type mismatch error.
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (urlFinished != url) {
                // do your stuff here
                val host = Uri.parse(url).host.toString()
                if (host == "m.youtube.com") {
                    jsc?.exec("init")
                    //Toast.makeText(this@MainActivity, "injected", Toast.LENGTH_SHORT).show()
                }
            }

            urlFinished = url
            super.onPageFinished(view, url)

        }

    }


    private fun isVideoView(): Boolean {
        return webView?.url.toString().contains("youtube.com/watch?v=")
    }

    fun hideSystemUI() {
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    fun showSystemUI() {
        if (supportActionBar != null) {
            supportActionBar!!.show()
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }


}
