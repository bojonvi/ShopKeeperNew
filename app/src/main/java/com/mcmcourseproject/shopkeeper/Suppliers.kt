package com.mcmcourseproject.shopkeeper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mcmcourseproject.shopkeeper.databinding.SuppliersActivityBinding

class Suppliers : AppCompatActivity() {
    private lateinit var binding: SuppliersActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SuppliersActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.supplierBackButton.setOnClickListener {
            finish()
        }

        // Supplier List - get string array (this is not yet part of database)
//        val supplierListArray = ArrayList<String>() // Creating an empty ArrayList for itemCategory
//
//        // Create the adapter and set it to the AutoCompleteTextView
//        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, supplierListArray)
//        // To add item dynamically,..
//        binding.supplierListView.adapter = adapter
//
//        binding.supplierListView.setOnItemClickListener { parent, view, position, id ->
//            val selectedValue = adapter.getItem(position)
//                Toast.makeText(this@Suppliers, "Item, $selectedValue,  clicked.", Toast.LENGTH_SHORT).show()
////            Toast.makeText(this@Suppliers, "Item, $selectedValue,  clicked.", Toast.LENGTH_SHORT).show()
//        }

    }


}
