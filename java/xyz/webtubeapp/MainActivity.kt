package xyz.webtubeapp


import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebViewFeature
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.Duration
import com.github.javiersantos.appupdater.enums.UpdateFrom


class MainActivity : AppCompatActivity() {
    private var urlFinished: String = ""
    var webView: customWebView? = null
    var progressBar: ProgressBar? = null
    private var javaScriptInterFace: JavaScriptInterface? = null
    private var jsc: JSController? = null
    private var THEME = "THEME"
    private var pipMode: Boolean = false


    @SuppressLint("SetJavaScriptEnabled", "WrongViewCast", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appUpdater = AppUpdater(this)
            .setDisplay(Display.DIALOG)
            //.setDisplay(Display.NOTIFICATION)
            .setDuration(Duration.INDEFINITE)
            .setUpdateFrom(UpdateFrom.JSON)
            //.showAppUpdated(true)
            .setUpdateJSON("https://raw.githubusercontent.com/thewebtube/webtube/main/update.json")

        appUpdater.start()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        supportActionBar?.hide()
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progress_bar_home)
        jsc = JSController(webView!!)
        if (!isNetworkAvailable) {
            val a_builder1 = AlertDialog.Builder(this)
            a_builder1.setMessage("No Internet Connection Please Check Internet Connection !!!")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog: DialogInterface?, which: Int ->
                    val iLoginVendor = Intent(this@MainActivity, MainActivity::class.java)
                    startActivity(iLoginVendor)
                    finishAffinity()
                }
            val alert = a_builder1.create()
            alert.setTitle("YouTube")
            alert.show()
        } else {
            webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            val webSettings = webView!!.settings
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            javaScriptInterFace = JavaScriptInterface(this)
            webView!!.addJavascriptInterface(javaScriptInterFace!!, "WT")
            webSettings.javaScriptEnabled = true
            webView!!.settings.domStorageEnabled = true
            webView!!.settings.javaScriptCanOpenWindowsAutomatically = true
            webView!!.settings.loadsImagesAutomatically = true
            webSettings.allowFileAccess = true
            webSettings.useWideViewPort = true
            webSettings.loadWithOverviewMode = true
            webSettings.setAppCacheEnabled(true)
            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
            initTheme()



            if (Build.VERSION.SDK_INT >= 21) {
                webSettings.mixedContentMode = 0
                webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            } else if (Build.VERSION.SDK_INT >= 19) {
                webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            } else {
                webView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }
            webView!!.webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    webView: WebView,
                    errorCode: Int,
                    description: String,
                    failingUrl: String,
                ) {
                    try {
                        webView.stopLoading()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (webView.canGoBack()) {
                        webView.goBack()
                    }
                    webView.loadUrl("about:blank")
                    val alertDialog = AlertDialog.Builder(this@MainActivity).create()
                    alertDialog.setTitle("Error")
                    alertDialog.setMessage(getString(R.string.check_internet))
                    alertDialog.setButton(
                        DialogInterface.BUTTON_POSITIVE,
                        getString(R.string.try_again)
                    ) { dialog, which ->
                        finish()
                        startActivity(intent)
                    }
                    alertDialog.show()
                    super.onReceivedError(webView, errorCode, description, failingUrl)
                }
            }

            //Main Code For Landscape Video
            if (savedInstanceState == null) {
                webView!!.post(Runnable { webView!!.loadUrl("https://m.youtube.com/?app=m") })
            }
            webView!!.webChromeClient = object : WebChromeClient() {
                @SuppressLint("StaticFieldLeak")
                private var mCustomView: View? = null
                private var mCustomViewCallback: CustomViewCallback? = null

                @SuppressLint("StaticFieldLeak")
                protected var mFullscreenContainer: FrameLayout? = null
                private var mOriginalOrientation = 0
                private var mOriginalSystemUiVisibility = 0

                // ChromeClient() {}
                override fun getDefaultVideoPoster(): Bitmap? {
                    return if (mCustomView == null) {
                        null
                    } else BitmapFactory.decodeResource(applicationContext.resources, 2130837573)
                }

                override fun onHideCustomView() {
                    (window.decorView as FrameLayout).removeView(mCustomView)
                    mCustomView = null
                    window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
                    requestedOrientation = mOriginalOrientation
                    mCustomViewCallback!!.onCustomViewHidden()
                    mCustomViewCallback = null
                    if (!pipMode) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                     //showSystemUI()
                    }
                }

                override fun onShowCustomView(
                    paramView: View,
                    paramCustomViewCallback: CustomViewCallback,
                ) {
                    if (mCustomView != null) {
                        onHideCustomView()
                        return
                    }
                    mCustomView = paramView
                    mOriginalSystemUiVisibility = window.decorView.systemUiVisibility
                    mOriginalOrientation = requestedOrientation
                    mCustomViewCallback = paramCustomViewCallback
                    if (!pipMode) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        //hideSystemUI()
                    }
                    (window.decorView as FrameLayout).addView(
                        mCustomView,
                        FrameLayout.LayoutParams(-1, -1)
                    )
                    window.decorView.systemUiVisibility = 3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }

                private fun isVideoView(): Boolean {
                    return webView?.url.toString().contains("youtube.com/watch?v=")
                }

                //fun hideSystemUI() {
                  //  if (supportActionBar != null) {
                    //    supportActionBar!!.hide()
                    //}
                    //window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                      //      or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        //    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                          //  or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            //or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            //or View.SYSTEM_UI_FLAG_FULLSCREEN)
                //}


                override fun onProgressChanged(view: WebView, progress: Int) {
                    //                    getActivity().setProgress(progress * 100);
                    if (progress == 100) progressBar!!.visibility = View.GONE
                }
            }
            webView!!.webViewClient = object : WebViewClient() {

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
            webView!!.setOnKeyListener(View.OnKeyListener { view: View?, keyCode: Int, keyEvent: KeyEvent ->
                if (keyEvent.action != KeyEvent.ACTION_DOWN) return@OnKeyListener true
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webView!!.canGoBack()) {
                        webView!!.goBack()
                    } else {
                        finish()
                    }
                    return@OnKeyListener true
                }
                false
            })
        }
    }


    override fun onResume() {
        super.onResume()
        initTheme()
    }

    private val isNetworkAvailable: Boolean
        get() {
            val connectivityManager = this
                .getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager
                .activeNetworkInfo
            return activeNetworkInfo != null
        }

    private fun initTheme(){
        val preferences: SharedPreferences = this.getSharedPreferences(THEME, MODE_PRIVATE)
        val webSettings = webView!!.settings
        if (preferences.getString("COLOR", "DARK") == "DARK") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webSettings, FORCE_DARK_ON)
            }
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                WebSettingsCompat.setForceDark(webSettings, FORCE_DARK_OFF)
            }
        }

    }
    private fun isVideoView(): Boolean {
        return webView?.url.toString().contains("youtube.com/watch?v=")

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
    //fun showSystemUI() {
      //  if (supportActionBar != null) {
            //supportActionBar!!.show()
        //}
        //window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
       // window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    //}

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig:
        Configuration
    ) {
        pipMode = isInPictureInPictureMode

        if (isInPictureInPictureMode) {
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            jsc?.exec("enterFullScreen")
        } else {
            jsc?.exec("exitFullScreen")
        }
    }




}
