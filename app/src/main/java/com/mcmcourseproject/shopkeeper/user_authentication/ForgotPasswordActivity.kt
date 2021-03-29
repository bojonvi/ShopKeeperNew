package com.mcmcourseproject.shopkeeper.user_authentication

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.databinding.ForgotpasswordActivityBinding


const val emailSentTextString =
    "We have sent an email message to your email address provided. " +
            "The password reset message will be delivered to your Inbox or Spam Folder shortly or within 24 hours."
const val emailSentTextStringError = "There was an error sending email to the server."

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ForgotpasswordActivityBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForgotpasswordActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth


        binding.forgotPasswordSendEmailButton.setOnClickListener {
            // Show sent confirmation string
            if (binding.forgotPasswordEmailField.text.toString().isEmpty()) {
                binding.forgotPasswordEmailField.error = "Email Address is Empty. Please provide."
                binding.forgotPasswordEmailField.requestFocus()
            } else {
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                auth.sendPasswordResetEmail(binding.forgotPasswordEmailField.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            auth.currentUser
                            binding.emailConfirmText.text = emailSentTextString
                            binding.emailConfirmText.visibility = View.VISIBLE
                            binding.forgotPasswordSendEmailButton.text = "Send Email Again"
                            binding.emailSentAnimation.visibility = View.VISIBLE
                            // Restore to default layout after 15 seconds of Email sent to User
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.forgotPasswordSendEmailButton.text = "Send Email"
                                binding.emailSentAnimation.visibility = View.GONE
                                binding.emailConfirmText.visibility = View.GONE
                            }, 10000)



                        } else if (!task.isSuccessful) {
                            binding.emailConfirmText.text = emailSentTextStringError
                            binding.emailConfirmText.visibility = View.VISIBLE
                            binding.emailFailedSentAnimation.visibility = View.VISIBLE
                            binding.forgotPasswordSendEmailButton.visibility = View.GONE
                            // Restore to default layout after 10 seconds of Email sent to User
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.forgotPasswordSendEmailButton.visibility = View.VISIBLE
                                binding.forgotPasswordSendEmailButton.text = "Send Email"
                                binding.emailFailedSentAnimation.visibility = View.GONE
                                binding.emailConfirmText.visibility = View.GONE
                            }, 8000)

                        }
                    }
            }
        }

        binding.forgotPasswordBackButton.setOnClickListener {
            finish()
        }
    }


    override fun onBackPressed() {
        finish()
    }

}
