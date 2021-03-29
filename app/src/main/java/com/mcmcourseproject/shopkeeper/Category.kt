package com.mcmcourseproject.shopkeeper

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.CategoryActivityBinding

class Category : AppCompatActivity() {
    private lateinit var binding: CategoryActivityBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onStart() {
        super.onStart()
        readDatabase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CategoryActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Database
        databaseHelper = DatabaseHelper(this)

        binding.addCategoryIcon.setOnClickListener {
            addCategory()
        }
        binding.categoryBackButton.setOnClickListener {
            finish()
        }
    }

    private fun addCategory() {
        val categoryName = binding.addCategoryTextField.text.toString().toUpperCase()
        if (categoryName.isNotEmpty()) {
            if (databaseHelper.checkCategory(categoryName)){
                databaseHelper.addCategory(categoryName)
                readDatabase()
            } else {
                binding.addCategoryTextField.error = "Category already exists!"
            }
        }
        else {
            binding.addCategoryTextField.error = "Please fill up this field!"
        }
    }

    private fun readDatabase() {
        binding.categoryMainFrameLinearLayout.removeAllViews()

        val category = databaseHelper.readCategory("CATEGORY_NAME")
        while (category.moveToNext()) {
            val categoryName = category.getString((1))
            createCardEntry(categoryName)
        }
    }

    private fun createCardEntry(categoryName: String = "None", ) {
        val layoutCategoryCard = LinearLayout(this)
        layoutCategoryCard.orientation = LinearLayout.VERTICAL
        val categoryCardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutCategoryCard.layoutParams = categoryCardParams

        val layoutCard = LinearLayout(this)
        layoutCard.orientation = LinearLayout.HORIZONTAL
        layoutCard.setPadding(50,25,50,25)
        if (binding.categoryMainFrameLinearLayout.childCount % 2 == 0) {
            layoutCard.setBackgroundColor(Color.GRAY)
        }
        layoutCard.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        // Card Information
        val categoryNameTextView = TextView(this)
        val categoryNameParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        categoryNameTextView.text = categoryName
        categoryNameTextView.textSize = 15f
        categoryNameTextView.layoutParams = categoryNameParams

        // Card Action Buttons
        val categoryDeleteButton = Button(this)
        val categoryDeleteParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        categoryDeleteButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.declineRed))
        categoryDeleteButton.layoutParams = categoryDeleteParams
        categoryDeleteButton.text = "Delete"
        categoryDeleteButton.textSize = 15f
        categoryDeleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Delete Category")
                .setMessage("Do you want to delete this category from your Inventory?")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES") { _, _ ->
                    databaseHelper.deleteCategory(categoryName)
                    Toast.makeText(this, "$categoryName has now been successfully deleted!", Toast.LENGTH_SHORT).show()
                    readDatabase()
                }.create().show()
        }

        // Add content views to layout
        layoutCard.addView(categoryNameTextView)
        layoutCard.addView(categoryDeleteButton)

        // Creates and add Budget Card to main layout
        layoutCategoryCard.addView(layoutCard)
        binding.categoryMainFrameLinearLayout.addView(layoutCategoryCard)
    }
}