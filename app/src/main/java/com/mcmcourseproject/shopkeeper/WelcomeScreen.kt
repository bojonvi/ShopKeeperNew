package com.mcmcourseproject.shopkeeper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.mcmcourseproject.shopkeeper.databinding.WelcomeScreenActivityBinding
import com.mcmcourseproject.shopkeeper.user_authentication.LoginActivity

class WelcomeScreen : AppCompatActivity() {
    private lateinit var binding: WelcomeScreenActivityBinding
    private lateinit var firestoreAuth: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomeScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        // Initial Data
//        val userID = intent.getStringExtra("userID")!!
//        val userEmail = intent.getStringExtra("userEmail")!!

        // FireStore Database
        firestoreAuth = FirebaseFirestore.getInstance()

//        binding.ownerRoleButton.strokeColor = Color.BLUE
//        binding.employeeRoleButton.strokeColor = Color.BLUE

//         Set the OwnerRole Activity Fragment as Default to show onscreen and apply
//        supportFragmentManager.beginTransaction().apply {
//            replace(R.id.frameLayoutFragment, ownerRole).commit()
//            binding.ownerRoleButton.strokeWidth = 4
//            binding.employeeRoleButton.strokeWidth = 0
//            binding.continueButton.text = getString(R.string.continue_as_owner)
//        }

//        binding.ownerRoleButton.setOnClickListener {
//            supportFragmentManager.beginTransaction().apply {
//                replace(R.id.frameLayoutFragment, ownerRole).commit()
//                binding.ownerRoleButton.strokeWidth = 4
//                binding.employeeRoleButton.strokeWidth = 0
//                binding.continueButton.text = getString(R.string.continue_as_owner)
//            }
//        }
//
//        binding.employeeRoleButton.setOnClickListener {
//            supportFragmentManager.beginTransaction().apply {
//                replace(R.id.frameLayoutFragment, employeeRole).commit()
//                binding.ownerRoleButton.strokeWidth = 0
//                binding.employeeRoleButton.strokeWidth = 4
//                binding.continueButton.text = getString(R.string.continue_as_employee)
//            }
//        }

        binding.continueButton.setOnClickListener {
//                //            val userName = OwnerRoleSelectInformationFragment().ownerFullNameText
//                val docRef = firestoreAuth.collection("usersTable").document(userID)
//                val user = hashMapOf(
//                    "userID" to userID,
//                    "userEmail" to userEmail,
//                    "userTheme" to "default",
//                )
//                docRef.set(user)
//                    .addOnSuccessListener {
//                        val activity = Intent(this, MainMenuOwner::class.java)
//                        activity.putExtra("userID", userID)
//                        activity.putExtra("userEmail", userEmail)
//                        startActivity(activity)
//                        finish()
//                    }

            when (binding.continueButton.text) {
//                "Continue as Owner" -> {
//                    startActivity(Intent(this, RegisterStore::class.java))
//                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
//                }
//                "Continue as Employee" -> {
//                    startActivity(Intent(this, EmployeeRoleSignUpActivity::class.java))
//                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
//                }
                "Continue" -> {
//                    Toast.makeText(this, "Please select a Role", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, RegisterStore::class.java))
                    finish()
                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
                }
            }

        }

        binding.userRoleLogOutButton.setOnClickListener {
            logoutConfirmation()
//            registerNewUser(userID, userEmail)
        }

    } // onCreate() {}


    // Pressing back will warn users that the account will log out
    override fun onBackPressed() {
        logoutConfirmation()
    }

    private fun logoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation to Log Out your Account")
            .setMessage("By confirming, your account will be logged out of the session")
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton("YES") { _, _ ->
                super.onBackPressed()
                Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }.create().show()
    }
}