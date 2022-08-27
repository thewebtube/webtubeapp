package xyz.webtubeapp

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebViewFeature
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.Duration
import com.github.javiersantos.appupdater.enums.UpdateFrom


class MainActivity : AppCompatActivity() {
    val ACTION_TOGGLE_PLAY = "togglePlay"


    private var urlFinished: String = ""
    var webView: customWebView? = null
    var progressBar: ProgressBar? = null
    private var javaScriptInterFace: JavaScriptInterface? = null
    var jsc: JSController? = null
    private var THEME = "THEME"
    private var backgroundPlayHelper : BackgroundPlayHelper? = null
    private var isFullScreen = false
    var togglePlay: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            jsc?.exec("togglePlay")
        }
    }
    @SuppressLint("SetJavaScriptEnabled", "WrongViewCast", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val appUpdater = AppUpdater(this)
            .setDisplay(Display.DIALOG)
            //.setDisplay(Display.NOTIFICATION)
            .setDuration(Duration.INDEFINITE)
            .setUpdateFrom(UpdateFrom.JSON)
            .setUpdateFrom(UpdateFrom.GITHUB)
            //.showAppUpdated(true)
            .setUpdateJSON("https://raw.githubusercontent.com/thewebtube/webtube/main/update.json")
            .setGitHubUserAndRepo("thewebtube", "webtube")

        appUpdater.start()

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(togglePlay,  IntentFilter(ACTION_TOGGLE_PLAY))

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        supportActionBar?.hide()
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progress_bar_home)
        jsc = JSController(webView!!, this)
        backgroundPlayHelper =  BackgroundPlayHelper(this, webView!!)
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
            alert.setTitle("WebTube")
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
            webView!!.settings.mediaPlaybackRequiresUserGesture = false;

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
                    openYT(intent.data.toString())
                }


            webView!!.webChromeClient = object : WebChromeClient() {
                // implement fullscreen functionality to youtube video
                private var mCustomView: View? = null
                private var mCustomViewCallback: CustomViewCallback? = null
                protected var mFullscreenContainer: FrameLayout? = null
                private var mOriginalOrientation = 0
                private var mOriginalSystemUiVisibility = 0

                @SuppressLint("WrongConstant")
                override fun onHideCustomView() {
                    (this@MainActivity.window.decorView as FrameLayout).removeView(mCustomView)
                    isFullScreen = false
                    mCustomView = null

                    this@MainActivity.window.decorView.systemUiVisibility =
                        mOriginalSystemUiVisibility
                    this@MainActivity.requestedOrientation = mOriginalOrientation
                    mCustomViewCallback!!.onCustomViewHidden()
                    mCustomViewCallback = null
                }

                override fun onShowCustomView(
                    paramView: View?,
                    paramCustomViewCallback: CustomViewCallback?
                ) {
                    if (mCustomView != null) {
                        onHideCustomView()
                        return
                    }
                    isFullScreen = true
                    mCustomView = paramView
                    mOriginalSystemUiVisibility =
                        this@MainActivity.window.decorView.systemUiVisibility
                    mOriginalOrientation = this@MainActivity.requestedOrientation
                    mCustomViewCallback = paramCustomViewCallback
                    (this@MainActivity.window.decorView as FrameLayout).addView(
                        mCustomView,
                        FrameLayout.LayoutParams(-1, -1)
                    )
                    this@MainActivity.window.decorView.systemUiVisibility = 3846
                }



                override fun onProgressChanged(view: WebView, progress: Int) {
                    //                    getActivity().setProgress(progress * 100);
                    if (progress == 100) progressBar!!.visibility = View.GONE
                }

                override fun getDefaultVideoPoster(): Bitmap? {
                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
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

    // on intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = intent.data
        openYT(url.toString())
    }
    private fun openYT(url : String) {
        if (url.contains("youtube.com/watch?v=") || url.toString().contains("youtu.be/")) {
            urlFinished = url.toString()

            // transform url to mobile version

            urlFinished = urlFinished.replace("youtu.be/", "youtube.com/watch?v=")
            // check if url is mobile version
            if (!urlFinished.contains("m.youtube.com")) {
                // remove www. from url
                urlFinished = urlFinished.replace("www.", "")
                urlFinished = urlFinished.replace("youtube.com", "m.youtube.com")
            }
            //Toast.makeText(this, "URL : $urlFinished", Toast.LENGTH_SHORT).show()
            webView!!.post(Runnable {
                webView!!.loadUrl(urlFinished)
            })
        } else {

                webView!!.post(Runnable { webView!!.loadUrl("https://m.youtube.com/?app=m") })

        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (isVideoView()) {
            webView!!.postDelayed({
                  jsc?.exec("toggleFull")
            }, 200)

        }

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // disable status bar
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }

            supportActionBar?.hide()
        }else{
            // enable status bar
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.statusBars())
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

            supportActionBar?.show()
        }
    }

    override fun onResume() {
        initTheme()
        super.onResume()
        backgroundPlayHelper!!.hideBackgroundPlaybackNotification();

    }

    override fun onPause() {
        //if (this.isVideoView()){
        //    jsc!!.exec("popup")
        //}
        if (backgroundPlayHelper!!.isBackgroundPlayEnabled) {
            backgroundPlayHelper!!.playInBackground();
        } else {
            jsc?.exec("pause")
        }
        super.onPause()
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
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webSettings, FORCE_DARK_OFF)
            }
        }

    }
    private fun isVideoView(): Boolean {
        return webView?.url.toString().contains("youtube.com/watch?v=")
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundPlayHelper!!.hideBackgroundPlaybackNotification();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(togglePlay)
    }
}