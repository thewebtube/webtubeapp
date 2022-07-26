package xyz.webtubeapp

import java.io.Serializable


class PluginsAdapater(id: Int, name: String, enabled: Boolean, url: String) : Serializable {

    var id: Int? = id
    var name: String? = name
    var enabled: Boolean? = enabled
    var url: String? = url

    override fun toString(): String {
        return "PluginsAdapater(id=$id, name=$name, enabled=$enabled, url=$url)"
    }


}