package com.mcmcourseproject.shopkeeper

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.ManageSalesActivityBinding


class ManageSales : AppCompatActivity() {
    private lateinit var binding: ManageSalesActivityBinding
    private val databaseHelper = DatabaseHelper(this)
    private var salesList = mutableMapOf<String, Int>()

    override fun onStart() {
        super.onStart()
        sortSales()
        readSales()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ManageSalesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.manageSalesBackButton.setOnClickListener {
            finish()
        }
    }

    private fun sortSales() {
        val salesListSort = mutableMapOf<String, Int>()
        val salesData = databaseHelper.readSales()
        while (salesData.moveToNext()) {
            if (salesData.getString((5)) == "CLUTTERED") {
                var salesInfoNew = ""
                val profitDataInfo = salesData.getString((2))
                val salesDataMonth = salesData.getString((4))
                val salesListData = salesData.getString((1)).split("^!^").toMutableList()
                for (i in 0 until salesListData.size - 1 step 2) {
                    if (salesListSort.containsKey(salesListData[i])) {
                        salesListSort[salesListData[i]] = salesListSort[salesListData[i]]!! + salesListData[i+1].toInt()
                    } else {
                        salesListSort[salesListData[i]] = salesListData[i+1].toInt()
                    }
                }
                val salesListSorted = salesListSort.toList().sortedBy { (_,v) -> v }.reversed().toMap()
                for (key in salesListSorted.keys) {
                    salesInfoNew += "$key^!^${salesListSorted[key]}^!^"
                }
                databaseHelper.updateSales(salesInfoNew, profitDataInfo, salesDataMonth, "GROUPED")
            }
        }
    }

    private fun readSales() {
        var monthSales = mutableListOf<String>()
        val salesData = databaseHelper.readSales()
        while (salesData.moveToNext()) {
            monthSales.add(salesData.getString((4)))
        }
        monthSales = monthSales.reversed().toMutableList()
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, monthSales)
        binding.salesInputTextField.setAdapter(adapter)
        binding.salesInputTextField.setOnItemClickListener { parent, view, position, id ->
            showSales(monthSales[position])
        }
    }

    private fun showSales(monthText: String) {
        binding.salesLinearLayout.removeAllViews()
        val salesData = databaseHelper.checkSales(monthText)
        while (salesData.moveToNext()) {
            val salesListData = salesData.getString((1)).split("^!^").toMutableList()
            for (i in 0 until salesListData.size - 1 step 2) {
                createSalesView(salesListData[i], salesListData[i+1])
            }
            val profitData = salesData.getString((2)).split("^!^").toMutableList()
            binding.salesIncomeTextView.text = profitData[0]
            binding.salesCostTextView.text = profitData[1]
            binding.salesProfitTextView.text = "%.2f".format(profitData[0].toFloat() - profitData[1].toFloat())
        }
    }

    private fun createSalesView(nameText: String, quantityText: String) {
        val layoutSaleEntry = LinearLayout(this)
        layoutSaleEntry.orientation = LinearLayout.HORIZONTAL
        if (binding.salesLinearLayout.childCount % 2 == 0) {
            layoutSaleEntry.setBackgroundColor(Color.parseColor("#d3d3d3"))
        }
        layoutSaleEntry.gravity = Gravity.CENTER
        layoutSaleEntry.setPadding(10, 10, 10, 10)
        val saleEntryParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutSaleEntry.layoutParams = saleEntryParams

        val nameTextView = TextView(this)
        nameTextView.setPadding(4,4,4,4)
        nameTextView.gravity = Gravity.CENTER
        nameTextView.text = nameText
        val nameTextParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 15f)
        nameTextView.layoutParams = nameTextParams
        val quantityTextView = TextView(this)
        quantityTextView.setPadding(4,4,4,4)
        quantityTextView.gravity = Gravity.CENTER
        quantityTextView.text = quantityText
        val quantityTextParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5f)
        quantityTextView.layoutParams = quantityTextParams

        layoutSaleEntry.addView(nameTextView)
        layoutSaleEntry.addView(quantityTextView)
        binding.salesLinearLayout.addView(layoutSaleEntry)
    }
}