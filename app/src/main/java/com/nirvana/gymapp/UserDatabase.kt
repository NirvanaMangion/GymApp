package com.nirvana.gymapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

class UserDatabase(context: Context) : SQLiteOpenHelper(context, "users.db", null, 4) {

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

        db.execSQL("""
            CREATE TABLE routine_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT,
                sets INTEGER DEFAULT 1,
                reps INTEGER DEFAULT 0,
                weight REAL DEFAULT 0
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE saved_routines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE routine_exercise_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                routine_id INTEGER,
                name TEXT,
                category TEXT,
                FOREIGN KEY (routine_id) REFERENCES saved_routines(id)
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE users ADD COLUMN weightUnit TEXT")
            db.execSQL("ALTER TABLE users ADD COLUMN distanceUnit TEXT")
            db.execSQL("ALTER TABLE users ADD COLUMN measurementUnit TEXT")
        }
        if (oldVersion < 3) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS routine_exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT,
                    sets INTEGER DEFAULT 1,
                    reps INTEGER DEFAULT 0,
                    weight REAL DEFAULT 0
                );
            """.trimIndent())
        }
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS saved_routines (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS routine_exercise_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    routine_id INTEGER,
                    name TEXT,
                    category TEXT,
                    FOREIGN KEY (routine_id) REFERENCES saved_routines(id)
                );
            """.trimIndent())
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
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun validateCredentials(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", arrayOf(username, password))
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

    fun addExerciseToRoutine(name: String, category: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("category", category)
            put("sets", 1)
            put("reps", 0)
            put("weight", 0.0)
        }
        db.insert("routine_exercises", null, values)
    }

    fun getRoutineExercises(): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name, category FROM routine_exercises", null)
        val list = mutableListOf<Pair<String, String>>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val category = cursor.getString(1)
            list.add(name to category)
        }
        cursor.close()
        return list
    }

    fun clearRoutineExercises() {
        writableDatabase.execSQL("DELETE FROM routine_exercises")
    }

    fun saveRoutine(name: String) {
        val db = writableDatabase
        val routineValues = ContentValues().apply {
            put("name", name)
        }
        val routineId = db.insert("saved_routines", null, routineValues)

        val exercises = getRoutineExercises()
        for ((exerciseName, category) in exercises) {
            val itemValues = ContentValues().apply {
                put("routine_id", routineId)
                put("name", exerciseName)
                put("category", category)
            }
            db.insert("routine_exercise_items", null, itemValues)
        }
    }

    fun getAllSavedRoutines(): List<Pair<Int, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name FROM saved_routines ORDER BY created_at DESC", null)
        val routines = mutableListOf<Pair<Int, String>>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            routines.add(id to name)
        }
        cursor.close()
        return routines
    }

    fun getExercisesForRoutine(routineId: Int): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name, category FROM routine_exercise_items WHERE routine_id = ?",
            arrayOf(routineId.toString())
        )
        val list = mutableListOf<Pair<String, String>>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val category = cursor.getString(1)
            list.add(name to category)
        }
        cursor.close()
        return list
    }
}
