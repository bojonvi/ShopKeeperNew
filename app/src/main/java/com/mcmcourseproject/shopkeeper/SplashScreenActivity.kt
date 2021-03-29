package com.mcmcourseproject.shopkeeper

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.SplashscreenActivityBinding
import com.mcmcourseproject.shopkeeper.main.MainMenuOwner
import com.mcmcourseproject.shopkeeper.user_authentication.LoginActivity
import java.text.SimpleDateFormat
import java.util.*


class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: SplashscreenActivityBinding
    private lateinit var firestoreAuth: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val databaseHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashscreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestoreAuth = FirebaseFirestore.getInstance()

        val imageAnimation = AnimationUtils.loadAnimation(this, R.anim.up_to_down)
        binding.appLogoImageViewSplashScreen.alpha = 0f
        binding.appLogoImageViewSplashScreen.startAnimation(imageAnimation)
        binding.appLogoImageViewSplashScreen.animate().setDuration(1000).alpha(1f).withEndAction {
            val user = auth.currentUser
            if (checkDataBase()) {
                val userData = databaseHelper.readUserAccount()
                var userEmail = ""
                var storeName = ""
                while (userData.moveToNext()) {
                    userEmail = userData.getString((2))
                    storeName = userData.getString((5))
                }
                val activity = Intent(this, MainMenuOwner::class.java)
                activity.putExtra("userEmail", userEmail)
                activity.putExtra("storeName", storeName)
                startActivity(activity)
                finish()
            } else if (user != null) {
                checkUserDatabase(user.uid, user.email!!)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            val dbPath = this@SplashScreenActivity.getDatabasePath("Person.db").absolutePath
            checkDB = SQLiteDatabase.openDatabase(
                dbPath, null,
                SQLiteDatabase.OPEN_READONLY
            )
            checkDB.close()
        } catch (e: Exception) {
            Log.w("ACCOUNT:", "Account doesn't exist.")
        }
        return checkDB != null
    }

    private fun checkUserDatabase(userID: String, userEmail: String) {
        val logInConfirmation = Toast.makeText(
            this@SplashScreenActivity,
            "You are now logged in\n$userEmail!",
            Toast.LENGTH_SHORT
        )
        logInConfirmation.setGravity(Gravity.TOP, 0, 0)
        logInConfirmation.show()
        firestoreAuth.collection("usersTable").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Date & Time Format
                    val dateTime = Calendar.getInstance().time
                    val sdf = SimpleDateFormat("h:mm:ss a MMM d, yyyy", Locale.getDefault())
                    val timeText: String = sdf.format(dateTime)

                    val userID = auth.currentUser!!.uid
                    val userEmail = auth.currentUser!!.email!!
                    val userName = document.getString("userName")!!
                    val userTheme = document.getString("userTheme")!!
                    val storeID = document.getString("storeID")!!
                    val storeName = document.getString("storeName")!!
                    val storeEmail = document.getString("storeEmail")!!
                    val storeAddress = document.getString("storeAddress")!!
                    val storeTelephone = document.getString("storeTelephone")!!
                    document.getString("storeCategories")!!.split("^!^").toMutableList().forEach {
                        if (it.isNotEmpty()) {
                            databaseHelper.addCategory(it)
                        }
                    }
                    val storeSuppliers = document.getString("storeSuppliers")!!.split("^!^").toMutableList().forEach {

                    }

                    firestoreAuth.collection("usersTable")
                        .document(userID)
                        .collection("inventoryTable")
                        .get()
                        .addOnSuccessListener { itemDocument ->
                            for (itemData in itemDocument) {
                                databaseHelper.addItem(itemData.data.getValue("itemID").toString(),
                                    itemData.data.getValue("itemName").toString(),
                                    itemData.data.getValue("itemCategory").toString(),
                                    itemData.data.getValue("itemCost").toString().toFloat(),
                                    itemData.data.getValue("itemPrice").toString().toFloat(),
                                    itemData.data.getValue("itemWhole").toString().toFloat(),
                                    itemData.data.getValue("itemWholePrice").toString().toFloat(),
                                    itemData.data.getValue("itemQuantity").toString().toFloat(),
                                    itemData.data.getValue("itemSupplier").toString()
                                )
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }

                    firestoreAuth.collection("usersTable")
                        .document(userID)
                        .collection("logsTable")
                        .get()
                        .addOnSuccessListener { itemDocument ->
                            for (itemData in itemDocument) {
                                databaseHelper.addLogs(itemData.data.getValue("logInfo").toString(),
                                    itemData.data.getValue("logDate").toString(),
                                    itemData.data.getValue("logMonth").toString())
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }

                    firestoreAuth.collection("usersTable")
                        .document(userID)
                        .collection("salesTable")
                        .get()
                        .addOnSuccessListener { itemDocument ->
                            for (itemData in itemDocument) {
                                databaseHelper.addSales(itemData.data.getValue("salesInfo").toString(),
                                    itemData.data.getValue("salesProfit").toString(),
                                    itemData.data.getValue("salesDate").toString(),
                                    itemData.data.getValue("salesMonth").toString(),
                                    itemData.data.getValue("salesStatus").toString())
                            }

                            databaseHelper.createUserAccount(userID, userName, userEmail, storeID, userTheme, storeName, storeAddress, storeEmail, storeTelephone, timeText)
                            val activity = Intent(this, MainMenuOwner::class.java)
                            activity.putExtra("userEmail", userEmail)
                            activity.putExtra("storeName", storeName)
                            startActivity(activity)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                } else { // Creates new user personal database
                    val activity = Intent(this, WelcomeScreen::class.java)
                    activity.putExtra("userID", userID)
                    activity.putExtra("userEmail", userEmail)
                    startActivity(activity)
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Cannot Connect to Server", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }

    private fun updateLocalDatabase(document: DocumentSnapshot): Boolean {
        return true
    }

}
