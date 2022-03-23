package dev.androne.webtube

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.Toast
import java.io.InputStream
import java.lang.Exception
import java.net.URLEncoder
import android.app.Activity;
import android.app.PictureInPictureParams
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap;
import android.view.*
import android.webkit.WebChromeClient.CustomViewCallback
import android.widget.FrameLayout;
import android.webkit.WebResourceRequest

import android.webkit.WebResourceResponse
import android.webkit.ValueCallback
import android.view.View.OnTouchListener
import android.webkit.JavascriptInterface
import androidx.annotation.RequiresApi
import dev.androne.webtube.JSController
import android.view.WindowManager





class MainActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var customViewContainer: FrameLayout? = null
    private var customViewCallback: CustomViewCallback? = null
    private var mCustomView: View? = null
    private var mWebChromeClient: myWebChromeClient? = null
    private var mWebViewClient: myWebViewClient? = null
    private val allowed = arrayOf("youtube.com", "google.ae","google.am","google.as","google.at","google.az","google.ba","google.be","google.bg","google.bi","google.bs","google.ca","google.cd","google.cg","google.ch","google.ci","google.cl","google.co.bw","google.co.ck","google.co.cr","google.co.hu","google.co.id","google.co.il","google.co.im","google.co.in","google.co.je","google.co.jp","google.co.ke","google.co.kr","google.co.ls","google.co.ma","google.co.nz","google.co.th","google.co.ug","google.co.uk","google.co.uz","google.co.ve","google.co.vi","google.co.za","google.co.zm","google.com","google.com.af","google.com.ag","google.com.ar","google.com.au","google.com.bd","google.com.bo","google.com.br","google.com.bz","google.com.co","google.com.cu","google.com.do","google.com.ec","google.com.eg","google.com.et","google.com.fj","google.com.gi","google.com.gt","google.com.hk","google.com.jm","google.com.kw","google.com.ly","google.com.mt","google.com.mx","google.com.my","google.com.na","google.com.nf","google.com.ni","google.com.np","google.com.om","google.com.pa","google.com.pe","google.com.ph","google.com.pk","google.com.pr","google.com.py","google.com.qa","google.com.sa","google.com.sb","google.com.sg","google.com.sv","google.com.tj","google.com.tr","google.com.tw","google.com.ua","google.com.uy","google.com.uz","google.com.vc","google.com.vn","google.cz","google.de","google.dj","google.dk","google.dm","google.ee","google.es","google.fi","google.fm","google.fr","google.gg","google.gl","google.gm","google.gr","google.hn","google.hr","google.ht","google.hu","google.ie","google.is","google.it","google.jo","google.kg","google.kz","google.li","google.lk","google.lt","google.lu","google.lv","google.md","google.mn","google.ms","google.mu","google.mw","google.net","google.nl","google.no","google.nr","google.nu","google.off.ai","google.org","google.pl","google.pn","google.pt","google.ro","google.ru","google.rw","google.sc","google.se","google.sh","google.si","google.sk","google.sm","google.sn","google.tm","google.to","google.tp","google.tt","google.tv","google.uz","google.vg","google.vu","google.ws")
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
            webView!!.onPause() // don't send to youtube (to play in backgroud)
        }
    }

    override fun onResume() {
        super.onResume() //To change body of overridden methods use File | Settings | File Templates
        if (!isVideoView()) {
            webView!!.onResume()
        }
    }

    override fun onStop() {
        super.onStop() //To change body of overridden methods use File | Settings | File Templates.
        if (inCustomView()) {
            hideCustomView()
        }
    }

    override fun onUserLeaveHint() {
        if (isVideoView()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setPictureInPictureParams(
                    PictureInPictureParams.Builder()
                        .setAutoEnterEnabled(true)
                        .build()
                )
            }else {
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
            hideSystemUI()
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
            showSystemUI()
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
