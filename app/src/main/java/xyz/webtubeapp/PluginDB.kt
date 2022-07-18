package xyz.webtubeapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import android.widget.Toast
import com.jakewharton.processphoenix.ProcessPhoenix
import org.json.JSONObject

object PluginDB {
    // plugin contain name, url, enabled, and, injectOnUrlChange
    object PluginEntry : BaseColumns {
        const val TABLE_NAME = "plugins"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_URL = "url"
        const val COLUMN_NAME_ENABLED = "enabled"
        const val COLUMN_NAME_INJECT_ON_URL_CHANGE = "injectOnUrlChange"
    }


}
private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${PluginDB.PluginEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${PluginDB.PluginEntry.COLUMN_NAME_NAME} TEXT," +
            "${PluginDB.PluginEntry.COLUMN_NAME_URL} TEXT," +
            "${PluginDB.PluginEntry.COLUMN_NAME_ENABLED} INTEGER," +
            "${PluginDB.PluginEntry.COLUMN_NAME_INJECT_ON_URL_CHANGE} INTEGER)"
private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${PluginDB.PluginEntry.TABLE_NAME}"


class PluginDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private var context: Context? = context
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        ProcessPhoenix.triggerRebirth(context) // fix first time db creation bug
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun getAllPlugins(): List<Plugin> {
        val db = readableDatabase
        val cursor = db.query(PluginDB.PluginEntry.TABLE_NAME,
            arrayOf(
                BaseColumns._ID,
                PluginDB.PluginEntry.COLUMN_NAME_NAME,
                PluginDB.PluginEntry.COLUMN_NAME_URL,
                PluginDB.PluginEntry.COLUMN_NAME_ENABLED,
                PluginDB.PluginEntry.COLUMN_NAME_INJECT_ON_URL_CHANGE
            ), null, null, null, null, null)
        val plugins = ArrayList<Plugin>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val name = getString(getColumnIndexOrThrow(PluginDB.PluginEntry.COLUMN_NAME_NAME))
                val url = getString(getColumnIndexOrThrow(PluginDB.PluginEntry.COLUMN_NAME_URL))
                val enabled = getInt(getColumnIndexOrThrow(PluginDB.PluginEntry.COLUMN_NAME_ENABLED)) == 1
                val injectOnUrlChange = getInt(getColumnIndexOrThrow(PluginDB.PluginEntry.COLUMN_NAME_INJECT_ON_URL_CHANGE)) == 1
                plugins.add(Plugin(id, name, url, enabled, injectOnUrlChange))
            }
        }
        cursor.close()
        return plugins

    }

    fun updatePlugin(plugin: Plugin?) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(PluginDB.PluginEntry.COLUMN_NAME_NAME, plugin?.name)
        values.put(PluginDB.PluginEntry.COLUMN_NAME_URL, plugin?.url)
        values.put(PluginDB.PluginEntry.COLUMN_NAME_ENABLED, if (plugin?.enabled == true) 1 else 0)
        values.put(PluginDB.PluginEntry.COLUMN_NAME_INJECT_ON_URL_CHANGE, if (plugin?.injectOnUrlChange == true) 1 else 0)
        db.update(PluginDB.PluginEntry.TABLE_NAME, values, BaseColumns._ID + " = ?", arrayOf(plugin?.id.toString()))


    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Plugins.db"
    }
}

class Plugin(
    var id: Long,
    var name: String,
    var url: String,
    var enabled: Boolean,
    var injectOnUrlChange: Boolean
) {
override fun toString(): String {
        return "Plugin(id=$id, name='$name', url='$url', enabled=$enabled, injectOnUrlChange=$injectOnUrlChange)"
    }

}
