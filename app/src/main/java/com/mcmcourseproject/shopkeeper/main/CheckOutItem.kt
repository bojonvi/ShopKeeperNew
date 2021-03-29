package com.mcmcourseproject.shopkeeper.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.mcmcourseproject.shopkeeper.R
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.CheckoutitemActivityBinding
import java.text.SimpleDateFormat
import java.util.*


class CheckOutItem : AppCompatActivity() {

    private lateinit var binding: CheckoutitemActivityBinding
    private var checkoutList = mutableListOf<String>()
    private val databaseHelper = DatabaseHelper(this)
    private var totalPriceConstant = 0f

    override fun onBackPressed() {
        backFunction()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CheckoutitemActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraScan.setOnClickListener {
            val scanner = IntentIntegrator(this)
            scanner.setOrientationLocked(false)
            scanner.initiateScan()
        }

        binding.checkoutDiscountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                discountFunc()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.addProductBarcode.setOnClickListener {
            addCheckout()
            binding.checkoutItemQuantityTextEdit.setText("1")
        }

        binding.checkoutFinishButton.setOnClickListener {
            if (checkoutList.size > 0) {
                if (discountFunc()) {
                    AlertDialog.Builder(this)
                        .setTitle("Confirm Products Checkout")
                        .setMessage("Do you want to complete this transaction?")
                        .setNegativeButton("NO", null)
                        .setPositiveButton("YES") { _, _ ->
                            finalizeCheckout(binding.checkoutPriceTotalTextView.text.toString())
                            finish()
                        }.create().show()
                } else {
                    binding.checkoutDiscountEditText.error = "up to 100% only"
                }
            } else {
                binding.itemCodeTextField.error = "Add item first!"
            }
        }

        binding.checkoutBackButton.setOnClickListener {
            backFunction()
        }
    }

    private fun discountFunc(): Boolean {
        val discount = try {
            "%.2f".format(binding.checkoutDiscountEditText.text.toString().toFloat()).toFloat()
        } catch (e: Exception) {
            0f
        }
        if (discount in 0.0..100.0) {
            binding.checkoutPriceTotalTextView.text = "%.2f".format(totalPriceConstant - totalPriceConstant * (discount/100f))
            return true
        } else {
            binding.checkoutPriceTotalTextView.text = "%.2f".format(totalPriceConstant)
            return false
        }
    }

    private fun backFunction() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Discard Transaction")
            .setMessage("Do you want to discard this transaction?")
            .setNegativeButton("NO", null)
            .setPositiveButton("YES") { _, _ ->
                finish()
            }.create().show()
    }

    private fun addCheckout() {
        val itemQuantity = binding.checkoutItemQuantityTextEdit.text.toString()
        val itemID = binding.itemCodeTextField.text.toString()
        val itemData = databaseHelper.checkItemCode(itemID)
        if (itemData.count < 1) {
            binding.itemCodeTextField.error = "Item doesn't exist"
        } else if (itemQuantity.toInt() == 0) {
            binding.checkoutItemQuantityTextEdit.error = "Invalid"
        } else {
            var itemQuantityCart = 0

            while (itemData.moveToNext()) {
                var itemIndex = 0
                val itemName = itemData.getString((1))
                val itemPrice = itemData.getString((4))
                val itemQuantityLeft = itemData.getInt((7))

                if (checkoutList.contains(itemID)) {
                    Toast.makeText(this, checkoutList[itemIndex + 1], Toast.LENGTH_SHORT).show()
                    itemIndex = checkoutList.indexOf(itemID)
                    itemQuantityCart += checkoutList[itemIndex + 1].toInt()
                }
                if ((itemQuantity.toFloat() + itemQuantityCart) <= itemQuantityLeft) {
                    if (itemQuantityCart != 0) {
                        checkoutList.removeAt(itemIndex)
                        checkoutList.removeAt(itemIndex)
                    }
                    val itemQuantityTotal = itemQuantityCart + itemQuantity.toInt()
                    checkoutList.add(itemID)
                    checkoutList.add("$itemQuantityTotal")
                    refreshCheckoutList()
                    binding.itemCodeTextField.setText("")
                } else {
                    binding.checkoutItemQuantityTextEdit.error = "${itemQuantityLeft - itemQuantityCart} only left!"
                }
            }
        }
    }

    private fun refreshCheckoutList() {
        binding.checkoutListLinearLayout.removeAllViews()
        binding.checkoutPriceTotalTextView.text = "0.00"

        for (i in 0 until checkoutList.size - 1 step 2) {
            val itemData = databaseHelper.checkItemCode(checkoutList[i])
            var totalPrice = 0f
            while (itemData.moveToNext()) {
                val itemQuantity = checkoutList[i+1].toInt()
                var itemName = itemData.getString((1))
                val itemPrice = itemData.getFloat((4))
                val itemWhole = itemData.getInt((5))
                val itemWholePrice = itemData.getFloat((6))
                if (itemWhole > 1) {
                    totalPrice += (itemQuantity / itemWhole) * itemWholePrice
                    totalPrice += (itemQuantity % itemWhole) * itemPrice
                    if ((itemQuantity / itemWhole) > 0) {
                        itemName += " ($itemWhole pcs for $itemWholePrice)"
                    }
                } else {
                    totalPrice += itemPrice * itemQuantity
                }

                createCheckoutList(itemName, itemQuantity.toString(), "%.2f".format(itemPrice), "%.2f".format(totalPrice))
            }
        }
        totalPriceConstant = binding.checkoutPriceTotalTextView.text.toString().toFloat()
        discountFunc()
    }

    private fun createCheckoutList(itemName: String, itemQuantity: String, itemPrice: String, itemTotal: String) {
        val layoutInflater = LayoutInflater.from(this@CheckOutItem)
        val container = binding.checkoutListLinearLayout
        val rowMain = layoutInflater.inflate(R.layout.checkout_list, container, false)

        val itemNameTextView = rowMain.findViewById<TextView>(R.id.checkoutItemName)
        val itemPriceTextView = rowMain.findViewById<TextView>(R.id.checkoutItemPrice)
        val itemQuantityTextView = rowMain.findViewById<TextView>(R.id.checkoutItemQuantity)
        val itemDeleteButton = rowMain.findViewById<Button>(R.id.checkoutDeleteButton)
        val itemTotalTextView = rowMain.findViewById<TextView>(R.id.checkoutItemTotal)
        val currentTotalPrice = binding.checkoutPriceTotalTextView.text.toString().toFloat()
        val position = container.childCount

        itemNameTextView.text = itemName
        itemPriceTextView.text = "%.2f".format(itemPrice.toFloat())
        itemQuantityTextView.text = itemQuantity

        itemTotalTextView.text = "%.2f".format(itemTotal.toFloat())
        itemDeleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Delete Item")
                .setMessage("Do you want to delete this item from this transaction?")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES") { _, _ ->
                    checkoutList.removeAt(position*2)
                    checkoutList.removeAt(position*2)
                    refreshCheckoutList()
                }.create().show()
        }
        container.addView(rowMain)
        binding.checkoutPriceTotalTextView.text = "%.2f".format(currentTotalPrice + itemTotal.toFloat())
    }

    private fun finalizeCheckout(totalPrice: String) {
        // Sorting List
        val hashmap = mutableMapOf<String, Int>()
        for (i in 0 until checkoutList.size - 1 step 2) {
            if (hashmap.containsKey(checkoutList[i])) {
                hashmap[checkoutList[i]] = hashmap[checkoutList[i]]!! + checkoutList[i+1].toInt()
            } else {
                hashmap[checkoutList[i]] = checkoutList[i+1].toInt()
            }
        }
        val checkingOutList = hashmap.toSortedMap(compareByDescending { it })


        // Date & Time Format
        val dateTime = Calendar.getInstance().time
        var sdf = SimpleDateFormat("h:mm:ss a")
        val timeText: String = sdf.format(dateTime)
        sdf = SimpleDateFormat("MMM d, yyyy")
        val dateText: String = sdf.format(dateTime)
        sdf = SimpleDateFormat("MMM yyyy")
        val monthText: String = sdf.format(dateTime)

        var salesInfo = ""
        var logsInfo = "$timeText^!^Sold: "
        for (key in checkingOutList.keys) {
            val itemData = databaseHelper.checkItemCode(key)
            val itemQuantity = checkingOutList[key]!!
            var itemName = key
            while (itemData.moveToNext()) {
                itemName = itemData.getString((1))
                val itemQuantityLeft = itemData.getInt((7)) - itemQuantity
                salesInfo += "$itemName^!^"
                salesInfo += "$itemQuantity^!^"
                databaseHelper.updateItemQuantity(key, itemQuantityLeft.toFloat())
            }
            logsInfo += "$itemQuantity $itemName; "
        }
        logsInfo += "with ${binding.checkoutDiscountEditText.text.toString()}% discount for a total of $totalPrice PHP.^!^"

        createCheckoutLogs(logsInfo, dateText, monthText)
        createCheckoutSales(salesInfo, dateText, monthText)
    }

    private fun createCheckoutLogs(logsInfo: String, dateText: String, monthText: String) {
        if (databaseHelper.checkLogs(dateText).count < 1) {
            databaseHelper.addLogs(logsInfo, dateText, monthText)
        } else {
            val logsData = databaseHelper.checkLogs(dateText)
            while(logsData.moveToNext()) {
                val logsInfoNew = logsData.getString((1)) + logsInfo
                databaseHelper.updateLogs(logsInfoNew, dateText)
            }
        }
    }

    private fun createCheckoutSales(salesInfo: String, dateText: String, monthText: String) {
        val netProfit = binding.checkoutPriceTotalTextView.text.toString().toFloat()
        if (databaseHelper.checkSales(monthText).count < 1) {
            databaseHelper.addSales(salesInfo, "$netProfit^!^0.00", dateText, monthText, "CLUTTERED")
        } else {
//            Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show()
            val salesData = databaseHelper.checkSales(monthText)
            while(salesData.moveToNext()) {
                val salesInfoNew = salesData.getString((1)) + salesInfo
                val profitInfoData = salesData.getString((2)).split("^!^").toMutableList()
                val profitInfoNew = "%.2f".format(profitInfoData[0].toFloat() + netProfit) + "^!^${profitInfoData[1]}"
                databaseHelper.updateSales(salesInfoNew, profitInfoNew, monthText, "CLUTTERED")
            }
        }
//        if (databaseHelper.checkSales(dateText, "PROFIT").count < 1) {
//            val profitInfo = "$netProfit^!^0.00"
//            databaseHelper.addSales(profitInfo, dateText, "PROFIT")
//        } else {
//            val profitData = databaseHelper.checkSales(dateText, "PROFIT")
//            while(profitData.moveToNext()) {
//                val profitDataInfo = profitData.getString((1)).split("^!^").toMutableList()
//                val profitInfo = "%.2f".format(profitDataInfo[0].toFloat() + netProfit) + "^!^${profitDataInfo[1]}"
//                databaseHelper.updateSales(profitInfo, dateText, "PROFIT")
//            }
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    val itemBarcode = result.getContents()
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG)
                        .show()
                    binding.itemCodeTextField.setText(itemBarcode)
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}