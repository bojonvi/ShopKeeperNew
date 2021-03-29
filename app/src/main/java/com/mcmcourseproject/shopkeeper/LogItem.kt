package com.mcmcourseproject.shopkeeper

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseHelper
import com.mcmcourseproject.shopkeeper.databinding.LogItemActivityBinding

class LogItem : AppCompatActivity() {
    private lateinit var binding: LogItemActivityBinding
    private val databaseHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogItemActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent Variables
        val dateText = intent.getStringExtra("dateText")!!

        binding.logItemBackButton.setOnClickListener {
            finish()
        }
        binding.logItemTitleTextView.text = dateText

        val logsData = databaseHelper.checkLogs(dateText)
        while (logsData.moveToNext()) {
            val logsEntries = logsData.getString((1)).split("^!^").toMutableList().reversed()
            for (i in 0 until logsEntries.size - 1 step 2) {
                createEntryLogs(logsEntries[i+2], logsEntries[i+1])
            }
        }
    }

    private fun createEntryLogs(timeText: String, infoText: String) {
        val layoutLogEntry = LinearLayout(this)
        layoutLogEntry.orientation = LinearLayout.VERTICAL
        if (binding.logItemLinearLayout.childCount % 2 == 0) {
            layoutLogEntry.setBackgroundColor(Color.parseColor("#d3d3d3"))
        }
        layoutLogEntry.gravity = Gravity.CENTER
        layoutLogEntry.setPadding(10, 10, 10, 10)
        val logEntryParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutLogEntry.layoutParams = logEntryParams

        val timeTextView = TextView(this)
        timeTextView.setPadding(5,5,5,5)
        timeTextView.gravity = Gravity.CENTER
        timeTextView.text = timeText
        val infoTextView = TextView(this)
        infoTextView.setPadding(5,5,5,5)
        infoTextView.gravity = Gravity.CENTER
        infoTextView.text = infoText

        layoutLogEntry.addView(timeTextView)
        layoutLogEntry.addView(infoTextView)
        binding.logItemLinearLayout.addView(layoutLogEntry)
    }
}