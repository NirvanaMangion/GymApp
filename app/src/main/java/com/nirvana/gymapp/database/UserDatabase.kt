package com.nirvana.gymapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class UserDatabase(context: Context) : SQLiteOpenHelper(context, "users.db", null, 6) {

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
        measurementUnit TEXT,
        profileImageUri TEXT
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

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS completed_routines_v2 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                routine_name TEXT,
                start_time INTEGER,
                end_time INTEGER
            );
        """.trimIndent())

        db.execSQL("""
    CREATE TABLE IF NOT EXISTS measurements (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT NOT NULL,
        weight TEXT,
        chest TEXT,
        waist TEXT,
        arms TEXT,
        timestamp TEXT
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
        if (oldVersion < 5) {
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS completed_routines_v2 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                routine_name TEXT,
                start_time INTEGER,
                end_time INTEGER
            );
        """.trimIndent())
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE users ADD COLUMN profileImageUri TEXT")
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

    // ✅ Save completed routine to v2 table
    fun insertCompletedRoutineV2(userId: String, routineName: String, startTime: Long, endTime: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("routine_name", routineName)
            put("start_time", startTime)
            put("end_time", endTime)
        }
        val result = db.insert("completed_routines_v2", null, values)
        Log.d("DB_INSERT", "insertCompletedRoutineV2: start=$startTime, end=$endTime, rowId=$result")
    }

    // ✅ Get durations from completed_routines_v2
    fun getV2Durations(userId: String): Map<String, Int> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT start_time, end_time FROM completed_routines_v2 WHERE user_id = ?",
            arrayOf(userId)
        )
        val result = mutableMapOf<String, Int>()
        while (cursor.moveToNext()) {
            val start = cursor.getLong(0)
            val end = cursor.getLong(1)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(start))
            val duration = ((end - start) / 1000 / 60).toInt()
            val current = if (result.containsKey(date)) result[date]!! else 0
            result[date] = current + duration
        }
        cursor.close()
        Log.d("ChartData", "V2 durations: $result")
        return result
    }

    // ✅ Simulated volume based on duration
    fun getDailyVolumes(userId: String): Map<String, Int> {
        val rawDurations = getV2Durations(userId)
        val result = mutableMapOf<String, Int>()
        for ((date, duration) in rawDurations) {
            result[date] = duration * 50 // e.g., 50kg/min
        }
        Log.d("ChartData", "V2 volumes: $result")
        return result
    }

    // ✅ Simulated reps based on duration
    fun getDailyReps(userId: String): Map<String, Int> {
        val rawDurations = getV2Durations(userId)
        val result = mutableMapOf<String, Int>()
        for ((date, duration) in rawDurations) {
            result[date] = duration * 2 // e.g., 2 reps/min
        }
        Log.d("ChartData", "V2 reps: $result")
        return result
    }

    data class RoutineLog(
        val routineName: String,
        val startTime: Long,
        val duration: Int,
        val volume: Int,
        val reps: Int
    )

    fun getAllCompletedRoutines(userId: String): List<RoutineLog> {
        val db = readableDatabase
        val result = mutableListOf<RoutineLog>()

        val cursor = db.rawQuery(
            "SELECT routine_name, start_time, end_time FROM completed_routines_v2 WHERE user_id = ? ORDER BY start_time DESC",
            arrayOf(userId)
        )

        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val start = cursor.getLong(1)
            val end = cursor.getLong(2)
            val duration = ((end - start) / 1000 / 60).toInt()
            val volume = duration * 50 // Example logic
            val reps = duration * 2
            result.add(RoutineLog(name, start, duration, volume, reps))
        }
        cursor.close()
        return result
    }
    fun getWorkoutHistoryLogs(userId: String): List<Triple<String, Long, Long>> {
        val db = readableDatabase
        val result = mutableListOf<Triple<String, Long, Long>>()
        val cursor = db.rawQuery(
            "SELECT routine_name, start_time, end_time FROM completed_routines_v2 WHERE user_id = ? ORDER BY start_time DESC",
            arrayOf(userId)
        )
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val start = cursor.getLong(1)
            val end = cursor.getLong(2)
            result.add(Triple(name, start, end))
        }
        cursor.close()
        return result
    }
    fun updateProfileImage(username: String, imageUri: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("profileImageUri", imageUri)
        }
        return db.update("users", values, "username = ?", arrayOf(username)) > 0
    }

    fun getProfileImage(username: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT profileImageUri FROM users WHERE username = ?",
            arrayOf(username)
        )
        var result: String? = null
        if (cursor.moveToFirst()) {
            result = cursor.getString(0)
        }
        cursor.close()
        return result
    }
    fun insertMeasurement(
        username: String,
        weight: String,
        chest: String,
        waist: String,
        arms: String,
        timestamp: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("weight", weight)
            put("chest", chest)
            put("waist", waist)
            put("arms", arms)
            put("timestamp", timestamp)
        }
        return db.insert("measurements", null, values) != -1L
    }
}
