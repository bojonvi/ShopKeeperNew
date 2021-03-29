package com.mcmcourseproject.shopkeeper

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.CheckLogsActivityBinding

class CheckLogs : AppCompatActivity() {
    private lateinit var binding: CheckLogsActivityBinding
    private val databaseHelper = DatabaseHelper(this)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CheckLogsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var logsList = mutableListOf<String>()
        val logsData = databaseHelper.readLogs()
        while(logsData.moveToNext()) {
            logsList.add(logsData.getString((2)))
        }
        logsList = logsList.reversed().toMutableList()

        binding.checkLogsBackButton.setOnClickListener {
            finish()
        }

        binding.logsListView.adapter = MyCustomAdapter(this, logsList)
        binding.logsListView.setOnItemClickListener { parent, view, position, id ->
            val activity = Intent(this, LogItem::class.java)
            activity.putExtra("dateText", logsList[position])
            startActivity(activity)
        }
    }

    private class MyCustomAdapter(context: Context, inventory: MutableList<String>): BaseAdapter() {

        private val mContext: Context

        private val itemList = inventory

        init {
            mContext = context
        }

        override fun getCount(): Int {
            return itemList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val nameTextView = TextView(mContext)
            nameTextView.text = itemList[position]
            nameTextView.textSize = 18f
            nameTextView.gravity = Gravity.CENTER
            nameTextView.setPadding(30, 10, 0, 30)
            return nameTextView
        }
    }
}