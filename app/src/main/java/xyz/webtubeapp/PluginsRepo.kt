package xyz.webtubeapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class PluginsRepo(context: Context) {
    private val plugins_url_api: String =
        "https://raw.githubusercontent.com/thewebtube/plugins/main/plugins.json"
    private var plugins: JSONArray? = null;
    private var dbHelper : PluginDBHelper = PluginDBHelper(context)
    private var db = dbHelper.writableDatabase
    init {

        val queue = Volley.newRequestQueue(context)

// Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, plugins_url_api,
            { response ->
                plugins = JSONArray(response)
                for (i in 0 until plugins!!.length()) {
                    val plugin = plugins!!.getJSONObject(i)
                    val name = plugin.getString("name")
                    val url = plugin.getString("url")
                    val enabled = plugin.getBoolean("enabled")
                    val injectOnUrlChange = plugin.getBoolean("injectOnUrlChange")

                    val c = db.rawQuery(
                        "SELECT * FROM plugins WHERE url = ?",
                        arrayOf(url)
                    )
                    if (c.moveToFirst()) {
                        db.execSQL(
                            "UPDATE plugins SET url = ?, injectOnUrlChange = ? WHERE name = ?",
                            arrayOf(url, injectOnUrlChange, name)
                        )
                    } else {
                        db.execSQL(
                            "INSERT INTO plugins (name, url, enabled, injectOnUrlChange) VALUES (?, ?, ?, ?)",
                            arrayOf(name, url, enabled, injectOnUrlChange)
                        )
                    }

                    // find old plugins and delete them
                    val c2 = db.rawQuery(
                        "SELECT * FROM plugins",
                        null
                    )
                    if (c2.moveToFirst()) {
                        do {
                            val url = c2.getString(c2.getColumnIndex("url"))
                            var found = false
                             for (i in 0 until plugins!!.length()) {
                                val plugin = plugins!!.getJSONObject(i)
                                val url = plugin.getString("url")
                                if (url == url) {
                                    found = true
                                    break
                                }
                            }
                            if (!found) {
                                db.execSQL(
                                    "DELETE FROM plugins WHERE url = ?",
                                    arrayOf(url)
                                )
                                // remove plugin from array plugins
                                for (i in 0 until plugins!!.length()) {
                                    val plugin = plugins!!.getJSONObject(i)
                                    val url = plugin.getString("url")
                                    if (url == url) {
                                        plugins!!.remove(i)
                                        break
                                    }
                                }

                            }
                        } while (c2.moveToNext())
                    }

                }
            },
            {})
        // Add the request to the RequestQueue.
        queue.add(stringRequest)

// Add the request
    }


    fun getPlugins(): JSONArray? {
        return plugins
    }
    fun getPlugin(url: String): JSONObject? {
        for (i in 0 until plugins!!.length()) {
            val plugin = plugins!!.getJSONObject(i)
            val url = plugin.getString("url")
            if (url == url) {
                return plugin
            }
        }
        return null
    }
    fun getAllEnabledPlugins(): JSONArray? {
        val tmp_plugins = JSONArray()
        val c = db.rawQuery(
            "SELECT * FROM plugins WHERE enabled = 1",
            null
        )
        if (c.moveToFirst()) {
            do {
                val tmp_plugin = JSONObject()
                tmp_plugin.put("name", c.getString(c.getColumnIndex("name")))
                tmp_plugin.put("url", c.getString(c.getColumnIndex("url")))
                tmp_plugin.put("enabled", c.getInt(c.getColumnIndex("enabled")))
                tmp_plugin.put("injectOnUrlChange", c.getInt(c.getColumnIndex("injectOnUrlChange")))
                tmp_plugins.put(tmp_plugin)
            } while (c.moveToNext())
        }
        return tmp_plugins
    }
    fun getJsScript() : String {
        val pluginsEnabled = getAllEnabledPlugins()
        var pluginsjs = "["
        for (i in 0 until pluginsEnabled!!.length()) {
            val plugin = pluginsEnabled.getJSONObject(i)

            val url = plugin.getString("url")
            var injectOnUrlChangejs = "false"
            if (plugin.getInt("injectOnUrlChange") == 1) {
                injectOnUrlChangejs = "true"
            }
            var enabledjs = "false"
            if (plugin.getInt("enabled") == 1) {
                enabledjs = "true"
            }

            pluginsjs += "{name: '" + plugin.getString("name") +
                    "', url: '" + url +
                    "', injectOnUrlChange: " +
                    injectOnUrlChangejs +
            ", enabled: " + enabledjs + "},\n"

        }
        pluginsjs += "];"

        var script :String = """
            
            var plugins = $pluginsjs
            
              var cache = {};
                
                function injectAll(mode = "all") {
                  for (var i = 0; i < plugins.length; i++) {
                      if (plugins[i].enabled && (plugins[i].injectOnUrlChange || mode == "all")) {
                          injectScript(plugins[i].url);
                      }
                  }
                }
                
                
                function injectScript(url) {
                  if (cache[url]) {
                      console.log("Injecting " + url + " from cache");
                      eval(cache[url]);
                  } else {
                      var xhr = new XMLHttpRequest();
                      xhr.open("GET", url, true);
                      xhr.onreadystatechange = function () {
                          if (xhr.readyState == 4) {
                              if (xhr.status == 200) {
                              cache[url] = xhr.responseText;
                              eval(xhr.responseText);
                              }
                          }
                      }
                      xhr.send();
                  }
                }
                
                // on page change 
                
                oldurl = window.location.href;
                
                setInterval(function () {
                  if (oldurl != window.location.href) {
                      oldurl = window.location.href;
                      injectAll("page change");
                  }
                }
                  , 1000);
                
                injectAll("all")
              
        """.trimIndent();


        return script
    }
}

