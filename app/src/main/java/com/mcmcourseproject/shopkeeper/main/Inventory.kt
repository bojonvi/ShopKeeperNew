package com.mcmcourseproject.shopkeeper.main

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mcmcourseproject.shopkeeper.Category
import com.mcmcourseproject.shopkeeper.R
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.InventoryActivityBinding
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import java.text.SimpleDateFormat
import java.util.*


class Inventory : AppCompatActivity() {

    private lateinit var binding: InventoryActivityBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var selectedCategory: String
    private lateinit var selectedSort: String

    override fun onStart() {
        super.onStart()
        readDatabase()
        readCategory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InventoryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Disable Soft-Keyboard when this Activity is launched
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        // Initialize SQLite
        databaseHelper = DatabaseHelper(this)

        // Initialize Default Value
        selectedCategory = "ALL"
        selectedSort = "ITEM_NAME"

        binding.fabSpeedDialAddButton.setMenuListener(object : SimpleMenuListenerAdapter() {
            override fun onMenuItemSelected(menuItem: MenuItem?): Boolean {
                when (menuItem?.itemId) {
                    R.id.floatingAddInventoryButton -> {
                        val activity = Intent(this@Inventory, AddItem::class.java)
                        activity.putExtra("itemID", "new")
                        startActivity(activity)
                    }
                    R.id.floatingAddCategoryButton -> startActivity(
                        Intent(this@Inventory, Category::class.java)
                    )
                }
                return false
            }
        })
        
        binding.inventorySearchButton.setOnClickListener {
            readDatabase()
        }

        binding.sortButton.setOnClickListener {
            val sortText = binding.sortButton
            if (sortText.text == "Name") {
                selectedSort = "ITEM_PRICE"
            } else if (sortText.text == "Price") {
                selectedSort = "ITEM_QUANTITY"
            } else {
                selectedSort = "ITEM_NAME"
            }
            readDatabase()
        }
    }

    private fun readDatabase() {
        binding.inventoryListLinearLayout.removeAllViews()
        Toast.makeText(this, selectedCategory, Toast.LENGTH_SHORT).show()

        var count = 0
        val inventory = databaseHelper.readInventory(selectedSort, selectedCategory)
        while (inventory.moveToNext()) {
            val itemName = inventory.getString((1))
            if (itemName.contains(binding.searchEditText.text.toString().toUpperCase())){
                val itemID = inventory.getString((0))
                val itemQuantity = inventory.getString((7))
                val itemPrice = inventory.getString((4))
                val itemCost = inventory.getString((3))
                createItemCard(itemID, itemName, itemPrice, itemQuantity, itemCost)
                count += 1
            }
        }
        binding.totalProductCount.text = count.toString()
        if (inventory.count < 1 || count < 1) {
            val nameTextView = TextView(this)
            val button = Button(this)
            if (inventory.count < 1) {
                nameTextView.text = "Empty, please create a new item"
                button.text = "Add Item"
                button.setOnClickListener {
                    val activity = Intent(this@Inventory, AddItem::class.java)
                    activity.putExtra("itemID", "new")
                    startActivity(activity)
                }
            } else if (count < 1) {
                nameTextView.text = "Couldn't find item"
                button.text = "Refresh"
                button.setOnClickListener {
                    binding.searchEditText.setText("")
                    readDatabase()
                }
            }
            nameTextView.gravity = Gravity.CENTER
            nameTextView.textSize = 20F
            nameTextView.setPadding(0, 20, 0, 20)
            button.gravity = Gravity.CENTER
            binding.inventoryListLinearLayout.addView(nameTextView)
            binding.inventoryListLinearLayout.addView(button)
        }
        if (selectedSort == "ITEM_NAME") {
            binding.sortButton.text = "Name"
        } else if (selectedSort == "ITEM_PRICE") {
            binding.sortButton.text = "Price"
        } else {
            binding.sortButton.text = "Quantity"
        }
    }

    private fun readCategory() {
        binding.categoryLinearLayout.removeAllViews()
        val category = databaseHelper.readCategory("CATEGORY_NAME")
        createCategoryButton("ALL")
        while (category.moveToNext()) {
            val categoryName = category.getString((1))
            createCategoryButton(categoryName)
        }
    }

    private fun createCategoryButton(categoryName: String) {
        val categoryButtonSelection = Button(this)
        val categoryButtonSelectionParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        categoryButtonSelection.layoutParams = categoryButtonSelectionParams
        categoryButtonSelection.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        categoryButtonSelection.text = categoryName
        categoryButtonSelection.textSize = 15f
        categoryButtonSelection.setOnClickListener {
            selectedCategory = categoryName
            readDatabase()
        }
        binding.categoryLinearLayout.addView(categoryButtonSelection)
    }

    private fun createItemCard(itemID: String, itemName: String, itemPrice: String, itemQuantity: String, itemCost: String) {
        val layoutInflater = LayoutInflater.from(this@Inventory)
        val container = binding.inventoryListLinearLayout
        val rowMain = layoutInflater.inflate(R.layout.inventory_list, container, false)

        if (binding.inventoryListLinearLayout.childCount % 2 == 0) {
            rowMain.setBackgroundColor(Color.parseColor("#d3d3d3"))
        }
        val itemNameTextView = rowMain.findViewById<TextView>(R.id.inventoryItemNameTextView)
        val itemIDTextView = rowMain.findViewById<TextView>(R.id.inventoryItemIDTextView)
        val itemPriceTextView = rowMain.findViewById<TextView>(R.id.inventoryItemPriceTextView)
        val itemQuantityTextView = rowMain.findViewById<TextView>(R.id.inventoryItemQuantityTextView)
        val itemDeleteButton = rowMain.findViewById<Button>(R.id.inventoryDeleteButton)
        val itemEditButton = rowMain.findViewById<Button>(R.id.inventoryEditButton)

        itemNameTextView.text = itemName
        itemIDTextView.text = "ID: " + itemID
        itemPriceTextView.text = "%.2f PHP".format(itemPrice.toFloat())
        itemQuantityTextView.text = itemQuantity + " pc(s) in storage"
        val cost = "%.2f".format(itemCost.toFloat() * itemQuantity.toFloat())
        itemDeleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirmation to Delete")
                .setMessage("Do you want to delete this product from your inventory?")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES") { _, _ ->
                    databaseHelper.deleteItem(itemID)
                    // Date & Time Format
                    val dateTime = Calendar.getInstance().time
                    var sdf = SimpleDateFormat("h:mm:ss a")
                    val timeText: String = sdf.format(dateTime)
                    sdf = SimpleDateFormat("MMM d, yyyy")
                    val dateText: String = sdf.format(dateTime)
                    sdf = SimpleDateFormat("MMM yyyy")
                    val monthText: String = sdf.format(dateTime)

                    val logsInfo = "$timeText^!^Deleted ${itemQuantity.toInt()} pc(s) of \"$itemName\" from inventory, worth for $cost PHP.^!^"
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
                        databaseHelper.addSales("", "0.00^!^-%.2f".format(cost.toFloat()), dateText, monthText, "CLUTTERED")
                    } else {
                        val salesData = databaseHelper.checkSales(monthText)
                        while(salesData.moveToNext()) {
                            val salesInfoNew = salesData.getString((1))
                            val profitInfoData = salesData.getString((2)).split("^!^").toMutableList()
                            val profitInfoNew = "${profitInfoData[0]}^!^" + "%.2f".format(profitInfoData[1].toFloat() - cost.toFloat())
                            databaseHelper.updateSales(salesInfoNew, profitInfoNew, monthText, "CLUTTERED")
                        }
                    }
                    readDatabase()
                }.create().show()
        }
        itemEditButton.setOnClickListener {
            val activity = Intent(this@Inventory, AddItem::class.java)
            activity.putExtra("itemID", itemID)
            startActivity(activity)
        }
        container.addView(rowMain)
    }
}