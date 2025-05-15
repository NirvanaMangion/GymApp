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
        db.execSQL(
            """
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
        """
        )

        db.execSQL(
            """
            CREATE TABLE routine_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                name TEXT NOT NULL,
                category TEXT,
                sets INTEGER DEFAULT 1,
                reps INTEGER DEFAULT 0,
                weight REAL DEFAULT 0
            );
        """
        )

        db.execSQL(
            """
            CREATE TABLE saved_routines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                name TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """
        )

        db.execSQL(
            """
            CREATE TABLE routine_exercise_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                routine_id INTEGER,
                name TEXT,
                category TEXT,
                FOREIGN KEY (routine_id) REFERENCES saved_routines(id)
            );
        """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS completed_routines_v2 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                routine_name TEXT,
                start_time INTEGER,
                end_time INTEGER
            );
        """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS measurements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                weight TEXT,
                chest TEXT,
                waist TEXT,
                arms TEXT,
                timestamp TEXT
            );
        """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS progress_photos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                timestamp TEXT NOT NULL,
                photo_path TEXT NOT NULL
            );
        """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 8) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS progress_photos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    photo_path TEXT NOT NULL
                );
            """
            )
        }
    }

    fun insertProgressPhoto(username: String, timestamp: String, photoPath: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("timestamp", timestamp)
            put("photo_path", photoPath)
        }
        return db.insert("progress_photos", null, values) != -1L
    }

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

    fun savePhotoToStorage(context: Context, bitmap: Bitmap, filename: String): String {
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
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

    fun updateUserPassword(username: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("password", newPassword)
        }
        return db.update("users", values, "username = ?", arrayOf(username)) > 0
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
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE username = ? AND password = ?",
            arrayOf(username, password)
        )
        val valid = cursor.count > 0
        cursor.close()
        return valid
    }

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


    fun setUserUnits(username: String, weight: String, distance: String, measure: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("weightUnit", weight)
            put("distanceUnit", distance)
            put("measurementUnit", measure)
        }
        return db.update("users", values, "username = ?", arrayOf(username)) > 0
    }

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

    fun getRoutineExercises(username: String): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name, category FROM routine_exercises WHERE username = ?", arrayOf(username))
        val list = mutableListOf<Pair<String, String>>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val category = cursor.getString(1)
            list.add(name to category)
        }
        cursor.close()
        return list
    }

    fun clearRoutineExercises(username: String) {
        writableDatabase.delete("routine_exercises", "username = ?", arrayOf(username))
    }

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

    fun getAllSavedRoutines(username: String): List<Pair<Int, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name FROM saved_routines WHERE username = ? ORDER BY created_at DESC", arrayOf(username))
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

    fun getDailyVolumes(userId: String): Map<String, Int> {
        val rawDurations = getV2Durations(userId)
        return rawDurations.mapValues { it.value * 50 }
    }

    fun getDailyReps(userId: String): Map<String, Int> {
        val rawDurations = getV2Durations(userId)
        return rawDurations.mapValues { it.value * 2 }
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
            val volume = duration * 50
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
