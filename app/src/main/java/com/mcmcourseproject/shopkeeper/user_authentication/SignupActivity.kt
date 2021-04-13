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
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.R
import com.mcmcourseproject.shopkeeper.databinding.SignupActivityBinding
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap


class SignupActivity : AppCompatActivity() {
    private lateinit var binding: SignupActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreAuth: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        statusBarColor()
        // Disable Soft-Keyboard when this Activity is launched
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        // Database
        auth = Firebase.auth
        firestoreAuth = FirebaseFirestore.getInstance()

        binding.signUpBackButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.signUpButton.setOnClickListener{
            val email = binding.registerEmailField.text.toString().toLowerCase(Locale.ROOT).trim { it <= ' '}
            val pass = binding.registerPasswordField.text.toString().trim { it <= ' '}
            val confirmPass = binding.registerConfirmPasswordField.text.toString().trim { it <= ' '}

            if (TextUtils.isEmpty(email)) {
                binding.registerEmailField.error = "The email address must not be empty!"
            } else if (TextUtils.isEmpty(pass)) {
                binding.registerPasswordField.error = "The password must not be empty!"
            } else if (TextUtils.isEmpty(confirmPass)) {
                binding.registerConfirmPasswordField.error = "The confirm password must not be empty!"
            } else if (!email.isValidEmail()) {
                binding.registerEmailField.error = "Please input a valid email address!"
            } else {
                signUpFun(email, pass, confirmPass)
            }
        }
    }

    private fun signUpFun(email: String, pass: String, confirmPass: String) {
        if (isInternetAvailable(this)) {
            if (pass != confirmPass) {
                val registerConfirmPasswordField: TextInputEditText =
                    findViewById(R.id.register_confirmPasswordField)
                registerConfirmPasswordField.error =
                    "The confirmation password does not match with the password"
            } else {
                auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { signUpTask ->
                        if (signUpTask.isSuccessful) {
                            val user = auth.currentUser
                            user.sendEmailVerification()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(this, "A verification mail has sent to your email!", Toast.LENGTH_SHORT).show()
                                    }
                                    Firebase.auth.signOut()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                        } else if (!signUpTask.isSuccessful) {
                            try {
                                throw signUpTask.exception!!
                            } catch (e: FirebaseAuthUserCollisionException) {
                                Toast.makeText(
                                        this,
                                        "The account [ $email ] has been already registered in the System.",
                                        Toast.LENGTH_LONG).show()
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                Toast.makeText(
                                        this,
                                        "Weak Password. Input at-least 6 characters.",
                                        Toast.LENGTH_LONG).show()
                            } catch (e: FirebaseAuthEmailException) {
                                Log.e(this.toString(), e.message.toString())
                            }
                        } else {
                            Toast.makeText(
                                    this,
                                    "Account is unable to register. Please try again. \n" + signUpTask.exception,
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            val internetValidationDialogInterface = AlertDialog.Builder(this)
            // set message of alert dialog
            internetValidationDialogInterface.setMessage(
                    "Make sure that WI-FI or Mobile Data is turned on, then try again.\n" +
                            "You cannot Sign Up Account without an Internet Connection.")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton("Retry") { _: DialogInterface, _: Int ->
                        recreate()
                    }
                    // negative button text and action
                    .setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
                        recreate()
                    }
            // create dialog box
            val internetValidationAlert = internetValidationDialogInterface.create()
            // set title for alert dialog box
            internetValidationAlert.setTitle("No Internet Connection")
            internetValidationAlert.setIcon(R.mipmap.ic_launcher)
            // show alert dialog
            internetValidationAlert.show()
        }
    }

    // Save for later use
    private fun saveDatabase(email: String, pass: String, userID: String) {
        Toast.makeText(this, userID, Toast.LENGTH_SHORT).show()
        val user: MutableMap<String, Any> = HashMap()
        user["userEmail"] = email
        user["userPass"] = md5Hash(pass)
        firestoreAuth.collection("usersTable")
            .document(userID)
            .set(user)
            .addOnSuccessListener(OnSuccessListener {
                Log.d(
                    "DocumentError",
                    "DocumentSnapshot added with ID: "
                )
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(
                    "DocumentError",
                    "Error adding document",
                    e
                )
            })

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

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    private fun md5Hash(str: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bigInt = BigInteger(1, md.digest(str.toByteArray(Charsets.UTF_8)))
        return String.format("%032x", bigInt)
    }
}