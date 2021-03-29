package com.mcmcourseproject.shopkeeper.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.mcmcourseproject.shopkeeper.R
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.AdditemActivityBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class AddItem : AppCompatActivity() {

    private lateinit var firestoreAuth: FirebaseFirestore
    private lateinit var binding: AdditemActivityBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var quantity: String
    private lateinit var userID: String
    private lateinit var storeID: String
    private lateinit var userEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdditemActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.doneButton.setText(R.string.add_item)
        // Database Instance
        databaseHelper = DatabaseHelper(this)
        firestoreAuth = FirebaseFirestore.getInstance()

        // Intent Variables
        val itemID = intent.getStringExtra("itemID")!!
        if (itemID != "new") readItemData(itemID)


//        verifyBarcodeInput()

//        binding.itemCodeTextField.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(s: Editable?) {
//                verifyBarcodeInput()
//            }
//        })

        binding.inventoryBackButton.setOnClickListener {
            finish()
        }

        // Get the string array
        val itemCategory = ArrayList<String>() // Creating an empty ArrayList for itemCategory
        itemCategory.add("ALL")
        val category = databaseHelper.readCategory("CATEGORY_NAME")
        while (category.moveToNext()) {
            itemCategory.add(category.getString((1)))
        }

        // Create the adapter and set it to the AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemCategory)
        // To add item dynamically,..
        binding.categoryInputTextField.setAdapter(adapter)


        binding.unitCostTextField.filters = (arrayOf<InputFilter>(DecimalDigitsInputFilter(6, 2)))
        binding.wholesaleContentQuantityTextField.filters = (arrayOf<InputFilter>(
            DecimalDigitsInputFilter(
                3,
                0
            )
        ))
        binding.markupPercentageRetailTextField.filters =
            (arrayOf<InputFilter>(DecimalDigitsInputFilter(3, 2)))
        binding.markupPercentageWholesaleTextField.filters =
            (arrayOf<InputFilter>(DecimalDigitsInputFilter(3, 2)))
        binding.retailPriceTextField.filters =
            (arrayOf<InputFilter>(DecimalDigitsInputFilter(9, 2)))
        binding.wholesalePriceTextField.filters =
            (arrayOf<InputFilter>(DecimalDigitsInputFilter(11, 2)))


        binding.barcodeEmployeeCopyText.setOnClickListener {
            barcodeCopyTextToClipboard()
        }

        binding.cameraScan.setOnClickListener {
            val scanner = IntentIntegrator(this)
            scanner.setOrientationLocked(false)
            scanner.initiateScan()
        }

        // Action for Done Button
        binding.doneButton.setOnClickListener {
            if (checkInput("full"))
                AlertDialog.Builder(this)
                    .setTitle("Confirm Inventory")
                    .setMessage("Do you want to place this item to your Inventory?")
                    .setNegativeButton("NO", null)
                    .setPositiveButton("YES") { _, _ ->
                        if (saveItem(itemID)){
                            finish()
                        } else {
                            binding.itemCodeTextField.error = "Barcode already been used!"
                            Toast.makeText(this, "Barcode already been used!", Toast.LENGTH_SHORT).show()
                        }
                    }.create().show()
        }

        binding.discardButton.setOnClickListener {
            discardBackPressed()
        }

        // Text Change Listener
        binding.markupPercentageRetailTextField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.markupPercentageRetailTextField.hasFocus()) {
//                    if (binding.retailPriceTextField.text.toString().isEmpty()) {
//                        binding.retailPriceTextField.setText("0")
//                    }
                    currentTextChange("retailMarkup")
                } else if (!checkInput() && !binding.markupPercentageRetailTextField.hasFocus()) {
                    binding.markupPercentageRetailTextField.setText("0")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.unitCostTextField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (checkInput() && binding.unitCostTextField.hasFocus()) {
                    currentTextChange("unitCost")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.retailPriceTextField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (checkInput() && binding.retailPriceTextField.hasFocus()) {
                    currentTextChange("retailPrice")
                } else if (!checkInput() && !binding.retailPriceTextField.hasFocus()) {
                    binding.retailPriceTextField.setText("0")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.markupPercentageWholesaleTextField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.markupPercentageWholesaleTextField.hasFocus()) {
                    currentTextChange("wholeMarkup")
                } else if (!checkInput() && !binding.markupPercentageWholesaleTextField.hasFocus()) {
                    binding.markupPercentageWholesaleTextField.setText("0")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.wholesalePriceTextField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (checkInput() && binding.wholesalePriceTextField.hasFocus()) {
                    currentTextChange("wholePrice")
                } else if (!checkInput() && !binding.wholesalePriceTextField.hasFocus()) {
                    binding.wholesalePriceTextField.setText("0")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.wholesaleContentQuantityTextField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (checkInput() && binding.wholesaleContentQuantityTextField.hasFocus()) {
                    currentTextChange("content")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun currentTextChange(currentTextEdit: String) {
        try {
            val unitCost = binding.unitCostTextField.text.toString().toFloat()
            val retailPrice = binding.retailPriceTextField.text.toString().toFloat()
            val wholePrice = binding.wholesalePriceTextField.text.toString().toFloat()
            val content = binding.wholesaleContentQuantityTextField.text.toString().toFloat()

            if (currentTextEdit == "unitCost") {
                val retailMarkup = binding.markupPercentageRetailTextField.text.toString().toFloat()
                val wholeMarkup =
                    binding.markupPercentageWholesaleTextField.text.toString().toFloat()
                binding.retailPriceTextField.setText("%.2f".format(price(unitCost, retailMarkup)))
                binding.wholesalePriceTextField.setText(
                    "%.2f".format(
                        price(
                            unitCost,
                            wholeMarkup
                        ) * content
                    )
                )
            } else if (currentTextEdit == "retailMarkup") {
                val retailMarkup = binding.markupPercentageRetailTextField.text.toString().toFloat()
                binding.retailPriceTextField.setText("%.2f".format(price(unitCost, retailMarkup)))
            } else if (currentTextEdit == "retailPrice") {
                val percent = markup(unitCost, retailPrice)
                if (percent < 0) {
                    binding.retailPriceTextField.error = "Retail Price is lower than the Unit Cost!"
                } else if (percent > 999) {
                    binding.retailPriceTextField.error = "Retail Price is too high!"
                }
                binding.markupPercentageRetailTextField.setText(
                    "%.2f".format(percent)
                )
            } else if (currentTextEdit == "wholeMarkup") {
                val wholeMarkup =
                binding.markupPercentageWholesaleTextField.text.toString().toFloat()
                binding.wholesalePriceTextField.setText(
                    "%.2f".format(
                        price(
                            unitCost,
                            wholeMarkup
                        ) * content
                    )
                )
            } else if (currentTextEdit == "wholePrice") {
                val percent = markup(unitCost * content, wholePrice)
                if (percent < 0) {
                    binding.wholesalePriceTextField.error =
                        "Wholesale Price is lower than the Unit Cost!"
                } else if (percent > 999) {
                    binding.retailPriceTextField.error = "Wholesale Price is too high!"
                }
                binding.markupPercentageWholesaleTextField.setText(
                    "%.2f".format(percent)
                )
            } else if (currentTextEdit == "content") {
                val wholeMarkup =
                    binding.markupPercentageWholesaleTextField.text.toString().toFloat()
                binding.wholesalePriceTextField.setText(
                    "%.2f".format(
                        price(
                            unitCost,
                            wholeMarkup
                        ) * content
                    )
                )
            }
        } catch (e: Exception) {

        }
    }

    private fun readItemData(id: String) {
        binding.doneButton.setText(R.string.update_string)
        val item = databaseHelper.checkItemCode(id)
        while (item.moveToNext()) {
            val itemID = item.getString((0))
            val itemName = item.getString((1))
            val itemCategory = item.getString((2))
            val itemCost = item.getString((3))
            val itemPrice = item.getString((4))
            val itemWhole = item.getString((5))
            val itemWholePrice = item.getString((6))
            val itemQuantity = item.getString((7))
            val itemSupplier = item.getString((8))
            val itemRetailMarkup = markup(itemCost.toFloat(), itemPrice.toFloat())
            val itemWholeMarkup = markup(itemCost.toFloat() * itemWhole.toFloat(), itemWholePrice.toFloat())

            binding.itemCodeTextField.setText(itemID)
            binding.itemCodeTextField.isEnabled = false
            binding.itemNameTextField.setText(itemName)
            binding.contentTextField.setText("0")
            binding.categoryInputTextField.setText(itemCategory)
            binding.supplierTextField.setText(itemSupplier)
            binding.unitCostTextField.setText(itemCost)
            binding.retailPriceTextField.setText(itemPrice)
            binding.wholesalePriceTextField.setText(itemWholePrice)
            binding.wholesaleContentQuantityTextField.setText(itemWhole)
            binding.oldRetailPrice.text = itemPrice
            binding.oldWholesalePrice.text = itemWholePrice
            binding.markupPercentageRetailTextField.setText("%.2f".format(itemRetailMarkup))
            binding.markupPercentageWholesaleTextField.setText("%.2f".format(itemWholeMarkup))
            quantity = itemQuantity
        }
    }

//    private fun verifyBarcodeInput() {
//        if (binding.itemCodeTextField.text.toString().isEmpty()) {
//            // All Editable Text Fields will be disabled if the Barcode is Empty
//            binding.itemNameTextField.isEnabled = false
//            binding.supplierTextField.isEnabled = false
//            binding.contentTextField.isEnabled = false
//            binding.purchaseUnitSelect.isEnabled = false
//            binding.unitCostTextField.isEnabled = false
//            binding.markupPercentageRetailTextField.isEnabled = false
//            binding.retailPriceTextField.isEnabled = false
//            binding.markupPercentageWholesaleTextField.isEnabled = false
//            binding.wholesalePriceTextField.isEnabled = false
//        } else if (!binding.itemCodeTextField.text.toString().isEmpty()) {
//            // All Editable Text Fields will be enabled if the Barcode has string
//            binding.itemNameTextField.isEnabled = true
//            binding.supplierTextField.isEnabled = true
//            binding.contentTextField.isEnabled = true
//            binding.purchaseUnitSelect.isEnabled = true
//            binding.unitCostTextField.isEnabled = true
//            binding.markupPercentageRetailTextField.isEnabled = true
//            binding.retailPriceTextField.isEnabled = true
//            binding.markupPercentageWholesaleTextField.isEnabled = true
//            binding.wholesalePriceTextField.isEnabled = true
//        }
//    }

    private fun checkInput(status: String = "half"): Boolean {
        if (binding.markupPercentageRetailTextField.text.toString().isEmpty()) {
            binding.markupPercentageRetailTextField.error = "Please fill this up!"
            return false
        }
        if (binding.unitCostTextField.text.toString().isEmpty() || binding.unitCostTextField.text.toString().toFloat() == 0f) {
            binding.unitCostTextField.error = "Please fill this up!"
            return false
        }
        if (binding.retailPriceTextField.text.toString().isEmpty()) {
            binding.retailPriceTextField.error = "Please fill this up!"
            return false
        }
        if (binding.markupPercentageWholesaleTextField.text.toString().isEmpty()) {
            binding.markupPercentageWholesaleTextField.error = "Please fill this up!"
            return false
        }
        if (binding.wholesalePriceTextField.text.toString().isEmpty()) {
            binding.wholesalePriceTextField.error = "Please fill this up!"
            return false
        }
        if (binding.wholesaleContentQuantityTextField.text.toString().isEmpty()) {
            binding.wholesaleContentQuantityTextField.error = "Please fill this up!"
            return false
        }

        if (status != "half") {
            if (binding.itemNameTextField.text.toString().isEmpty()) {
                binding.itemNameTextField.error = "This field must not be empty!"
                return false
            }
            if (binding.categoryInputTextField.text.toString().isEmpty()) {
                binding.categoryInputTextField.error = "This field must not be empty!"
                return false
            }
            if (binding.itemCodeTextField.text.toString().isEmpty()) {
                binding.itemCodeTextField.error = "This field must not be empty!"
                return false
            }
            if (binding.contentTextField.text.toString().isEmpty()) {
                binding.contentTextField.error = "This field must not be empty!"
                return false
            }
            if (binding.supplierTextField.text.toString().isEmpty()) {
                binding.supplierTextField.error = "This field must not be empty!"
                return false
            }
            if (binding.itemNameTextField.text.toString().contains("^")) {
                binding.itemNameTextField.error = "'^' is invalid!"
                return false
            }
        }
        return true
    }

    private fun saveItem(itemID: String): Boolean{
        val itemBarcode = binding.itemCodeTextField.text.toString()
        val itemName = binding.itemNameTextField.text.toString().toUpperCase()
        val itemCategory = binding.categoryInputTextField.text.toString()
        val itemCost = binding.unitCostTextField.text.toString().toFloat()
        val itemPrice = binding.retailPriceTextField.text.toString().toFloat()
        val itemWhole = binding.wholesaleContentQuantityTextField.text.toString().toFloat()
        val itemWholePrice = binding.wholesalePriceTextField.text.toString().toFloat()
        val itemQuantity = binding.contentTextField.text.toString().toFloat()
        val itemSupplier = binding.supplierTextField.text.toString()
        val cost = itemQuantity * itemCost


        if (itemQuantity != 0f) {
            // Date & Time Format
            val dateTime = Calendar.getInstance().time
            var sdf = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
            val timeText: String = sdf.format(dateTime)
            sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val dateText: String = sdf.format(dateTime)
            sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val monthText: String = sdf.format(dateTime)
            val logsInfo = "$timeText^!^Added ${itemQuantity.toInt()} pc(s) of \"$itemName\" to inventory, worth for $cost PHP.^!^"

            if (databaseHelper.checkLogs(dateText).count < 1) {
                databaseHelper.addLogs(logsInfo, dateText, monthText)
            } else {
                val logsData = databaseHelper.checkLogs(dateText)
                while(logsData.moveToNext()) {
                    val logsInfoNew = logsData.getString((1)) + logsInfo
                    databaseHelper.updateLogs(logsInfoNew, dateText)
                }
            }

            if (databaseHelper.checkSales(monthText).count < 1) {
                databaseHelper.addSales("", "0.00^!^%.2f".format(cost), dateText, monthText, "CLUTTERED")
            } else {
                val salesData = databaseHelper.checkSales(monthText)
                while(salesData.moveToNext()) {
                    val salesInfoNew = salesData.getString((1))
                    val profitInfoData = salesData.getString((2)).split("^!^").toMutableList()
                    val profitInfoNew = "${profitInfoData[0]}^!^" + "%.2f".format(profitInfoData[1].toFloat() + cost)
                    databaseHelper.updateSales(salesInfoNew, profitInfoNew, monthText, "CLUTTERED")
                }
            }
        }

        if (itemID == "new") {
            if (databaseHelper.checkItemCode(binding.itemCodeTextField.text.toString()).count < 1) {
                return databaseHelper.addItem(
                    itemBarcode,
                    itemName,
                    itemCategory,
                    itemCost,
                    itemPrice,
                    itemWhole,
                    itemWholePrice,
                    itemQuantity,
                    itemSupplier
                )
            } else {
                return false
            }
        } else {
            val totalQuantity = quantity.toFloat() + itemQuantity
            return databaseHelper.updateItem(
                itemBarcode,
                itemName,
                itemCategory,
                itemCost,
                itemPrice,
                itemWhole,
                itemWholePrice,
                totalQuantity,
                itemSupplier
            )
        }
    }

    private fun discardBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Discard Inventory Item")
            .setMessage("Do you confirm to discard the details you provided?")
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton("CONFIRM") { _, _ ->
                super.onBackPressed()
                finish()
                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out)
            }.create().show()
    }

    override fun onBackPressed() {
        discardBackPressed()
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

    private fun markup(cost: Float, sell: Float): Float {
        val percent = (sell - cost) / cost * 100
        return percent
    }

    private fun price(cost: Float, percent: Float): Float {
        val sell = (percent / 100) * cost + cost
        return sell
    }


    private fun barcodeCopyTextToClipboard() {
        try {
            val textToCopy = binding.itemCodeTextField.text
            if (TextUtils.isEmpty(textToCopy)) {
                Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show()
            } else {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", textToCopy)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Unable to copy text. Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    class DecimalDigitsInputFilter(digitsBeforeDecimal: Int, digitsAfterDecimal: Int) :
        InputFilter {

        var decimalPattern: Pattern =
            Pattern.compile("[0-9]{0,$digitsBeforeDecimal}+((\\.[0-9]{0,$digitsAfterDecimal})?)||(\\.)?")

        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val matcher: Matcher = decimalPattern.matcher(
                dest?.subSequence(0, dstart).toString() + source?.subSequence(
                    start,
                    end
                ).toString() + dest?.subSequence(dend, dest.length).toString()
            )
            if (!matcher.matches())
                return ""
            else
                return null
        }
    }

}