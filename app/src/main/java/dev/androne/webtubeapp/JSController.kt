package xyz.webtubeapp

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
    document.querySelector("#menu > div > ytm-multi-page-menu-renderer > div > ytm-multi-page-menu-section-renderer > ytm-compact-link-renderer:nth-child(1) > a").href="https://accounts.google.com/signin/v2/identifier?continue=https://m.youtube.com/&app=m&next=%2F&passive=false&service=youtube&uilel=0&flowName=GlifWebSignIn&flowEntry=AddSession"
    }catch(e){}
    }
    
    
    ,1000)
    
    function insertSettings(){
       if(document.querySelector("#wt-settings")) return
        const bar = document.querySelector("ytm-topbar-menu-button-renderer");
        const logo = document.createElement("span");
              logo.innerHTML = `<button class="icon-button topbar-menu-button-avatar-button" aria-label="Rechercher sur YouTube" aria-haspopup="false"><c3-icon>
    
    <svg xmlns="http://www.w3.org/2000/svg" height="24px" viewBox="0 0 24 24" width="24px" fill="#000000"><path d="M0 0h24v24H0V0z" fill="none"/><path d="M19.43 12.98c.04-.32.07-.64.07-.98 0-.34-.03-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64l-2-3.46c-.09-.16-.26-.25-.44-.25-.06 0-.12.01-.17.03l-2.49 1c-.52-.4-1.08-.73-1.69-.98l-.38-2.65C14.46 2.18 14.25 2 14 2h-4c-.25 0-.46.18-.49.42l-.38 2.65c-.61.25-1.17.59-1.69.98l-2.49-1c-.06-.02-.12-.03-.18-.03-.17 0-.34.09-.43.25l-2 3.46c-.13.22-.07.49.12.64l2.11 1.65c-.04.32-.07.65-.07.98 0 .33.03.66.07.98l-2.11 1.65c-.19.15-.24.42-.12.64l2 3.46c.09.16.26.25.44.25.06 0 .12-.01.17-.03l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65c.03.24.24.42.49.42h4c.25 0 .46-.18.49-.42l.38-2.65c.61-.25 1.17-.59 1.69-.98l2.49 1c.06.02.12.03.18.03.17 0 .34-.09.43-.25l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65zm-1.98-1.71c.04.31.05.52.05.73 0 .21-.02.43-.05.73l-.14 1.13.89.7 1.08.84-.7 1.21-1.27-.51-1.04-.42-.9.68c-.43.32-.84.56-1.25.73l-1.06.43-.16 1.13-.2 1.35h-1.4l-.19-1.35-.16-1.13-1.06-.43c-.43-.18-.83-.41-1.23-.71l-.91-.7-1.06.43-1.27.51-.7-1.21 1.08-.84.89-.7-.14-1.13c-.03-.31-.05-.54-.05-.74s.02-.43.05-.73l.14-1.13-.89-.7-1.08-.84.7-1.21 1.27.51 1.04.42.9-.68c.43-.32.84-.56 1.25-.73l1.06-.43.16-1.13.2-1.35h1.39l.19 1.35.16 1.13 1.06.43c.43.18.83.41 1.23.71l.91.7 1.06-.43 1.27-.51.7 1.21-1.07.85-.89.7.14 1.13zM12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 6c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"/></svg>
    </c3-icon></button>`
              logo.setAttribute("id", "wt-settings");
        bar.parentNode.insertBefore(logo, bar.nextSibling);
        document.querySelector("#wt-settings").onclick = ()=>{
        WT.openSettings()
        }
    }
    
    setInterval(insertSettings,500)

    
    window.executed = true
    }
       

    """.trimIndent()

    private val search = """
        document.querySelector("#header-bar > header > div > button").click()

    """.trimIndent()


    private val toggleFull = """
   
    document.querySelector(".fullscreen-icon").click()

     """.trimIndent()


    private val exitFullScreen = """
        if(Array.from(document.querySelector("body").attributes,x => x.name).toString().includes("fullscreen")){
          try{
          $toggleFull
          catch(e){
      
          }
        }
    """.trimIndent()

    private val enterFullScreen = """
      if(!Array.from(document.querySelector("body").attributes,x => x.name).toString().includes("fullscreen")){
            try{
          $toggleFull
          catch(e){
        
          }        
       }
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
            isPlay = (value == "\"false\"")
            Log.i("PLAYER", isPlay.toString())
        }

    }

    fun exec(action: String) {
        var script = ""
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
        }
        try {
            webView.evaluateJavascript(script) { _ -> Log.d("SCRIPT", "Injected") }
        } catch (e: Error) {
            Log.d("SCRIPT", "Inject Fail")
        }
    }


}