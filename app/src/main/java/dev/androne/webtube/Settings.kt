package dev.androne.webtube

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import dev.androne.webtube.databinding.ActivitySettingsBinding
import android.content.Intent
import android.app.UiModeManager
import android.content.SharedPreferences
import android.content.ComponentName
import android.content.Context

import android.content.pm.PackageManager
import android.view.View
import android.widget.Button
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.Duration
import com.github.javiersantos.appupdater.enums.UpdateFrom


class Settings : AppCompatActivity() {

    private var THEME = "THEME"


    private lateinit var binding: ActivitySettingsBinding

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
        binding.buttonBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                back()
            }
        })

        binding.checkUpdate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                appUpdater.start()
            }
        })



    }

    private fun back(){
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }

}