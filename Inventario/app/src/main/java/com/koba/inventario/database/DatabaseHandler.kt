package com.koba.inventario.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.os.Environment
import java.io.File

//creating the database logic, extending the SQLiteOpenHelper base class
class DatabaseHandler(): SQLiteOpenHelper(null,DATABASE_NAME,null,DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = Environment.getDownloadCacheDirectory().canonicalPath + File.separator + "LogDatabase"
        //private val DATABASE_NAME = "/temp/LogDatabase"
        private val TABLE_LOGS = "LogTable"
        private val KEY_ID = "id"
        private val KEY_NAME = "type"
        private val KEY_MESSAGE = "message"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        //creating table with fields
        println("DBBBBB " +DATABASE_NAME);
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_LOGS + "("
                + KEY_ID + " TEXT," + KEY_NAME + " TEXT,"
                + KEY_MESSAGE + " TEXT" + ")")
        db?.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS)
        onCreate(db)
    }

    //method to insert data
    fun add(id: String, type: String, message: String):Long{
        println("DBBBBB " +DATABASE_NAME);
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, id)
        contentValues.put(KEY_NAME, type)
        contentValues.put(KEY_MESSAGE, message)
        // Inserting Row
        val success = db.insert(TABLE_LOGS, null, contentValues)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }
}