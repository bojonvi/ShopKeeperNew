package com.mcmcourseproject.shopkeeper.SQLite

import android.provider.BaseColumns

object DatabaseContainer {
    class PersonTable: BaseColumns {
        companion object{
            val ITEM_TABLE = "Item_table"
            val ITEM_ID = "ITEM_ID"
            val ITEM_NAME = "ITEM_NAME"
            val ITEM_CATEGORY = "ITEM_CATEGORY"
            val ITEM_COST = "ITEM_COST"
            val ITEM_PRICE = "ITEM_PRICE"
            val ITEM_QUANTITY = "ITEM_QUANTITY"
            val ITEM_WHOLE = "ITEM_WHOLE"
            val ITEM_WHOLE_PRICE = "ITEM_WHOLE_PRICE"
            val ITEM_SUPPLIER = "ITEM_SUPPLIER"

            val SALES_TABLE = "Sales_table"
            val SALES_INFO = "SALES_INFO"
            val SALES_PROFIT = "SALES_PROFIT"
            val SALES_DATE = "SALES_DATE"
            val SALES_MONTH = "SALES_MONTH"
            val SALES_STATUS = "SALES_STATUS"

            val LOGS_TABLE = "Logs_table"
            val LOGS_INFO = "LOGS_INFO"
            val LOGS_DATE = "LOGS_DATE"
            val LOGS_MONTH = "LOGS_MONTH"

            val USER_TABLE = "User_table"
            val USER_ID = "USER_ID"
            val USER_NAME = "USER_NAME"
            val USER_STORE = "USER_STORE"
            val USER_THEME = "USER_THEME"
            val USER_EMAIL = "USER_EMAIL"
            val STORE_NAME = "STORE_NAME"
            val STORE_EMAIL = "STORE_EMAIL"
            val STORE_ADDRESS = "STORE_ADDRESS"
            val STORE_TELEPHONE = "STORE_TELEPHONE"
            val LAST_SAVED = "LAST_SAVED"

            val CATEGORY_TABLE = "Category_table"
            val CATEGORY_NAME = "CATEGORY_NAME"
        }
    }
}