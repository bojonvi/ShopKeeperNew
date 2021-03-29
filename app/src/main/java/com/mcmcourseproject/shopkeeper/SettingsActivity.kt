package com.mcmcourseproject.shopkeeper

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mcmcourseproject.shopkeeper.databinding.SettingsActivityBinding


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        binding.versionNumber.text = info.versionName.toString()

        binding.appVersionLinearLayout.setOnClickListener {
            Toast.makeText(this@SettingsActivity, "No updates available.", Toast.LENGTH_SHORT)
                .show()
        }


    }
}