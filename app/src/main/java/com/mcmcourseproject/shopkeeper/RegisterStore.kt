package com.mcmcourseproject.shopkeeper

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.databinding.RegisterStoreActivityBinding
import java.util.*

class RegisterStore : AppCompatActivity() {
    private lateinit var binding: RegisterStoreActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreAuth: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterStoreActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase
        firestoreAuth = FirebaseFirestore.getInstance()
        auth = Firebase.auth

//        binding.backButton.setOnClickListener {
//            startActivity(Intent(this, WelcomeScreen::class.java))
//            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
//            finish()
//        }

        binding.confirmButton.setOnClickListener {
            val email =
                binding.emailAddressTextField.text.toString().toLowerCase(Locale.ROOT)
                    .trim { it <= ' ' }
            val storeName = binding.storeNameTextField.text.toString().toUpperCase()
            val storeAddress = binding.storeAddressTextField.text.toString()
            val storeTelephoneNumber =
                binding.telephoneNumberTextField.text.toString().toLowerCase(Locale.ROOT)
                    .trim { it <= ' ' }
            val ownerFullName = binding.ownerFullNameTextField.text.toString()

            if (TextUtils.isEmpty(storeName)) {
                binding.storeNameTextField.error = "Store Name is blank."
            } else if (TextUtils.isEmpty(ownerFullName)) {
                binding.ownerFullNameTextField.error = "The Name field is required."
            } else { // If all validations met, then Sign Up the Employee
                AlertDialog.Builder(this)
                    .setTitle("Confirm registration")
                    .setMessage("Do you want to confirm your registration as Owner?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("YES") { _, _ ->
                        // GET ALL VALUES, STORE TO DATABASE, AND PROCEED TO OWNER_MAINMENU ACTIVITY
//                        // Checks the availability of the store name
//                        firestoreAuth.collection("storesTable")
//                            .whereEqualTo("storeName", storeName)
//                            .get()
//                            .addOnSuccessListener { documents ->
//                                if (documents.size() == 0) {
//                                    createDatabaseEntry(
//                                        email,
//                                        storeName,
//                                        storeAddress,
//                                        storeTelephoneNumber,
//                                        ownerFullName
//                                    )
//                                } else {
//                                    binding.storeNameTextField.error =
//                                        "Store name has already been taken!"
//                                }
//                            }
                        createDatabaseEntry(
                            email,
                            storeName,
                            storeAddress,
                            storeTelephoneNumber,
                            ownerFullName
                        )
                    }.create().show()
            }
        }
    }

    fun createDatabaseEntry(
        email: String, storeName: String, storeAddress: String,
        storeTelephoneNumber: String, ownerFullName: String
    ) {
        // Creates unique store code
//        val storeCode = Random.nextInt(100000, 999999).toString()
        val userID = auth.currentUser!!.uid
        val refUser = firestoreAuth.collection("usersTable").document(userID)
        val user = hashMapOf(
            "userID" to auth.currentUser!!.uid,
            "userName" to ownerFullName,
            "userEmail" to auth.currentUser!!.email,
            "userTheme" to "default",
            "storeID" to userID,
            "storeName" to storeName,
            "storeEmail" to email,
            "storeAddress" to storeAddress,
            "storeTelephone" to storeTelephoneNumber,
            "storeCategories" to "",
            "storeSuppliers" to ""
        )
        refUser.set(user)
        startActivity(Intent(this, SplashScreenActivity::class.java))
        overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out)
        finish()

//        val refStore = firestoreAuth.collection("inventoryTable").document(userID)
//        refStore.get().addOnSuccessListener { code ->
//            if (code.exists()){
//                createDatabaseEntry(
//                    email,
//                    storeName,
//                    storeAddress,
//                    storeTelephoneNumber,
//                    ownerFullName
//                )
//            } else if (!code.exists()){
                // Register User
//                val refUser = firestoreAuth.collection("usersTable").document(userID)
//                val user = hashMapOf(
//                    "userID" to auth.currentUser!!.uid,
//                    "userName" to ownerFullName,
//                    "userEmail" to auth.currentUser!!.email,
//                    "userTheme" to "default",
//                    "storeID" to userID,
//                    "storeName" to storeName,
//                    "storeEmail" to email,
//                    "storeAddress" to storeAddress,
//                    "storeTelephone" to storeTelephoneNumber,
//                    "storeCategories" to "",
//                    "storeSuppliers" to ""
//                )
//                refUser.set(user)
//                startActivity(Intent(this, SplashScreenActivity::class.java))
//                overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out)
//                finish()
//            }
//        }
    }

    override fun onBackPressed() {
        val activity = Intent(this, WelcomeScreen::class.java)
        activity.putExtra("userID", auth.currentUser!!.uid)
        activity.putExtra("userEmail", auth.currentUser!!.email)
        startActivity(activity)
        finish()
    }

}