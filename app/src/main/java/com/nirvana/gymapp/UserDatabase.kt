package com.nirvana.gymapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

class UserDatabase(context: Context) : SQLiteOpenHelper(context, "users.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                email TEXT,
                phone TEXT,
                password TEXT NOT NULL,
                weightUnit TEXT,
                distanceUnit TEXT,
                measurementUnit TEXT
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN weightUnit TEXT")
            db.execSQL("ALTER TABLE users ADD COLUMN distanceUnit TEXT")
            db.execSQL("ALTER TABLE users ADD COLUMN measurementUnit TEXT")
        }
    }

    fun addUser(username: String, email: String?, phone: String?, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("email", email)
            put("phone", phone)
            put("password", password)
        }
        return db.insert("users", null, values) != -1L
    }

    fun checkUserExists(username: String): Boolean {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun validateCredentials(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", arrayOf(username, password))
        val valid = cursor.count > 0
        cursor.close()
        return valid
    }

    fun setUserUnits(username: String, weight: String, distance: String, measure: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("weightUnit", weight)
            put("distanceUnit", distance)
            put("measurementUnit", measure)
        }
        return db.update("users", values, "username = ?", arrayOf(username)) > 0
    }
}
