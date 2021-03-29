package com.mcmcourseproject.shopkeeper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.GuestSignUpActivityBinding
import java.util.*
import kotlin.system.exitProcess


class GuestSignUp : AppCompatActivity() {
    private val databaseHelper = DatabaseHelper(this)
    private lateinit var binding: GuestSignUpActivityBinding
    private val auth = Firebase.auth
    private var firestoreAuth: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GuestSignUpActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.guestSignUpBackButton.setOnClickListener {
            finish()
        }

        binding.guestSignUpMainButton.setOnClickListener {
            val email = binding.guestSignUpEmailEditText.text.toString().toLowerCase(Locale.ROOT).trim { it <= ' '}
            val pass = binding.guestSignUpPasswordEditText.text.toString().trim { it <= ' '}
            val confirmPass = binding.guestSignUpConfirmEditText.text.toString().trim { it <= ' '}
            val storeName = binding.guestSignUpStoreEditText.text.toString()

            if (TextUtils.isEmpty(email)) {
                binding.guestSignUpEmailEditText.error = "The email address must not be empty!"
            } else if (TextUtils.isEmpty(pass)) {
                binding.guestSignUpPasswordEditText.error = "The password must not be empty!"
            } else if (TextUtils.isEmpty(confirmPass)) {
                binding.guestSignUpConfirmEditText.error = "The confirm password must not be empty!"
            } else if (!email.isValidEmail()) {
                binding.guestSignUpPasswordEditText.error = "Please input a valid email address!"
            } else if (TextUtils.isEmpty(storeName)) {
                binding.guestSignUpStoreEditText.error = "The Store Name must not be empty!"
            } else {
                signUpFun(email, pass, confirmPass, storeName)
            }
        }
    }


    private fun signUpFun(email: String, pass: String, confirmPass: String, storeName: String) {
        if (isInternetAvailable(this)) {
            if (pass != confirmPass) {
                binding.guestSignUpConfirmEditText.error =
                    "The confirmation password does not match with the password"
            } else {
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { signUpTask ->
                        if (signUpTask.isSuccessful) {
                            val refCategory = firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid)
                            val categoryDocument = hashMapOf(
                                "userID" to auth.currentUser!!.uid,
                                "userEmail" to email,
                                "userTheme" to "default",
                                "storeID" to auth.currentUser!!.uid,
                                "storeName" to storeName,
                                "storeAddress" to "guest",
                                "storeEmail" to "guest",
                                "storeSuppliers" to "",
                                "storeTelephone" to "1",
                                "userName" to email
                            )
                            refCategory.set(categoryDocument, SetOptions.merge())
                            databaseHelper.updateUserAccount(
                                email,
                                binding.guestSignUpStoreEditText.text.toString()
                            )
                            AlertDialog.Builder(this)
                                .setTitle("Successful")
                                .setMessage("Account Created Successfully! You might need to restart the app to reload your changes.")
                                .setPositiveButton("OKAY") { _, _ ->
                                    restartApp()
                                }.create().show()
                        } else if (!signUpTask.isSuccessful) {
                            try {
                                throw signUpTask.exception!!
                            } catch (e: FirebaseAuthUserCollisionException) {
                                Toast.makeText(
                                    this,
                                    "The account [ $email ] has been already registered in the System.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                Toast.makeText(
                                    this,
                                    "Weak Password. Input at-least 6 characters.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: FirebaseAuthEmailException) {
                                Log.e(this.toString(), e.message.toString())
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Account is unable to register. Please try again. \n" + signUpTask.exception,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        } else {
            val internetValidationDialogInterface = AlertDialog.Builder(this)
            internetValidationDialogInterface.setMessage(
                "Make sure that WI-FI or Mobile Data is turned on, then try again.\n" +
                        "You cannot Sign Up Account without an Internet Connection."
            )
                .setCancelable(false)
                .setPositiveButton("Retry") { _: DialogInterface, _: Int ->
                    recreate()
                }
                .setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                    recreate()
                }
            val internetValidationAlert = internetValidationDialogInterface.create()
            internetValidationAlert.setTitle("No Internet Connection")
            internetValidationAlert.setIcon(R.mipmap.ic_launcher)
            internetValidationAlert.show()
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        var internetResult = false
        val internetConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = internetConnectivityManager.activeNetwork ?: return false
            val actNw =
                internetConnectivityManager.getNetworkCapabilities(networkCapabilities)?: return false
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

    private fun restartApp() {
        val intent = Intent(applicationContext, SplashScreenActivity::class.java)
        val mPendingIntentId = 1111
        val mPendingIntent = PendingIntent.getActivity(
            applicationContext,
            mPendingIntentId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val mgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
        this.finishAffinity()
        exitProcess(0)
    }

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(
        this
    ).matches()
}