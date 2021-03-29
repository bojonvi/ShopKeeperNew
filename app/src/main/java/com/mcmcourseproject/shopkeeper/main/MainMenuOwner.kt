package com.mcmcourseproject.shopkeeper.main

import android.annotation.SuppressLint
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.*
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.MainmenuownerActivityBinding
import com.mcmcourseproject.shopkeeper.user_authentication.LoginActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MainMenuOwner : AppCompatActivity() {
    private lateinit var binding: MainmenuownerActivityBinding
    private val databaseHelper = DatabaseHelper(this)
    private var firestoreAuth: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private var clickCounter = ""
    private var userEmail: String? = ""
    private var storeName: String? = ""

    override fun onStart() {
        super.onStart()
        var checkDB: SQLiteDatabase? = null
        try {
            val dbPath = this@MainMenuOwner.getDatabasePath("Person.db").absolutePath
            checkDB = SQLiteDatabase.openDatabase(
                dbPath, null,
                SQLiteDatabase.OPEN_READONLY
            )
            checkDB.close()
        } catch (e: Exception) {
            Log.w("ACCOUNT:", "Account doesn't exist.")
        }
        if (checkDB == null) {
            Firebase.auth.signOut() // For app regular email Sign-ins
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainmenuownerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent Variables
        userEmail = intent.getStringExtra("userEmail")
        storeName = intent.getStringExtra("storeName")
//
//        binding.storeName.text = storeNameGet

        binding.userEmailAddressOwner.text = "$userEmail (Logged In)"
        binding.storeName.text = "$storeName".toUpperCase(Locale.getDefault())

        binding.manageInventoryCV.setOnClickListener {
            startActivity(Intent(this@MainMenuOwner, Inventory::class.java))
        }
        binding.manageSalesCV.setOnClickListener {
            startActivity(Intent(this@MainMenuOwner, ManageSales::class.java))
        }
        binding.checkOutProductButton.setOnClickListener {
            startActivity(Intent(this@MainMenuOwner, CheckOutItem::class.java))
        }
        binding.supplierButton.setOnClickListener {
            startActivity(Intent(this@MainMenuOwner, Suppliers::class.java))
        }

        binding.checkLogsButton.setOnClickListener {
            startActivity(Intent(this@MainMenuOwner, CheckLogs::class.java))
        }
        binding.storeSettingsButton.setOnClickListener {
            startActivity(Intent(this@MainMenuOwner, StoreSettings::class.java))
        }
        binding.appSettingsOwner.setOnClickListener {
            val activity = Intent(this@MainMenuOwner, SettingsActivity::class.java)
            startActivity(activity)
        }

        binding.logOutOwnerButton.setOnClickListener {
            if (auth.currentUser != null) {
                // Date & Time Format
                val dateTime = Calendar.getInstance().time
                val sdf = SimpleDateFormat("h:mm:ss a MMM d, yyyy", Locale.getDefault())
                val timeText: String = sdf.format(dateTime)
                logoutConfirmation(timeText)
            } else {
                AlertDialog.Builder(this)
                    .setTitle("WARNING!")
                    .setMessage("Your data will be deleted if you logout, please head to Store Settings first to save your data.")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton("LOGOUT") { _, _ ->
                        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                        this.deleteDatabase("Person.db")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }.create().show()
            }

        }

        val calendar: Calendar = Calendar.getInstance()
        val currentDate: String = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.time)
        binding.dateTextView.text = currentDate

    } // fun onCreate() {}

    // Press back again to EXIT APPLICATION
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation to Close Application")
            .setMessage("Exit the Application?")
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton("YES") { _, _ ->
                super.onBackPressed()
                quit()
            }.create().show()
    }

    private fun quit() {
        val start = Intent(Intent.ACTION_MAIN)
        start.addCategory(Intent.CATEGORY_HOME)
        start.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        start.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(start)
    }

    private fun logoutConfirmation(timeText: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirmation to Log Out your Account")
            .setMessage("By confirming, your account will be logged out of the session. Please make sure that you've a stable internet connection to logout successfully")
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton("YES") { _, _ ->
                saveOnline(timeText)
                firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid)
                    .get(Source.SERVER)
                    .addOnSuccessListener {
                        super.onBackPressed()
                        if (timeText == it.getString("lastSaved")) {
                            Firebase.auth.signOut() // For app regular email Sign-ins
                            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                            this.deleteDatabase("Person.db")
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Unstable internet connection", Toast.LENGTH_SHORT).show()
                    }
            }.create().show()
    }

    private fun saveOnline(timeText: String) {
        val itemData = databaseHelper.readInventory("ITEM_NAME", "ALL")
        while (itemData.moveToNext()) {
            val itemID = itemData.getString((0))
            val itemName = itemData.getString((1))
            val itemCategory = itemData.getString((2))
            val itemCost = itemData.getString((3))
            val itemPrice = itemData.getString((4))
            val itemWhole = itemData.getString((5))
            val itemWholePrice = itemData.getString((6))
            val itemQuantity = itemData.getString((7))
            val itemSupplier = itemData.getString((8))

            val refUser = firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid).collection("inventoryTable").document(itemID)
            val user = hashMapOf(
                "itemID" to itemID,
                "itemName" to itemName,
                "itemCategory" to itemCategory,
                "itemCost" to itemCost,
                "itemPrice" to itemPrice,
                "itemWhole" to itemWhole,
                "itemWholePrice" to itemWholePrice,
                "itemQuantity" to itemQuantity,
                "itemSupplier" to itemSupplier
            )
            refUser.set(user)
        }

        val logData = databaseHelper.readLogs()
        while (logData.moveToNext()) {
            val logID = logData.getString((0))
            val logInfo = logData.getString((1))
            val logDate = logData.getString((2))
            val logMonth = logData.getString((3))

            val refLog = firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid).collection("logsTable").document(logID)
            val logDocument = hashMapOf(
                "logID" to logID,
                "logInfo" to logInfo,
                "logDate" to logDate,
                "logMonth" to logMonth
            )
            refLog.set(logDocument)
        }

        val salesData = databaseHelper.readSales()
        while (salesData.moveToNext()) {
            val salesID = salesData.getString((0))
            val salesInfo = salesData.getString((1))
            val salesProfit = salesData.getString((2))
            val salesDate = salesData.getString((3))
            val salesMonth = salesData.getString((4))
            val salesStatus = salesData.getString((5))

            val refSales = firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid).collection("salesTable").document(salesID)
            val salesDocument = hashMapOf(
                "salesID" to salesID,
                "salesInfo" to salesInfo,
                "salesProfit" to salesProfit,
                "salesDate" to salesDate,
                "salesMonth" to salesMonth,
                "salesStatus" to salesStatus
            )
            refSales.set(salesDocument)
        }

        val categoryData = databaseHelper.readCategory("CATEGORY_NAME")
        var categoryDocumentValue = ""
        while (categoryData.moveToNext()) {
            val categoryName = categoryData.getString((1))
            categoryDocumentValue += "$categoryName^!^"
        }
        val refCategory = firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid)
        val categoryDocument = hashMapOf(
            "storeCategories" to categoryDocumentValue,
            "lastSaved" to timeText
        )
        refCategory.set(categoryDocument, SetOptions.merge())

    }
}