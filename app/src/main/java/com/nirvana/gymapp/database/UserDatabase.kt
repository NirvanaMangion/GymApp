package com.nirvana.gymapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class MeasurementEntry(
    val timestamp: String,
    val weight: String,
    val chest: String,
    val waist: String,
    val arms: String
)

class UserDatabase(context: Context) : SQLiteOpenHelper(context, "users.db", null, 8) {

    override fun onCreate(db: SQLiteDatabase) {
        // User info table
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
        """)

        // Temporary routine builder table
        db.execSQL("""
            CREATE TABLE routine_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                name TEXT NOT NULL,
                category TEXT,
                sets INTEGER DEFAULT 1,
                reps INTEGER DEFAULT 0,
                weight REAL DEFAULT 0
            );
        """)

        // Saved routines table
        db.execSQL("""
            CREATE TABLE saved_routines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                name TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """)

        // Exercises linked to saved routines
        db.execSQL("""
            CREATE TABLE routine_exercise_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                routine_id INTEGER,
                name TEXT,
                category TEXT,
                FOREIGN KEY (routine_id) REFERENCES saved_routines(id)
            );
        """)

        // Completed routine sessions
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS completed_routines_v2 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                routine_name TEXT,
                start_time INTEGER,
                end_time INTEGER
            );
        """)

        // Body measurements
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
        """)

        // Progress photo paths
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS progress_photos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                photo_path TEXT NOT NULL
            );
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add photos table if updating from older version
        if (oldVersion < 8) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS progress_photos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    photo_path TEXT NOT NULL
                );
            """)
        }
    }

    // Insert new photo record
    fun insertProgressPhoto(username: String, timestamp: String, photoPath: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("timestamp", timestamp)
            put("photo_path", photoPath)
        }
        return db.insert("progress_photos", null, values) != -1L
    }

    // Get all photos for a measurement entry
    fun getPhotosForMeasurement(username: String, timestamp: String): List<String> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT photo_path FROM progress_photos WHERE username = ? AND timestamp = ?",
            arrayOf(username, timestamp)
        )
        val paths = mutableListOf<String>()
        while (cursor.moveToNext()) {
            paths.add(cursor.getString(0))
        }
        cursor.close()
        return paths
    }

    // Delete measurement and its associated photos
    fun deleteMeasurement(username: String, timestamp: String) {
        val db = writableDatabase
        val photoPaths = getPhotosForMeasurement(username, timestamp)
        for (path in photoPaths) {
            try {
                File(path).delete()
            } catch (e: Exception) {
                Log.e("UserDatabase", "Failed to delete image: $path")
            }
        }
        db.delete("progress_photos", "username = ? AND timestamp = ?", arrayOf(username, timestamp))
        db.delete("measurements", "username = ? AND timestamp = ?", arrayOf(username, timestamp))
    }

    // Get all measurements for user
    fun getMeasurementsForUser(username: String): List<MeasurementEntry> {
        val list = mutableListOf<MeasurementEntry>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT timestamp, weight, chest, waist, arms FROM measurements WHERE username = ? ORDER BY timestamp DESC",
            arrayOf(username)
        )
        while (cursor.moveToNext()) {
            val timestamp = cursor.getString(0)
            val weight = cursor.getString(1)
            val chest = cursor.getString(2)
            val waist = cursor.getString(3)
            val arms = cursor.getString(4)
            list.add(MeasurementEntry(timestamp, weight, chest, waist, arms))
        }
        cursor.close()
        return list
    }

    // Save bitmap to internal storage
    fun savePhotoToStorage(context: Context, bitmap: Bitmap, filename: String): String {
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    // Add new user
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

    // Get user's password
    fun getPasswordForUser(username: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT password FROM users WHERE username = ?", arrayOf(username))
        var password: String? = null
        if (cursor.moveToFirst()) {
            password = cursor.getString(0)
        }
        cursor.close()
        return password
    }

    // Update user's password
    fun updateUserPassword(username: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("password", newPassword)
        }
        return db.update("users", values, "username = ?", arrayOf(username)) > 0
    }

    // Check if user exists
    fun checkUserExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", arrayOf(username))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Check credentials match
    fun validateCredentials(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE username = ? AND password = ?",
            arrayOf(username, password)
        )
        val valid = cursor.count > 0
        cursor.close()
        return valid
    }

    // Get email and phone
    fun getUserDetails(username: String): Pair<String?, String?> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT email, phone FROM users WHERE username = ?",
            arrayOf(username)
        )
        var email: String? = null
        var phone: String? = null
        if (cursor.moveToFirst()) {
            email = cursor.getString(0)
            phone = cursor.getString(1)
        }
        cursor.close()
        return Pair(email, phone)
    }

    // Update units (weight, distance, measurement)
    fun setUserUnits(username: String, weight: String, distance: String, measure: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("weightUnit", weight)
            put("distanceUnit", distance)
            put("measurementUnit", measure)
        }
        return db.update("users", values, "username = ?", arrayOf(username)) > 0
    }

    // Add exercise to temp routine
    fun addExerciseToRoutine(username: String, name: String, category: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("name", name)
            put("category", category)
            put("sets", 1)
            put("reps", 0)
            put("weight", 0.0)
        }
        db.insert("routine_exercises", null, values)
    }

    // Get temp routine exercises
    fun getRoutineExercises(username: String): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name, category FROM routine_exercises WHERE username = ?",
            arrayOf(username)
        )
        val list = mutableListOf<Pair<String, String>>()
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0) to cursor.getString(1))
        }
        cursor.close()
        return list
    }

    // Clear temp routine
    fun clearRoutineExercises(username: String) {
        writableDatabase.delete("routine_exercises", "username = ?", arrayOf(username))
    }

    // Save routine to permanent table
    fun saveRoutine(username: String, name: String) {
        val db = writableDatabase
        val routineValues = ContentValues().apply {
            put("username", username)
            put("name", name)
        }
        val routineId = db.insert("saved_routines", null, routineValues)

        val exercises = getRoutineExercises(username)
        for ((exerciseName, category) in exercises) {
            val itemValues = ContentValues().apply {
                put("routine_id", routineId)
                put("name", exerciseName)
                put("category", category)
            }
            db.insert("routine_exercise_items", null, itemValues)
        }
    }

    // Get saved routines
    fun getAllSavedRoutines(username: String): List<Pair<Int, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name FROM saved_routines WHERE username = ? ORDER BY created_at DESC",
            arrayOf(username)
        )
        val routines = mutableListOf<Pair<Int, String>>()
        while (cursor.moveToNext()) {
            routines.add(cursor.getInt(0) to cursor.getString(1))
        }
        cursor.close()
        return routines
    }

    // Get exercises for a specific saved routine
    fun getExercisesForRoutine(routineId: Int): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name, category FROM routine_exercise_items WHERE routine_id = ?",
            arrayOf(routineId.toString())
        )
        val list = mutableListOf<Pair<String, String>>()
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0) to cursor.getString(1))
        }
        cursor.close()
        return list
    }

    // Insert completed workout session
    fun insertCompletedRoutineV2(userId: String, routineName: String, startTime: Long, endTime: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("routine_name", routineName)
            put("start_time", startTime)
            put("end_time", endTime)
        }
        db.insert("completed_routines_v2", null, values)
    }

    // Get workout durations per day
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
            result[date] = result.getOrDefault(date, 0) + duration
        }
        cursor.close()
        return result
    }

    // Estimate volume per day
    fun getDailyVolumes(userId: String): Map<String, Int> {
        val rawDurations = getV2Durations(userId)
        return rawDurations.mapValues { it.value * 50 }
    }

    // Estimate reps per day
    fun getDailyReps(userId: String): Map<String, Int> {
        val rawDurations = getV2Durations(userId)
        return rawDurations.mapValues { it.value * 2 }
    }

    data class RoutineLog(val routineName: String, val startTime: Long, val duration: Int, val volume: Int, val reps: Int)

    // Get all routine logs
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
            val volume = duration * 50
            val reps = duration * 2
            result.add(RoutineLog(name, start, duration, volume, reps))
        }
        cursor.close()
        return result
    }

    // Get all workout sessions as Triples
    fun getWorkoutHistoryLogs(userId: String): List<Triple<String, Long, Long>> {
        val db = readableDatabase
        val result = mutableListOf<Triple<String, Long, Long>>()
        val cursor = db.rawQuery(
            "SELECT routine_name, start_time, end_time FROM completed_routines_v2 WHERE user_id = ? ORDER BY start_time DESC",
            arrayOf(userId)
        )
        while (cursor.moveToNext()) {
            result.add(Triple(cursor.getString(0), cursor.getLong(1), cursor.getLong(2)))
        }
        cursor.close()
        return result
    }

    // Update user's profile image
    fun updateProfileImage(username: String, imageUri: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("profileImageUri", imageUri)
        }
        return db.update("users", values, "username = ?", arrayOf(username)) > 0
    }

    // Get user's profile image
    fun getProfileImage(username: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT profileImageUri FROM users WHERE username = ?", arrayOf(username))
        var result: String? = null
        if (cursor.moveToFirst()) result = cursor.getString(0)
        cursor.close()
        return result
    }

    // Insert new measurement entry
    fun insertMeasurement(username: String, weight: String, chest: String, waist: String, arms: String, timestamp: String): Boolean {
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

    // Delete user and all related data
    fun deleteUserCompletely(username: String): Boolean {
        val db = writableDatabase

        // Delete user's progress photos
        val photoCursor = db.rawQuery("SELECT photo_path FROM progress_photos WHERE username = ?", arrayOf(username))
        while (photoCursor.moveToNext()) {
            try {
                File(photoCursor.getString(0)).delete()
            } catch (e: Exception) {
                Log.e("UserDatabase", "Failed to delete photo: ${e.message}")
            }
        }
        photoCursor.close()

        // Delete user-related data from all tables
        db.delete("progress_photos", "username = ?", arrayOf(username))
        db.delete("measurements", "username = ?", arrayOf(username))
        db.delete("routine_exercises", "username = ?", arrayOf(username))
        db.delete("completed_routines_v2", "user_id = ?", arrayOf(username))

        val routineCursor = db.rawQuery("SELECT id FROM saved_routines WHERE username = ?", arrayOf(username))
        val routineIds = mutableListOf<Int>()
        while (routineCursor.moveToNext()) {
            routineIds.add(routineCursor.getInt(0))
        }
        routineCursor.close()

        for (id in routineIds) {
            db.delete("routine_exercise_items", "routine_id = ?", arrayOf(id.toString()))
        }
        db.delete("saved_routines", "username = ?", arrayOf(username))

        // Delete the user
        return db.delete("users", "username = ?", arrayOf(username)) > 0
    }

    // Delete a saved routine by its ID
    fun deleteRoutineById(routineId: Int): Boolean {
        val db = writableDatabase
        db.delete("routine_exercise_items", "routine_id = ?", arrayOf(routineId.toString()))
        return db.delete("saved_routines", "id = ?", arrayOf(routineId.toString())) > 0
    }
}
