package com.mcmcourseproject.shopkeeper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.StoreSettingsActivityBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess


class StoreSettings : AppCompatActivity() {
    private lateinit var binding: StoreSettingsActivityBinding
    private val databaseHelper = DatabaseHelper(this)
    private val auth = Firebase.auth
    private var firestoreAuth: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onStart() {
        super.onStart()
        binding.deleteAccountButton.visibility = View.VISIBLE
        binding.changePassButton.visibility = View.VISIBLE
        if (auth.currentUser != null) {
            val userData = databaseHelper.readUserAccount()
            while (userData.moveToNext()) {
                binding.storeSettingsMessage.text = "Last time saved: ${userData.getString(9)}"
            }
            binding.storeSettingsMainButton.setText(R.string.save_cloud_string)
            binding.storeSettingsMainButton.setOnClickListener {
                // Date & Time Format
                val dateTime = Calendar.getInstance().time
                val sdf = SimpleDateFormat("h:mm:ss a MMM d, yyyy", Locale.getDefault())
                val timeText: String = sdf.format(dateTime)

                AlertDialog.Builder(this)
                    .setTitle("Confirmation to Save")
                    .setMessage("This will overwrite the save data in cloud, do you still want to continue? Make sure to have a stable connection to avoid saved data corruption")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton("YES") { _, _ ->
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

                            val refUser = firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid).collection(
                                "inventoryTable"
                            ).document(itemID)
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

                            val refLog = firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid).collection(
                                "logsTable"
                            ).document(logID)
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

                            val refSales = firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid).collection(
                                "salesTable"
                            ).document(salesID)
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

                        firestoreAuth.collection("usersTable").document(auth.currentUser!!.uid)
                            .get(Source.SERVER)
                            .addOnSuccessListener {
                                val newTimeText = it.getString("lastSaved")
                                if (timeText == newTimeText) {
                                    binding.storeSettingsMessage.text = "Saved: $newTimeText"
                                    Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Unstable internet connection",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }.create().show()
            }
        } else {
            binding.deleteAccountButton.visibility = View.INVISIBLE
            binding.changePassButton.visibility = View.INVISIBLE
            binding.storeSettingsMessage.text = "Please Sign In or Sign Up first!"
            binding.storeSettingsMainButton.setText(R.string.sign_up_account)
            binding.storeSettingsMainButton.setOnClickListener {
                startActivity(Intent(this@StoreSettings, GuestSignUp::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StoreSettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.storeSettingsBackButton.setOnClickListener {
            finish()
        }

        binding.deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmation to Delete Store and Account")
                .setMessage("Deleting this account will result in completely removing your account and store from the system and you won't be able to access your saved data.\n\nDo you confirm?")
                .setNegativeButton("NO", null)
                .setPositiveButton("CONFIRM") { _, _ ->
                    val user = auth.currentUser!!
                    val userID = user.uid
                    user.delete()
                        .addOnSuccessListener { task ->
//                            auth.signOut()
                            this.deleteDatabase("Person.db")
                            firestoreAuth.collection("usersTable").document(userID)
                                .delete()
                                .addOnSuccessListener {
                                    finish()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@StoreSettings,
                                "There was an error. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }.create().show()
        } // BINDING.DELETEACCOUNTANDSTORE BUTTON

        binding.changePassButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }
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
        exitProcess(0)
    }

}