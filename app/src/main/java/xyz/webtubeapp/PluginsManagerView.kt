package xyz.webtubeapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.jakewharton.processphoenix.ProcessPhoenix
import org.json.JSONArray
import xyz.webtubeapp.databinding.ActivityPluginsViewBinding


class PluginsManagerView : AppCompatActivity() {

    private lateinit var binding: ActivityPluginsViewBinding
    val pluginDBHelper = PluginDBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPluginsViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var plugins = pluginDBHelper.getAllPlugins()

        binding.listView.adapter = PluginsAdapter(plugins, this)

        binding.SaveAndExitFab.setOnClickListener {
            ProcessPhoenix.triggerRebirth(this);
        }
    }



    private class PluginsAdapter(val plugins: List<Plugin>, val context: Context) : BaseAdapter() {
        val pluginDBHelper = PluginDBHelper(context)
        private val mInflator: LayoutInflater

        init {
            this.mInflator = LayoutInflater.from(context)
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val view: View?

            if (convertView == null) {
                view = mInflator.inflate(R.layout.plugins_list_item, parent, false)
                view.findViewById<TextView>(R.id.name).text = plugins[position].name
                view.findViewById<SwitchMaterial>(R.id.enabledSwitch).isChecked = plugins[position].enabled
                view.findViewById<SwitchMaterial>(R.id.enabledSwitch).setOnCheckedChangeListener { _, isChecked ->
                    plugins[position].enabled = isChecked
                    pluginDBHelper.updatePlugin(plugins[position])
                }

            } else {
                view = convertView
            }
            return view
        }

        override fun getCount(): Int {
            return plugins.size
        }
        override fun getItem(position: Int): String {
            return plugins[position].toString()
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

    }


}



private operator fun JSONArray?.iterator(): Iterator<PluginDB.PluginEntry> {
    return (0 until this!!.length()).asSequence().map { getJSONObject(it) }.map { PluginDB.PluginEntry }.iterator()
}

