package dev.androne.webtube

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.KeyEvent
import android.view.Window
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.Toast
import java.io.InputStream
import java.lang.Exception
import java.net.URLEncoder
import android.app.Activity;
import android.content.Context
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.FrameLayout;
import android.webkit.WebResourceRequest

import android.webkit.WebResourceResponse
import android.webkit.ValueCallback
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.webkit.JavascriptInterface
import dev.androne.webtube.JSController


class MainActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var customViewContainer: FrameLayout? = null
    private var customViewCallback: CustomViewCallback? = null
    private var mCustomView: View? = null
    private var mWebChromeClient: myWebChromeClient? = null
    private var mWebViewClient: myWebViewClient? = null
    private val allowed = arrayOf("youtube.com", "google.com")
    private var urlFinished = "";
    private var jsc: JSController? = null;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        customViewContainer = findViewById<View>(R.id.customViewContainer) as FrameLayout
        webView = findViewById<View>(R.id.webView) as WebView
        mWebViewClient = myWebViewClient()
        webView!!.webViewClient = mWebViewClient!!
        mWebChromeClient = myWebChromeClient()
        webView!!.webChromeClient = mWebChromeClient
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.setAppCacheEnabled(true)
        webView!!.settings.builtInZoomControls = true
        webView!!.settings.saveFormData = true
        webView!!.settings.allowUniversalAccessFromFileURLs = true;
        webView!!.loadUrl("http://m.youtube.com")
        jsc = JSController(webView!!)
    }

    fun inCustomView(): Boolean {
        return mCustomView != null
    }

    fun hideCustomView() {
        mWebChromeClient!!.onHideCustomView()
    }

    override fun onPause() {
        super.onPause() //To change body of overridden methods use File | Settings | File Templates.
        if (!isVideoView()) {
            webView!!.onPause()
        }
    }

    override fun onResume() {
        if (!isVideoView()) {
            super.onResume() //To change body of overridden methods use File | Settings | File Templates.
        }
        //webView!!.onResume()
    }

    override fun onStop() {
        super.onStop() //To change body of overridden methods use File | Settings | File Templates.
        if (inCustomView()) {
            hideCustomView()
        }
    }

    override fun onUserLeaveHint() {
        if (isVideoView()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                enterPictureInPictureMode()
                jsc?.exec("toggleFull")
            }
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
        private val mDefaultVideoPoster: Bitmap? = null
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
        }

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
        }
    }

    internal inner class myWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val host = Uri.parse(url).host.toString()
            val path = Uri.parse(url).path.toString()
            allowed.forEach {
                if (host.endsWith(it)) {
                    return false
                }
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
                val path = Uri.parse(url).path.toString()
                if (host.endsWith("youtube.com") && (path == "/" || path == "")) {
                    jsc?.exec("init")
                }
            }

            urlFinished = url;
            super.onPageFinished(view, url);

        }

    }


    private fun isVideoView(): Boolean {
        return webView?.url.toString().contains("youtube.com/watch?v=")
    }


}
