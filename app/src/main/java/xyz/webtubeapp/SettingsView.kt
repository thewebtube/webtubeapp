package xyz.webtubeapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.content.Intent
import android.content.SharedPreferences

import android.content.pm.PackageManager
import android.net.Uri
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.Duration
import com.github.javiersantos.appupdater.enums.UpdateFrom
import android.content.pm.PackageInfo
import xyz.webtubeapp.databinding.ActivitySettingsBinding


class SettingsView : AppCompatActivity() {

    private var THEME = "THEME"


    private lateinit var binding: ActivitySettingsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val preferences: SharedPreferences = this.getSharedPreferences(THEME, MODE_PRIVATE)
        val editor = preferences.edit()

        val appUpdater = AppUpdater(this)
            .setDisplay(Display.DIALOG)
            //.setDisplay(Display.NOTIFICATION)
            .setDuration(Duration.INDEFINITE)
            .setUpdateFrom(UpdateFrom.JSON)
            .showAppUpdated(true)
            .setUpdateJSON("https://raw.githubusercontent.com/thewebtube/webtube/main/update.json")


        binding.darkswitch.isChecked = preferences.getString("COLOR","DARK").equals("DARK") && AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.backgroundswitch.isChecked = preferences.getBoolean("BACKGROUND_MODE",true)
        binding.darkswitch.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                true -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    editor.putString("COLOR","DARK")
                    editor.apply()
                    //triggerRebirth()
                }
                false -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    editor.putString("COLOR","LIGHT")
                    editor.apply()
                    //triggerRebirth()
                }


            }

        }
        binding.backgroundswitch.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                true -> {
                    editor.putBoolean("BACKGROUND_MODE",true)
                    editor.apply()
                    //triggerRebirth()
                }
                false -> {
                    editor.putBoolean("BACKGROUND_MODE",false)
                    editor.apply()
                    //triggerRebirth()
                }


            }

        }
        binding.buttonBack.setOnClickListener { back() }

        binding.checkUpdate.setOnClickListener { appUpdater.start() }

        binding.openDiscord.setOnClickListener { openDiscord() }

        binding.info.text = "WebTube | ${getVersionInfo()}"

    }

    private fun back(){
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }
    private fun openDiscord(){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/ypdkWPm9yD"))
        this.startActivity(intent)
    }
    fun getVersionInfo(): String? {
        var strVersion = "v"
        val packageInfo: PackageInfo
        try {
            packageInfo = applicationContext
                .packageManager
                .getPackageInfo(
                    applicationContext.packageName,
                    0
                )
            strVersion += packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            strVersion += "Unknown"
        }
        return strVersion
    }
}