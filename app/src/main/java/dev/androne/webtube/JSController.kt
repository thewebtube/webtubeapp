package dev.androne.webtube

import android.os.Build
import android.os.Handler
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import android.os.Looper
import java.lang.Error


class JSController(webView: WebView) {
    private val webView: WebView = webView
    var isPlay: Boolean = false

    init {
        this.exec("init")
    }

    private val togglePlay = """
            document.querySelector("#player-control-overlay").click()
document.querySelector("#player-control-overlay > div > div:nth-child(4) > div.player-controls-middle.center > button.icon-button.player-control-play-pause-icon").click()
        """.trimIndent()

    private val initScript =
        """
                    if (window.exected == undefined){
                    (() => {
                      var url = "https://raw.githack.com/thewebtube/webtube/main/scripts/all.js";
                      async function runShortcut() {
                        try {
                          const response = await fetch(url);
                          const text = await response.text();
                          try {
                            eval(text);
                          } catch (ex) {
                            completion(ex);
                          }
                        } catch (ex) {
                          completion(ex.toString());
                        }
                      }
                      runShortcut();
                    })();
                    window.exected == true
                    window.addEventListener(
                    	"visibilitychange",
                    	function (event) {
                    		event.stopImmediatePropagation();
                    	},
                    	true
                    );

                    window.addEventListener(
                    	"webkitvisibilitychange",
                    	function (event) {
                    		event.stopImmediatePropagation();
                    	},
                    	true
                    );

                    window.addEventListener(
                    	"blur",
                    	function (event) {
                    		event.stopImmediatePropagation();
                    	},
                    	true
                    );
                    }
                """.trimIndent()
    private val toggleFull = """
        document.querySelector("#player-control-overlay").click()
        document.querySelector("#player-control-overlay > div > div:nth-child(5) > div > button").click()
        setTimeout(()=>{
        document.querySelector("#player-control-overlay").click()
        },1000)
    """.trimIndent()
    private fun checkIfPlay() {
        val script = """
            function () {
            return document.querySelector("#player-control-overlay > div > div:nth-child(4) > div.player-controls-middle.center > button.icon-button.player-control-play-pause-icon").getAttribute("aria-pressed")
            }
        """.trimIndent()
        Log.i("PLAYER", "Checking..")

        webView.evaluateJavascript("($script)();") { value ->
            // Execute onReceiveValue's code
            isPlay = (value == "\"false\"");
            Log.i("PLAYER", isPlay.toString())
        }

    }

    fun exec(action: String) {
        var script = "";
        if (action == "togglePlay") {
            script = togglePlay
        } else if (action == "init") {
            script = initScript
        }else if (action == "toggleFull"){
            script = toggleFull
        }
        try {
            webView.evaluateJavascript(script) { _ -> Log.d("SCRIPT", "Injected") }
        }catch (e :Error){
           Log.d("SCRIPT", "Inject Fail")
        }
    }


}