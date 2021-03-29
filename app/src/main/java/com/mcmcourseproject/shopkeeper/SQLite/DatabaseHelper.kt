package com.mcmcourseproject.shopkeeper.SQLite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.CATEGORY_NAME
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.CATEGORY_TABLE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_CATEGORY
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_COST
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_ID
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_NAME
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_PRICE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_QUANTITY
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_SUPPLIER
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_TABLE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_WHOLE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.ITEM_WHOLE_PRICE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.LAST_SAVED
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.LOGS_DATE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.LOGS_INFO
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.LOGS_MONTH
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.LOGS_TABLE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.SALES_DATE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.SALES_INFO
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.SALES_MONTH
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.SALES_PROFIT
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.SALES_STATUS
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.SALES_TABLE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.STORE_ADDRESS
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.STORE_EMAIL
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.STORE_NAME
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.STORE_TELEPHONE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.USER_EMAIL
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.USER_ID
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.USER_NAME
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.USER_STORE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.USER_TABLE
import com.mcmcourseproject.shopkeeper.SQLite.DatabaseContainer.PersonTable.Companion.USER_THEME

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        var createTable = "CREATE TABLE " + ITEM_TABLE + "(" +
                ITEM_ID + " INTEGER PRIMARY KEY, " +
                ITEM_NAME + " TEXT, " +
                ITEM_CATEGORY + " TEXT, " +
                ITEM_COST + " REAL, " +
                ITEM_PRICE + " REAL, " +
                ITEM_WHOLE + " REAL, " +
                ITEM_WHOLE_PRICE + " REAL, " +
                ITEM_QUANTITY + " REAL," +
                ITEM_SUPPLIER + " TEXT" + ")"
        db!!.execSQL(createTable)
        createTable = "CREATE TABLE " + USER_TABLE + "(" +
                USER_ID + " TEXT, " +
                USER_NAME + " TEXT, " +
                USER_EMAIL + " TEXT, " +
                USER_STORE + " TEXT, " +
                USER_THEME + " TEXT, " +
                STORE_NAME + " TEXT, " +
                STORE_ADDRESS + " TEXT, " +
                STORE_EMAIL + " TEXT, " +
                STORE_TELEPHONE + " TEXT, " +
                LAST_SAVED + " TEXT" + ")"
        db!!.execSQL(createTable)
        createTable = "CREATE TABLE " + CATEGORY_TABLE + "(" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CATEGORY_NAME + " TEXT" + ")"
        db!!.execSQL(createTable)
        createTable = "CREATE TABLE " + LOGS_TABLE + "(" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOGS_INFO + " TEXT, " +
                LOGS_DATE + " TEXT, " +
                LOGS_MONTH + " TEXT" + ")"
        db!!.execSQL(createTable)
        createTable = "CREATE TABLE " + SALES_TABLE + "(" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SALES_INFO + " TEXT, " +
                SALES_PROFIT + " TEXT, " +
                SALES_DATE + " TEXT, " +
                SALES_MONTH + " TEXT, " +
                SALES_STATUS + " TEXT" + ")"
        db!!.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $ITEM_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $USER_TABLE")
    }

    fun createUserAccount(
        id: String,
        name: String,
        email: String,
        store: String,
        theme: String,
        storeName: String,
        storeAddress: String,
        storeEmail: String,
        storeTelephone: String,
        lastSaved: String
    ): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(USER_ID, id)
        contentValues.put(USER_NAME, name)
        contentValues.put(USER_EMAIL, email)
        contentValues.put(USER_STORE, store)
        contentValues.put(USER_THEME, theme)
        contentValues.put(STORE_NAME, storeName)
        contentValues.put(STORE_ADDRESS, storeAddress)
        contentValues.put(STORE_EMAIL, storeEmail)
        contentValues.put(STORE_TELEPHONE, storeTelephone)
        contentValues.put(LAST_SAVED, lastSaved)
        val insert_data = db.insert(USER_TABLE, null, contentValues)
        db.close()

        return !insert_data.equals(-1)
    }

    fun readUserAccount(): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        val read: Cursor = db.rawQuery("SELECT * FROM $USER_TABLE", null)
        return read
    }

    fun updateUserAccount(email: String, storeName: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(USER_EMAIL, email)
        contentValues.put(STORE_NAME, storeName)
        val update_data = db.update(USER_TABLE, contentValues, "$USER_ID=?", arrayOf("guest"))
        db.close()
        return !update_data.equals(-1)
    }

    fun checkItemCode(itemCode: String): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        val read: Cursor =
            db.rawQuery("SELECT * FROM $ITEM_TABLE WHERE $ITEM_ID = '$itemCode'", null)
        return read
    }

    fun addItem(
        id: String,
        name: String,
        category: String,
        cost: Float,
        price: Float,
        whole: Float,
        wholePrice: Float,
        quantity: Float,
        supplier: String
    ): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ITEM_ID, id)
        contentValues.put(ITEM_NAME, name)
        contentValues.put(ITEM_CATEGORY, category)
        contentValues.put(ITEM_COST, cost)
        contentValues.put(ITEM_PRICE, price)
        contentValues.put(ITEM_WHOLE, whole)
        contentValues.put(ITEM_WHOLE_PRICE, wholePrice)
        contentValues.put(ITEM_QUANTITY, quantity)
        contentValues.put(ITEM_SUPPLIER, supplier)
        val insert_data = db.insert(ITEM_TABLE, null, contentValues)
        db.close()

        return !insert_data.equals(-1)
    }

    fun updateItem(
        id: String,
        name: String,
        category: String,
        cost: Float,
        price: Float,
        whole: Float,
        wholePrice: Float,
        quantity: Float,
        supplier: String
    ): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ITEM_ID, id)
        contentValues.put(ITEM_NAME, name)
        contentValues.put(ITEM_CATEGORY, category)
        contentValues.put(ITEM_COST, cost)
        contentValues.put(ITEM_PRICE, price)
        contentValues.put(ITEM_WHOLE, whole)
        contentValues.put(ITEM_WHOLE_PRICE, wholePrice)
        contentValues.put(ITEM_QUANTITY, quantity)
        contentValues.put(ITEM_SUPPLIER, supplier)
        val update_data = db.update(ITEM_TABLE, contentValues, "$ITEM_ID=?", arrayOf(id))
        db.close()
        return !update_data.equals(-1)
    }

    fun updateItemQuantity(id: String, quantity: Float): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ITEM_QUANTITY, quantity)
        val update_data = db.update(ITEM_TABLE, contentValues, "$ITEM_ID=?", arrayOf(id))
        db.close()
        return !update_data.equals(-1)
    }

    fun deleteItem(id: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val delete_data = db.delete(ITEM_TABLE, "$ITEM_ID=?", arrayOf(id))
        return !delete_data.equals(-1)
    }

    fun readInventory(orderIn: String, category: String): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        if (category != "ALL") {
            val read: Cursor = db.rawQuery(
                "SELECT * FROM $ITEM_TABLE WHERE $ITEM_CATEGORY = '$category' ORDER BY $orderIn ASC",
                null
            )
            return read
        } else {
            val read: Cursor = db.rawQuery("SELECT * FROM $ITEM_TABLE ORDER BY $orderIn ASC", null)
            return read
        }
    }

    fun readCategory(orderIn: String): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        val read: Cursor = db.rawQuery("SELECT * FROM $CATEGORY_TABLE ORDER BY $orderIn ASC", null)
        return read
    }

    fun addCategory(name: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(CATEGORY_NAME, name)
        val insert_data = db.insert(CATEGORY_TABLE, null, contentValues)
        db.close()

        return !insert_data.equals(-1)
    }

    fun checkCategory(category: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val read: Cursor =
            db.rawQuery("SELECT * FROM $CATEGORY_TABLE WHERE $CATEGORY_NAME = '$category'", null)
        val count = read.count
        read.close()
        return count < 1
    }

    fun deleteCategory(name: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val delete_data = db.delete(CATEGORY_TABLE, "$CATEGORY_NAME=?", arrayOf(name))
        return !delete_data.equals(-1)
    }

    fun checkLogs(dateText: String): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        val read: Cursor =
            db.rawQuery("SELECT * FROM $LOGS_TABLE WHERE $LOGS_DATE = '$dateText'", null)
        return read
    }

    fun addLogs(infoText: String, dateText: String, monthText: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(LOGS_INFO, infoText)
        contentValues.put(LOGS_DATE, dateText)
        contentValues.put(LOGS_MONTH, monthText)
        val insert_data = db.insert(LOGS_TABLE, null, contentValues)
        db.close()

        return !insert_data.equals(-1)
    }

    fun updateLogs(infoText: String, dateText: String): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(LOGS_INFO, infoText)
        val update_data = db.update(LOGS_TABLE, contentValues, "$LOGS_DATE=?", arrayOf(dateText))
        db.close()
        return !update_data.equals(-1)
    }

    fun readLogs(): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        val read: Cursor = db.rawQuery("SELECT * FROM $LOGS_TABLE", null)
        return read
    }

    fun checkSales(monthText: String): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        val read: Cursor =
            db.rawQuery("SELECT * FROM $SALES_TABLE WHERE $SALES_MONTH = '$monthText'", null)
        return read
    }

    fun addSales(
        infoText: String,
        profitInfo: String,
        dateText: String,
        monthText: String,
        salesStatus: String
    ): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(SALES_INFO, infoText)
        contentValues.put(SALES_PROFIT, profitInfo)
        contentValues.put(SALES_DATE, dateText)
        contentValues.put(SALES_MONTH, monthText)
        contentValues.put(SALES_STATUS, salesStatus)
        val insert_data = db.insert(SALES_TABLE, null, contentValues)
        db.close()

        return !insert_data.equals(-1)
    }

    fun updateSales(
        infoText: String,
        profitInfo: String,
        monthText: String,
        salesStatus: String
    ): Boolean {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(SALES_INFO, infoText)
        contentValues.put(SALES_PROFIT, profitInfo)
        contentValues.put(SALES_STATUS, salesStatus)
        val update_data =
            db.update(SALES_TABLE, contentValues, "$SALES_MONTH=?", arrayOf(monthText))
        db.close()
        return !update_data.equals(-1)
    }

    fun readSales(): Cursor {
        val db: SQLiteDatabase = this.writableDatabase
        val read: Cursor = db.rawQuery("SELECT * FROM $SALES_TABLE", null)
        return read
    }

    companion object {
        private const val DATABASE_NAME = "Person.db"
        private const val DATABASE_VERSION = 1
    }
}