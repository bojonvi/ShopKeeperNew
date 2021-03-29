package com.mcmcourseproject.shopkeeper


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mcmcourseproject.shopkeeper.databinding.PermissionSettingsActivityBinding
import com.mcmcourseproject.shopkeeper.user_authentication.LoginActivity

class PermissionSettings : AppCompatActivity() {
    private lateinit var binding: PermissionSettingsActivityBinding
    private val requestCameraPermission: Int = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PermissionSettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out)
            finish()
        }


        /** IF THE DEVICE IS ANDROID LOLLIPOP, MAKE A TEXTVIEW THAT WILL VISIBLE ON THEIR SCREEN, THAT THEY MUST
        SET PERMISSION MANUALLY, AND THE "REQEST & GRANT PERMISSION" BUTTON WILL BE DISABLED
        BECAUSE PERMISSION DIALOGS WERE NOT SUPPORTED AT THIS O.S.
        PERMISSION DIALOGS ARE SUPPORTED STARTING FROM ANDROID MARSHMALLOW.

        if (device_sdk(android version) == lollipop) {
        binding.setPermissionNowButton.enabled = false OR
        binding.setPermissionNowButton.visibility = View.GONE

        make it disappear or disable button only..???

         */


        binding.setPermissionNowButton.setOnClickListener {
                checkForPermission(Manifest.permission.CAMERA, "camera", requestCameraPermission)
        }

        binding.setManualPermissionButton.setOnClickListener {
            val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            appSettingsIntent.data = uri
            startActivity(appSettingsIntent)
        }

    }

    private fun checkForPermission(permission: String, name: String, requestCode: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            when {
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(
                        applicationContext,
                        "$name permission granted.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.grantedTextInto.visibility = View.VISIBLE

                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(
                    permission,
                    name,
                    requestCode
                )
                // Check for permission, if its not been granted, its going to ask for the permission
                else -> ActivityCompat.requestPermissions(
                    this@PermissionSettings, arrayOf(
                        permission
                    ), requestCode
                )

            }
        }
        else {
            binding.errorInfo.visibility = View.VISIBLE
            binding.setPermissionNowButton.isEnabled = false
            binding.setPermissionNowButton.setTextColor(Color.GRAY)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        when (requestCode) {
            requestCameraPermission -> innerCheck("camera")
        }

    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("We need permission to access your $name inorder for ShopKeeper to work properly. Camera will serve as a Barcode Scanner later on.")
            setTitle("Permission required")
            setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    this@PermissionSettings,
                    arrayOf(permission),
                    requestCode
                )
            }
        }
        val dialog = builder.create()
        dialog.show()

    }
}