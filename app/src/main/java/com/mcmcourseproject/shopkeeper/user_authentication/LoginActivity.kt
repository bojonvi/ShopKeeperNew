package com.mcmcourseproject.shopkeeper.user_authentication

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.PermissionSettings
import com.mcmcourseproject.shopkeeper.R
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.SplashScreenActivity
import com.mcmcourseproject.shopkeeper.databinding.LoginActivityBinding
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreAuth: FirebaseFirestore
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        statusBarColor()
        // Disable Soft-Keyboard when this Activity is launched
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        // Firestore Database
        auth = Firebase.auth
        firestoreAuth = FirebaseFirestore.getInstance()

        // SQLite Database
        databaseHelper = DatabaseHelper(this)

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // USE SYSTEM DEFAULT
        )

        binding.loginSignUpButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        binding.loginForgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        binding.loginGuestButton!!.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("Any information entered in Guest Mode will not be saved online.\nYou won't be able to retrieve it once you Logged out or Reinstall the Application.\n\nDo you want to Proceed?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("PROCEED") { _, _ ->
                    // Date & Time Format
                    val dateTime = Calendar.getInstance().time
                    val sdf = SimpleDateFormat("h:mm:ss a MMM d, yyyy", Locale.getDefault())
                    val timeText: String = sdf.format(dateTime)

                    databaseHelper.createUserAccount(
                        "guest",
                        "Guest",
                        "Guest Mode",
                        "guest",
                        "default",
                        "guest",
                        "guest",
                        "guest",
                        "guest",
                        timeText
                    )
                    startActivity(Intent(this, SplashScreenActivity::class.java))
                    finish()
                }.create().show()


        }
        binding.loginButton.setOnClickListener {
            val loginEmailText =
                binding.loginEmailField.text.toString().toLowerCase(Locale.ROOT).trim { it <= ' ' }
            val loginPasswordText = binding.loginPasswordField.text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(loginEmailText) -> {
                    binding.loginEmailField.error = "Please input your Email Address."
                    binding.loginEmailField.requestFocus()
                }
                TextUtils.isEmpty(loginPasswordText) -> {
                    binding.loginPasswordField.error = "Please input your Password."
                    binding.loginPasswordField.requestFocus()
                }
                else -> {
                    if (isInternetAvailable(this)) {
                        loginFun(loginEmailText, loginPasswordText)
                    } else {
                        val internetValidationDialogInterface = AlertDialog.Builder(this)
                        // set message of alert dialog
                        internetValidationDialogInterface.setMessage(
                            "Make sure that WI-FI or Mobile Data is turned on, then try again.\n"
                        )
                            // if the dialog is cancelable
                            .setCancelable(false)
                            // positive button text and action
                            .setPositiveButton("Retry") { _: DialogInterface, _: Int -> recreate() }
                            // negative button text and action
                            .setNegativeButton("Cancel") { _: DialogInterface, _: Int -> recreate() }
                        // create dialog box
                        val internetValidationAlert = internetValidationDialogInterface.create()
                        // set title for alert dialog box
                        internetValidationAlert.setTitle("No Internet Connection")
                        internetValidationAlert.setIcon(R.mipmap.ic_launcher)
                        // show alert dialog
                        internetValidationAlert.show()
                    }
                }
            }
        }

        binding.permissionSettingsButton.setOnClickListener {
            startActivity(Intent(this, PermissionSettings::class.java))
            overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out)
        }

    }

//    private fun checkUserDatabase(userID: String, userEmail: String) {
//        Toast.makeText(
//            this,
//            "You are now logged in\n$userEmail!",
//            Toast.LENGTH_SHORT
//        ).show()
//        val docRef = firestoreAuth.collection("usersTable").document(userID)
//        docRef.get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val activity = Intent(this, MainMenuOwner::class.java)
//                    activity.putExtra("userID", userID)
//                    activity.putExtra("userEmail", userEmail)
//                    startActivity(activity)
//                    finish()
//                } else {
//                    val activity = Intent(this, WelcomeScreen::class.java)
//                    activity.putExtra("userID", userID)
//                    activity.putExtra("userEmail", userEmail)
//                    startActivity(activity)
//                    finish()
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d("Failure", "get failed with ", exception)
//            }
//    }

    private fun loginFun(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.i("TAG", "signInWithEmail:success")
//                    val userEmail = auth.currentUser!!.email
//                    val userID = auth.currentUser!!.uid
//                    checkUserDatabase(userID, userEmail!!)
                    startActivity(Intent(this, SplashScreenActivity::class.java))
                    overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun statusBarColor() {
        // Set Status Bar Color first, in this case it will be the login background ALWAYS
        window.statusBarColor = ContextCompat.getColor(this, R.color.loginBackground)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // If the device is running Marshmallow
            window.statusBarColor = resources.getColor(R.color.loginBackground, this.theme)
        } else // Else if device is running  Lollipop
            ContextCompat.getColor(this, R.color.loginBackground)
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

// Log.e: This is for when bad stuff happens. Use this tag in places like inside a catch statement. You know that an error has occurred and therefore you're logging an error.
//
//Log.w: Use this when you suspect something shady is going on. You may not be completely in full on error mode, but maybe you recovered from some unexpected behavior. Basically, use this to log stuff you didn't expect to happen but isn't necessarily an error. Kind of like a "hey, this happened, and it's weird, we should look into it."
//
//Log.i: Use this to post useful information to the log. For example: that you have successfully connected to a server. Basically use it to report successes.
//
//Log.d: Use this for debugging purposes. If you want to print out a bunch of messages so you can log the exact flow of your program, use this. If you want to keep a log of variable values, use this.
//
//Log.v: Use this when you want to go absolutely nuts with your logging. If for some reason you've decided to log every little thing in a particular part of your app, use the Log.v tag.
//
//And as a bonus...
//
// wtf Stands for "What a Terrible Failure"
//Log.wtf: Use this when stuff goes absolutely, horribly, holy-crap wrong. You know those catch blocks where you're catching errors that you never should get...yeah, if you wanna log them use Log.wtf