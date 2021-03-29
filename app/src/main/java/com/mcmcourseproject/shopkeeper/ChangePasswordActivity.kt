package com.mcmcourseproject.shopkeeper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.databinding.ChangePasswordActivityBinding

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ChangePasswordActivityBinding
    private var firestoreAuth: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChangePasswordActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        binding.emailAddressText.text = user!!.email

        binding.changePassBackButton.setOnClickListener {
            finish()
        }

        binding.changePassTriggerButton.setOnClickListener {
            val pass = binding.newPasswordFieldInputEditText.text.toString().trim { it <= ' ' }
            val confirmPass =
                binding.confirmPasswordFieldInputEditText.text.toString().trim { it <= ' ' }
            val oldPass = binding.currentPasswordFieldEditText.text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(pass) -> {
                    binding.newPasswordFieldInputEditText.error = "The password must not be empty!"
                }
                TextUtils.isEmpty(confirmPass) -> {
                    binding.confirmPasswordFieldInputEditText.error =
                        "The confirmation password must not be empty!"
                }
                TextUtils.isEmpty(oldPass) -> {
                    binding.currentPasswordFieldEditText.error =
                        "The current password must not be empty!"
                }
                pass != confirmPass -> {
                    binding.confirmPasswordFieldInputEditText.error =
                        "Incorrect Confirmation Password"
                }
                else -> {
                    val credential = EmailAuthProvider
                        .getCredential(user.email!!, oldPass)

                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            user.updatePassword(pass)
                                .addOnSuccessListener {
                                    AlertDialog.Builder(this)
                                        .setCancelable(false)
                                        .setTitle("Successful")
                                        .setMessage("Your password has been successfully changed!")
                                        .setPositiveButton("CONFIRM") { _, _ ->
                                            finish()
                                        }.create().show()
                                }
                                .addOnFailureListener { exception ->
                                    try {
                                        throw exception
                                    } catch (e: FirebaseAuthWeakPasswordException) {
                                        binding.newPasswordFieldInputEditText.error =
                                            "Weak Password. Input at-least 6 characters."
                                    }
                                }
                        }
                        .addOnFailureListener { exception ->
                            try {
                                throw exception
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                binding.currentPasswordFieldEditText.error =
                                    "Incorrect Old Password"
                            }
                        }

                    //                if (isInternetAvailable(this)) {
                    //                    if (pass != confirmPass) {
                    //                        binding.registerConfirmPasswordField.error =
                    //                            "The confirmation password does not match with the password"
                    //                    } else {
                    //                        val user = auth.currentUser
                    //                        if (user != null && user.email != null) {
                    //                            val credential = EmailAuthProvider
                    //                                .getCredential(user.email!!, pass)
                    //
                    //// Prompt the user to re-provide their sign-in credentials
                    //                            user.reauthenticate(credential)
                    //                                .addOnCompleteListener {
                    //                                    if (it.isSuccessful) {
                    //                                        user.updatePassword(pass)
                    //                                            .addOnCompleteListener { task ->
                    //                                                if (task.isSuccessful) {
                    //                                                    auth.signOut()
                    //                                                    startActivity(
                    //                                                        Intent(
                    //                                                            this,
                    //                                                            LoginActivity::class.java
                    //                                                        )
                    //                                                    )
                    //                                                    finish()
                    //                                                }
                    //                                            }
                    //                                        Toast.makeText(
                    //                                            this,
                    //                                            "Changed Password successfully.\nPlease authenticate again.",
                    //                                            Toast.LENGTH_SHORT
                    //                                        ).show()
                    //                                        startActivity(Intent(this, LoginActivity::class.java))
                    //                                        finish()
                    //                                    }
                    //                                }
                    //
                    //                        } else {
                    //                            Toast.makeText(
                    //                                this,
                    //                                "There was an error.\nPlease try again",
                    //                                Toast.LENGTH_SHORT
                    //                            ).show()
                    //                        }
                    //
                    //                    }
                    //                } else {
                    //                    val internetValidationDialogInterface = AlertDialog.Builder(this)
                    //                    // set message of alert dialog
                    //                    internetValidationDialogInterface.setMessage(
                    //                        "Make sure that WI-FI or Mobile Data is turned on, then try again.\n" +
                    //                                "You cannot Sign Up Account without an Internet Connection."
                    //                    )
                    //                        // if the dialog is cancelable
                    //                        .setCancelable(false)
                    //                        // positive button text and action
                    //                        .setPositiveButton("Retry") { _: DialogInterface, _: Int ->
                    //                            recreate()
                    //                        }
                    //                        // negative button text and action
                    //                        .setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                    //                            recreate()
                    //                        }
                    //                    // create dialog box
                    //                    val internetValidationAlert = internetValidationDialogInterface.create()
                    //                    // set title for alert dialog box
                    //                    internetValidationAlert.setTitle("No Internet Connection")
                    //                    internetValidationAlert.setIcon(R.mipmap.ic_launcher)
                    //                    // show alert dialog
                    //                    internetValidationAlert.show()
                    //                }
                }
            }

        }
    }


    private fun isInternetAvailable(context: Context): Boolean {
        var internetResult = false
        val internetConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = internetConnectivityManager.activeNetwork ?: return false
            val actNw =
                internetConnectivityManager.getNetworkCapabilities(networkCapabilities)
                    ?: return false
            internetResult = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else { // Else code is for devices running Lollipop and Marshmallow where IT IS NOT yet depreciated
            internetConnectivityManager.run {
                internetConnectivityManager.activeNetworkInfo?.run {
                    internetResult = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }
        return internetResult
    } // check if internet is available on the device.

}