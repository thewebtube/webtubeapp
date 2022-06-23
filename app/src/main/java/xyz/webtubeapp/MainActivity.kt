package xyz.webtubeapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import android.widget.Toast
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

        // Uninstall old apk
        if (isPackageInstalled(this,"dev.androne.webtube")){
            Toast.makeText(this, getString(R.string.dialog_message_uninstall_old_package), Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:dev.androne.webtube")
            startActivity(intent)
        }

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


                override fun onHideCustomView() {
                    (window.decorView as FrameLayout).removeView(mCustomView)
                    mCustomView = null
                    window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
                    requestedOrientation = mOriginalOrientation
                    mCustomViewCallback!!.onCustomViewHidden()
                    mCustomViewCallback = null
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
                    (window.decorView as FrameLayout).addView(
                        mCustomView,
                        FrameLayout.LayoutParams(-1, -1)
                    )
                    window.decorView.systemUiVisibility = 3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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

    override fun onResume() {
        initTheme()
        super.onResume()
    }

    override fun onPause() {
        if (this.isVideoView()){
            jsc!!.exec("popup")
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
    private fun isPackageInstalled(context: Context, packageName: String?): Boolean {
        val packageManager: PackageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName!!) ?: return false
        val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list.size > 0
    }

}