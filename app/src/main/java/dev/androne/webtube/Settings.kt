package dev.androne.webtube

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import dev.androne.webtube.databinding.ActivitySettingsBinding
import android.content.Intent
import android.app.UiModeManager
import android.content.SharedPreferences


class Settings : AppCompatActivity() {

    private var THEME = "THEME"
    var preferences: SharedPreferences = getSharedPreferences(THEME, MODE_PRIVATE)
    var editor = preferences.edit()

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.darkswitch.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                true -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    editor.putString(THEME,"DARK")
                    editor.apply()
                }
                false -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    editor.putString(THEME,"LiGHT")
                    editor.apply()
                }

            }
        }

        binding.darkswitch.isChecked = preferences.getString("COLOR","BLACK").equals("DARK")

    }


}