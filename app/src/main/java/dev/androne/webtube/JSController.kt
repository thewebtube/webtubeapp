package dev.androne.webtube

import android.util.Log
import android.webkit.WebView


class JSController(webView: WebView) {
    private val webView: WebView = webView
    var isPlay: Boolean = false

    init {
        this.exec("init")
    }

    private val togglePlay = """
                   document.querySelector("#player-control-overlay .player-controls-content").style.visibility = "visible"

document.querySelector("#player-control-overlay > div > div:nth-child(4) > div.player-controls-middle.center > button.icon-button.player-control-play-pause-icon").click()
               document.querySelector("#player-control-overlay .player-controls-content").style.visibility = "hidden"

        """.trimIndent()

    private val initScript =
        """
       if (!window.executed) {
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
                    
                    setInterval(()=>{
                    try{ 
                    document.querySelector("#app > div.page-container > ytm-browse > ytm-single-column-browse-results-renderer > div > div > ytm-sign-in-promo-with-background-renderer > ytm-promo > a.sign-in-link").href="https://accounts.google.com/signin/v2/identifier?service=youtube&uilel=3&passive=false&continue=https://m.youtube.com/"
                    }catch(e){}
                    
                    try{
                    document.querySelector("#menu > div > ytm-multi-page-menu-renderer > div > ytm-multi-page-menu-section-renderer:nth-child(2) > ytm-compact-link-renderer > a").href="https://accounts.google.com/signin/v2/identifier?service=youtube&uilel=3&passive=false&continue=https://m.youtube.com/"
                    }catch(e){}
                    
                    
                    try{
                    document.querySelector("#menu > div > ytm-multi-page-menu-renderer > div > ytm-multi-page-menu-section-renderer > ytm-compact-link-renderer:nth-child(1) > a").href="https://accounts.google.com/signin/v2/identifier?continue=https://m.youtube.com/&app=m&hl=fr&next=%2F&hl=fr&passive=false&service=youtube&uilel=0&flowName=GlifWebSignIn&flowEntry=AddSession"
                    }catch(e){}
                    }
                    ,1000)
                    window.executed = true
                    
                }
                   

                    
                """.trimIndent()

    private val search = """
        document.querySelector("#header-bar > header > div > button").click()

    """.trimIndent()


    private val toggleFull = """
document.querySelector("#player-control-overlay").classList.add("fadein")
setTimeout(()=>{
document.querySelector("#player-control-overlay > div > div:nth-child(4) > div > button").click()
document.querySelector("#player-control-overlay").classList.remove("fadein")
},100)


 """.trimIndent()


    private val exitFullScreen = """
        if(Array.from(document.querySelector("body").attributes,x => x.name).toString().includes("fullscreen")){
          $toggleFull
        }
    """.trimIndent()

    private val enterFullScreen = """
      if(!Array.from(document.querySelector("body").attributes,x => x.name).toString().includes("fullscreen")){
         $toggleFull
        }
    """.trimIndent()
    private val enterFullScreenWithoutPlayer = """
    document.getElementsByTagName('video')[0].webkitEnterFullscreen()
    """.trimIndent()
    private val exitFullScreenWithoutPlayer = """
        document.getElementsByTagName('video')[0].webkitExitFullscreen()
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
        } else if (action == "toggleFull") {
            script = toggleFull
        } else if (action == "exitFullScreen") {
            script = exitFullScreen
        } else if (action == "enterFullScreen") {
            script = enterFullScreen
        } else if (action == "search") {
            script = search
        } else if (action == "enterFullScreenWithoutPlayer") {
            script = enterFullScreenWithoutPlayer
        } else if (action == "exitFullScreenWithoutPlayer") {
            script = exitFullScreenWithoutPlayer
        }
        try {
            webView.evaluateJavascript(script) { _ -> Log.d("SCRIPT", "Injected") }
        } catch (e: Error) {
            Log.d("SCRIPT", "Inject Fail")
        }
    }


}